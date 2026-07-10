package com.poe.cache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 版本化数据库迁移管理器。
 * <p>
 * 通过 {@code schema_version} 表跟踪已执行的迁移版本，
 * 仅执行未运行过的脚本，保证迁移幂等性。
 * <p>
 * 迁移脚本命名规则：{@code migration/v{数字版本号}_{描述}.sql}，
 * 如 {@code v001_base_items.sql}、{@code v002_skill_gems.sql}。
 * <p>
 * 每个迁移脚本在独立事务中执行，失败时自动回滚，
 * 已成功执行的迁移不受影响。
 */
public class MigrationManager {

    private static final String MIGRATION_DIR = "migration/";
    private static final Pattern VERSION_PATTERN = Pattern.compile("^v(\\d+)_.+\\.sql$");

    private static final String VERSION_TABLE_SQL =
        "CREATE TABLE IF NOT EXISTS schema_version (" +
        "    version     INTEGER PRIMARY KEY," +
        "    description TEXT," +
        "    executed_at TEXT DEFAULT (datetime('now'))" +
        ")";

    private final Connection connection;

    public MigrationManager(Connection connection) {
        this.connection = connection;
    }

    /**
     * 执行所有未运行的迁移脚本。
     * <p>
     * 首次调用时创建 {@code schema_version} 表，然后按版本号升序
     * 依次执行每个未迁移的脚本。每个脚本在独立事务中运行。
     *
     * @throws RuntimeException 任一迁移失败时抛出，已成功的迁移不回滚
     */
    public void migrate() {
        ensureVersionTable();
        int currentVersion = getCurrentVersion();

        List<MigrationFile> pending = findPendingMigrations(currentVersion);
        for (MigrationFile mf : pending) {
            executeMigration(mf);
        }
    }

    // ---- internal ----

    private void ensureVersionTable() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(VERSION_TABLE_SQL);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create schema_version table", e);
        }
    }

    private int getCurrentVersion() {
        String sql = "SELECT COALESCE(MAX(version), 0) FROM schema_version";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query current schema version", e);
        }
        return 0;
    }

    /**
     * 从 classpath 扫描 migration/ 目录，过滤出未执行的脚本并按版本排序。
     * <p>
     * 支持文件系统目录和 JAR 内资源两种 classpath 形式。
     */
    protected List<MigrationFile> findPendingMigrations(int currentVersion) {
        List<MigrationFile> all = new ArrayList<>();

        try {
            Enumeration<URL> resources = getClass().getClassLoader()
                .getResources(MIGRATION_DIR);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();

                if ("file".equals(url.getProtocol())) {
                    // 文件系统目录
                    java.io.File dir = new java.io.File(url.toURI());
                    java.io.File[] files = dir.listFiles();
                    if (files != null) {
                        for (java.io.File f : files) {
                            String name = f.getName();
                            Matcher m = VERSION_PATTERN.matcher(name);
                            if (m.matches()) {
                                int version = Integer.parseInt(m.group(1));
                                all.add(new MigrationFile(version, name));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to discover migration files", e);
        }

        // 按版本排序，过滤已执行的
        all.sort(Comparator.comparingInt(m -> m.version));

        List<MigrationFile> pending = new ArrayList<>();
        for (MigrationFile mf : all) {
            if (mf.version > currentVersion) {
                pending.add(mf);
            }
        }
        return pending;
    }

    /**
     * 在事务中执行单个迁移脚本：
     * <ol>
     *   <li>读取 SQL 文件内容</li>
     *   <li>开始事务</li>
     *   <li>执行 SQL</li>
     *   <li>记录到 schema_version</li>
     *   <li>提交事务</li>
     * </ol>
     * 任何步骤失败则回滚事务并抛出异常。
     */
    private void executeMigration(MigrationFile mf) {
        String resourcePath = MIGRATION_DIR + mf.fileName;
        String sql = loadResource(resourcePath);

        try {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(sql);

                // 记录版本
                try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO schema_version (version, description) VALUES (?, ?)"
                )) {
                    ps.setInt(1, mf.version);
                    ps.setString(2, mf.fileName);
                    ps.executeUpdate();
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException(
                    "Migration v" + mf.version + " (" + mf.fileName + ") failed and was rolled back", e);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute migration: " + mf.fileName, e);
        }
    }

    private String loadResource(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("Migration file not found: " + path);
            }
            try (BufferedReader reader = new BufferedReader(
                     new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read migration file: " + path, e);
        }
    }

    /**
     * 内部表示一个迁移文件的版本和文件名。
     */
    protected static class MigrationFile {
        final int version;
        final String fileName;

        MigrationFile(int version, String fileName) {
            this.version = version;
            this.fileName = fileName;
        }
    }
}
