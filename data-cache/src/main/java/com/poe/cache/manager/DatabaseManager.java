package com.poe.cache.manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * SQLite 数据库管理器（线程安全单例）。
 * <p>
 * 负责连接生命周期管理和迁移脚本执行。
 * 数据库文件位于 {@code ~/.poe-tool/data/poe.db}。
 * <p>
 * 通过 {@link #testMode} / {@link #testDbPath} 支持测试时注入内存数据库。
 * 通过 {@link #DatabaseManager(String)} 构造函数支持自定义路径（如 SeedDbBuilder）。
 */
public class DatabaseManager {

    static final String DB_PATH =
        System.getProperty("user.home") + "/.poe-tool/data/poe.db";

    private static DatabaseManager instance;
    private Connection connection;
    private final String dbPath;

    // 测试模式：设为 true 后 getConnection() 使用 testDbPath 而非默认磁盘路径
    public static volatile boolean testMode = false;
    public static String testDbPath = null;

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
     * 获取数据库连接，首次调用自动执行迁移脚本初始化表结构。
     * <p>调用方应确保数据库文件已就绪（通过 DatabaseInitializer）。
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = getDbUrl();
            connection = DriverManager.getConnection(url);
            init();
        }
        return connection;
    }

    private String getDbUrl() {
        if (testMode && testDbPath != null) {
            return "jdbc:sqlite:" + testDbPath;
        }
        return "jdbc:sqlite:" + dbPath;
    }

    /**
     * 通过 {@link MigrationManager} 执行版本化迁移。
     * 仅执行未运行过的脚本，支持幂等重复调用。
     */
    public void init() {
        try {
            new MigrationManager(connection).migrate();
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to run migrations", e);
        }
    }

    /**
     * 强制初始化：创建连接 + 运行迁移（用于 SeedDbBuilder 首次构建新库）。
     */
    public void forceInit() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        String url = getDbUrl();
        connection = DriverManager.getConnection(url);
        init();
    }

    /**
     * 关闭数据库连接并重置状态，线程安全。
     */
    public synchronized void shutdown() {
        close();
    }

    /**
     * 关闭数据库连接，线程安全。
     * 关闭失败时静默忽略，确保状态重置。
     */
    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // ignore
            } finally {
                connection = null;
            }
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
