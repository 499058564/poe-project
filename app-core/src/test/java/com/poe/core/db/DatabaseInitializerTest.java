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
    void firstRunWithoutSeedDb_createsEmptyDb() throws Exception {
        Path appDir = tempDir.resolve("app-home");
        DatabaseInitializer initializer = new DatabaseInitializer(appDir);

        Path result = initializer.initialize();

        // 即使 seed.db 不在 classpath，也应创建空文件让应用继续运行
        assertTrue(Files.exists(result), "Should create db file even without seed resource");
    }

    @Test
    void alreadyExists_skipsCopy() throws Exception {
        Path appDir = tempDir.resolve("app-home2");
        Path dataDir = appDir.resolve("data");
        Path targetDb = dataDir.resolve("poe.db");

        Files.createDirectories(dataDir);

        // 放入一个有效的 SQLite 数据库模拟已存在的库
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + targetDb);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE existing (col TEXT)");
            stmt.execute("INSERT INTO existing VALUES ('persist')");
        }

        long originalSize = Files.size(targetDb);

        // 再次运行 initialize，不应覆盖
        DatabaseInitializer initializer = new DatabaseInitializer(appDir);
        initializer.initialize();

        assertEquals(originalSize, Files.size(targetDb),
                "Existing db should not be overwritten");

        // 验证内容完整
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + targetDb);
             Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery("SELECT col FROM existing");
            assertTrue(rs.next());
            assertEquals("persist", rs.getString("col"));
        }
    }

    @Test
    void initialize_returnsCorrectPath() {
        Path appDir = tempDir.resolve("app-home3");
        DatabaseInitializer initializer = new DatabaseInitializer(appDir);

        Path result = initializer.initialize();
        assertEquals(appDir.resolve("data").resolve("poe.db"), result);
    }

    @Test
    void initialize_isIdempotent() {
        Path appDir = tempDir.resolve("app-home4");
        DatabaseInitializer initializer = new DatabaseInitializer(appDir);

        Path first = initializer.initialize();
        Path second = initializer.initialize();

        assertEquals(first, second);
    }
}
