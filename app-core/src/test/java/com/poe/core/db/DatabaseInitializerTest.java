package com.poe.core.db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DatabaseInitializer 测试。
 */
class DatabaseInitializerTest {

    @TempDir
    Path tempDir;

    @Test
    void firstRunWithoutSeedDb_noPlaceholderCreated() throws Exception {
        // seed.db 不在测试 classpath 中，复制失败后不应创建占位文件
        Path appDir = tempDir.resolve("app-home");
        DatabaseInitializer initializer = new DatabaseInitializer(appDir);

        Path result = initializer.initialize();

        // 不创建占位文件：DatabaseManager 后续会自动创建有效空库
        assertFalse(Files.exists(result),
                "Should NOT create placeholder file — DatabaseManager will create valid empty DB");
    }

    @Test
    void alreadyExists_withSeedMarker_skipsCopy() throws Exception {
        Path appDir = tempDir.resolve("app-home2");
        Path dataDir = appDir.resolve("data");
        Path targetDb = dataDir.resolve("poe.db");
        Path seedMarker = dataDir.resolve(".seed_ok");

        Files.createDirectories(dataDir);

        // 放入一个有效的 SQLite 数据库模拟已存在的库
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + targetDb);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE existing (col TEXT)");
            stmt.execute("INSERT INTO existing VALUES ('persist')");
        }

        // 创建 seed marker 表示种子已成功复制
        Files.createFile(seedMarker);

        long originalSize = Files.size(targetDb);

        // 再次运行 initialize，不应覆盖
        DatabaseInitializer initializer = new DatabaseInitializer(appDir);
        initializer.initialize();

        assertEquals(originalSize, Files.size(targetDb),
                "Existing seeded db should not be overwritten");

        // 验证内容完整
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + targetDb);
             Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery("SELECT col FROM existing");
            assertTrue(rs.next());
            assertEquals("persist", rs.getString("col"));
        }
    }

    @Test
    void dbExists_withoutSeedMarker_retriesCopy() throws Exception {
        // 模拟上次 seed.db 复制失败、DatabaseManager 创建了空库的场景
        // 没有 .seed_ok marker，应该重试复制（但测试 classpath 无 seed.db，所以仍失败）
        Path appDir = tempDir.resolve("app-home3");
        Path dataDir = appDir.resolve("data");
        Path targetDb = dataDir.resolve("poe.db");

        Files.createDirectories(dataDir);

        // 创建一个空的有效 SQLite 库（模拟 DatabaseManager 的降级行为）
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + targetDb);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE fallback (col TEXT)");
        }

        // 没有 .seed_ok marker → 应尝试重试复制
        Path seedMarker = dataDir.resolve(".seed_ok");
        assertFalse(Files.exists(seedMarker));

        DatabaseInitializer initializer = new DatabaseInitializer(appDir);
        initializer.initialize();

        // seed.db 不在 classpath，复制仍失败，但 marker 不会被创建
        assertFalse(Files.exists(seedMarker),
                "Seed marker should not be created when copy fails");
    }

    @Test
    void initialize_returnsCorrectPath() {
        Path appDir = tempDir.resolve("app-home4");
        DatabaseInitializer initializer = new DatabaseInitializer(appDir);

        Path result = initializer.initialize();
        assertEquals(appDir.resolve("data").resolve("poe.db"), result);
    }

    @Test
    void initialize_isIdempotent() {
        Path appDir = tempDir.resolve("app-home5");
        DatabaseInitializer initializer = new DatabaseInitializer(appDir);

        Path first = initializer.initialize();
        Path second = initializer.initialize();

        assertEquals(first, second);
    }
}
