package com.poe.cache;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * SQLite 数据库管理器（线程安全单例）。
 * <p>
 * 负责连接生命周期管理和迁移脚本执行。
 * 数据库文件位于 {@code ~/.poe-tool/data/poe.db}。
 * <p>
 * 通过 {@link #testMode} / {@link #testDbPath} 支持测试时注入内存数据库。
 */
public class DatabaseManager {

    private static final String DB_PATH =
        System.getProperty("user.home") + "/.poe-tool/data/poe.db";

    private static DatabaseManager instance;
    private Connection connection;

    // 测试模式：设为 true 后 getConnection() 使用 testDbPath 而非默认磁盘路径
    static volatile boolean testMode = false;
    static String testDbPath = null;

    private DatabaseManager() {}

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
        return "jdbc:sqlite:" + DB_PATH;
    }

    /**
     * 按编号顺序执行所有迁移 SQL 文件。
     * 每个文件的 CREATE 语句均包含 {@code IF NOT EXISTS}，支持幂等执行。
     */
    public void init() {
        try {
            String[] migrationFiles = {
                "001_base_items.sql",
                "002_skill_gems.sql",
                "003_passive_skills.sql",
                "004_mods.sql",
                "005_data_version.sql",
                "006_translations.sql",
                "007_items_fts.sql",
            };

            for (String file : migrationFiles) {
                String sql = loadResource("migration/" + file);
                try (Statement stmt = connection.createStatement()) {
                    stmt.executeUpdate(sql);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to run migrations", e);
        }
    }

    private String loadResource(String path) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("Migration file not found: " + path);
            }
            try (BufferedReader reader = new BufferedReader(
                     new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        }
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
    static synchronized void reset() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }
}
