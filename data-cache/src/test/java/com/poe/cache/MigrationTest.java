package com.poe.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证迁移 SQL 脚本在 SQLite 中可正确执行。
 * 按编号顺序执行每个表文件（001~007），确保独立文件可分别执行。
 */
class MigrationTest {

    private static final String[] MIGRATION_FILES = {
        "v001_base_items.sql",
        "v002_skill_gems.sql",
        "v003_passive_skills.sql",
        "v004_mods.sql",
        "v005_data_version.sql",
        "v006_translations.sql",
        "v007_items_fts.sql",
    };

    /** 加载并执行所有迁移文件 */
    private void runMigrations(Connection conn) throws Exception {
        for (String file : MIGRATION_FILES) {
            String sql = loadResource("migration/" + file);
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
            }
        }
    }

    @Test
    @DisplayName("执行迁移脚本并验证所有表、索引、FTS")
    void shouldCreateAllTables() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            conn.setAutoCommit(false);
            runMigrations(conn);
            conn.commit();

            // 验证常规表
            List<String> tables = listTables(conn);
            assertTrue(tables.contains("base_items"), "base_items table missing");
            assertTrue(tables.contains("skill_gems"), "skill_gems table missing");
            assertTrue(tables.contains("passive_skills"), "passive_skills table missing");
            assertTrue(tables.contains("mods"), "mods table missing");
            assertTrue(tables.contains("data_version"), "data_version table missing");
            assertTrue(tables.contains("translations"), "translations table missing");

            // 验证 FTS 虚拟表
            assertTrue(tables.contains("items_fts"), "items_fts FTS table missing");

            // 验证索引
            List<String> indices = listIndices(conn);
            assertTrue(indices.contains("idx_base_items_name"), "idx_base_items_name missing");
            assertTrue(indices.contains("idx_base_items_class"), "idx_base_items_class missing");
            assertTrue(indices.contains("idx_skill_gems_name"), "idx_skill_gems_name missing");
        }
    }

    @Test
    @DisplayName("base_items 表 INSERT + SELECT")
    void shouldInsertAndSelectBaseItem() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            runMigrations(conn);

            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO base_items (id, name, name_zh, class, drop_level, version) " +
                "VALUES (?, ?, ?, ?, ?, ?)"
            )) {
                ps.setInt(1, 1);
                ps.setString(2, "Iron Hat");
                ps.setString(3, "铁盔");
                ps.setString(4, "Helmet");
                ps.setInt(5, 5);
                ps.setString(6, "3.23");
                assertEquals(1, ps.executeUpdate());
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT name, name_zh, class FROM base_items WHERE id = 1"
                 )) {
                assertTrue(rs.next());
                assertEquals("Iron Hat", rs.getString("name"));
                assertEquals("铁盔", rs.getString("name_zh"));
                assertEquals("Helmet", rs.getString("class"));
            }
        }
    }

    @Test
    @DisplayName("FTS 外部内容表搜索")
    void shouldSearchWithFts() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            runMigrations(conn);
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "INSERT INTO base_items (id, name, name_zh, class, version) " +
                    "VALUES (1, 'Iron Hat', '铁盔', 'Helmet', '3.23')"
                );
                // 手动同步 FTS（外部内容表需要手动重建索引）
                stmt.executeUpdate(
                    "INSERT INTO items_fts(items_fts) VALUES ('rebuild')"
                );
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT name, name_zh FROM items_fts WHERE items_fts MATCH 'Iron'"
                 )) {
                assertTrue(rs.next());
                assertEquals("Iron Hat", rs.getString("name"));
            }
        }
    }

    @Test
    @DisplayName("translations 表联合主键唯一约束")
    void shouldEnforceTranslationUniqueConstraint() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            runMigrations(conn);
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "INSERT INTO translations VALUES ('Iron Hat', '铁盔', 'item')"
                );
            }

            // 重复插入应失败
            assertThrows(SQLException.class, () -> {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(
                        "INSERT INTO translations VALUES ('Iron Hat', '另一个', 'item')"
                    );
                }
            });
        }
    }

    @Test
    @DisplayName("data_version 表记录同步状态")
    void shouldTrackSyncVersion() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            runMigrations(conn);
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "INSERT INTO data_version VALUES ('base_items', '2024-01-01', 50, '3.23')"
                );
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT record_count FROM data_version WHERE table_name = 'base_items'"
                 )) {
                assertTrue(rs.next());
                assertEquals(50, rs.getInt("record_count"));
            }
        }
    }

    // ---- helpers ----

    private String loadResource(String path) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(is, "Resource not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private List<String> listTables(Connection conn) throws SQLException {
        List<String> names = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT name FROM sqlite_master WHERE type='table' OR type='view' ORDER BY name"
             )) {
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        }
        return names;
    }

    private List<String> listIndices(Connection conn) throws SQLException {
        List<String> names = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT name FROM sqlite_master WHERE type='index' ORDER BY name"
             )) {
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        }
        return names;
    }
}
