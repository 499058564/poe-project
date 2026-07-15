package com.poe.cache.manager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * SQLite 数据库管理器（线程安全单例，HikariCP 连接池）。
 * <p>
 * 负责连接池生命周期管理。
 * 数据库文件位于 {@code ~/.poe-tool/data/poe.db}。
 * <p>
 * 连接池配置：最大 5 个连接，WAL 模式，支持并发读。
 * 内存数据库模式自动降级为单连接池（{@code file::memory:?cache=shared}）。
 * <p>
 * 自动检测 JUnit 测试环境（Gradle / Maven / IDE），检测到时默认使用内存数据库。
 * 通过 {@link #testMode} / {@link #testDbPath} 支持手动覆盖测试数据库配置。
 * 通过 {@link #DatabaseManager(String)} 构造函数支持自定义路径（如 SeedDbBuilder）。
 */
public class DatabaseManager {

    static final String DB_PATH =
        System.getProperty("user.home") + "/.poe-tool/data/poe.db";

    private static final int MAX_POOL_SIZE = 5;
    private static final int IN_MEMORY_POOL_SIZE = 1;
    private static final int CONNECTION_TIMEOUT_MS = 5000;
    private static final int IDLE_TIMEOUT_MS = 600_000;
    private static final int MAX_LIFETIME_MS = 1_800_000;

    private static DatabaseManager instance;
    private DataSource dataSource;
    private volatile boolean poolClosed = true;
    private final String dbPath;
    private final Object poolLock = new Object();

    // 测试模式：设为 true 后 getConnection() 使用 testDbPath 而非默认磁盘路径。
    // 保留为手动覆盖项，通常不需要设置——框架会自动检测测试环境并使用内存数据库。
    public static volatile boolean testMode = false;
    public static String testDbPath = null;

    /** 自动检测结果缓存，延迟初始化。 */
    private static Boolean autoTestDetected = null;

    private DatabaseManager() {
        this.dbPath = DB_PATH;
    }

    /**
     * 使用自定义数据库路径创建管理器（非单例）。
     * <p>适用于 SeedDbBuilder 等独立工具链。
     */
    public DatabaseManager(String dbPath) {
        this.dbPath = dbPath;
    }

    /**
     * 获取单例实例（双重检查锁定）。
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * 从连接池获取数据库连接。
     * <p>调用方 <b>必须</b>在使用完毕后调用 {@link Connection#close()} 归还连接到池中。
     *
     * @return 池化连接
     */
    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /**
     * 获取连接池 {@link DataSource}，供 DAO 层按需借还连接。
     * <p>首次调用自动初始化连接池。
     *
     * @return HikariCP 数据源
     */
    public DataSource getDataSource() throws SQLException {
        if (dataSource == null || poolClosed) {
            synchronized (poolLock) {
                if (dataSource == null || poolClosed) {
                    initPool();
                }
            }
        }
        return dataSource;
    }

    private void initPool() throws SQLException {
        String url = getDbUrl();
        boolean inMemory = isInMemory();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setMaximumPoolSize(inMemory ? IN_MEMORY_POOL_SIZE : MAX_POOL_SIZE);
        config.setMinimumIdle(inMemory ? 1 : 0);
        config.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
        config.setIdleTimeout(IDLE_TIMEOUT_MS);
        config.setMaxLifetime(MAX_LIFETIME_MS);

        if (!inMemory) {
            // 磁盘数据库启用 WAL + 性能优化 PRAGMA
            config.setConnectionInitSql(
                "PRAGMA journal_mode=WAL;" +
                "PRAGMA busy_timeout=5000;" +
                "PRAGMA synchronous=NORMAL;" +
                "PRAGMA foreign_keys=ON"
            );
        }

        HikariDataSource pool = new HikariDataSource(config);
        dataSource = SqlLoggingDataSource.wrap(pool);
        poolClosed = false;
    }

    /**
     * 自动检测当前是否运行在测试环境中。
     * <p>检测顺序：Gradle test worker 属性 → Maven Surefire 属性 → JUnit 堆栈。
     * 结果缓存，全局仅计算一次。
     */
    static synchronized boolean isTestEnvironment() {
        if (autoTestDetected == null) {
            if (System.getProperty("org.gradle.test.worker") != null) {
                autoTestDetected = true;
            } else if (System.getProperty("surefire.test.class.path") != null) {
                autoTestDetected = true;
            } else {
                for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
                    if (e.getClassName().startsWith("org.junit.")) {
                        autoTestDetected = true;
                        break;
                    }
                }
                if (autoTestDetected == null) {
                    autoTestDetected = false;
                }
            }
        }
        return autoTestDetected;
    }

    private boolean isInMemory() {
        // 手动 testMode + testDbPath 优先
        if (testMode && testDbPath != null) {
            return ":memory:".equals(testDbPath);
        }
        // 自动检测到测试环境时默认使用内存数据库
        if (isTestEnvironment()) {
            return true;
        }
        return ":memory:".equals(dbPath);
    }

    private String getDbUrl() {
        // 手动 testMode + testDbPath 优先
        if (testMode && testDbPath != null) {
            if (":memory:".equals(testDbPath)) {
                return "jdbc:sqlite:file::memory:?cache=shared";
            }
            return "jdbc:sqlite:" + testDbPath;
        }
        // 自动检测到测试环境时默认使用内存数据库
        if (isTestEnvironment()) {
            return "jdbc:sqlite:file::memory:?cache=shared";
        }
        return "jdbc:sqlite:" + dbPath;
    }

    /**
     * 初始化数据库连接池。
     * 仅在连接池尚未初始化时触发；已初始化时幂等跳过。
     */
    public void init() {
        if (dataSource == null || poolClosed) {
            synchronized (poolLock) {
                if (dataSource == null || poolClosed) {
                    try {
                        initPool();
                    } catch (SQLException e) {
                        throw new RuntimeException("Failed to initialise database pool", e);
                    }
                }
            }
        }
    }

    /**
     * 强制初始化：关闭旧连接池 + 重新创建（用于 SeedDbBuilder 首次构建新库）。
     */
    public void forceInit() throws SQLException {
        close();
        initPool();
    }

    /**
     * 关闭连接池并重置状态，线程安全。
     */
    public synchronized void shutdown() {
        close();
    }

    /**
     * 关闭 HikariCP 连接池，线程安全。
     * 关闭失败时静默忽略，确保状态重置。
     */
    public synchronized void close() {
        if (dataSource != null) {
            try {
                dataSource.unwrap(com.zaxxer.hikari.HikariDataSource.class).close();
            } catch (SQLException ignored) {
            }
            dataSource = null;
            poolClosed = true;
        }
    }

    /**
     * 重置单例（仅用于测试清理）。
     */
    public static synchronized void reset() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }
}
