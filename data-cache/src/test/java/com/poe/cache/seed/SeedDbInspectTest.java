package com.poe.cache.seed;

import com.poe.cache.manager.DatabaseManager;
import org.junit.jupiter.api.*;

import java.nio.file.*;
import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 检测 seed.db 完整性：委托 {@link SeedDbInspector} 统计行数并检测空表。
 *
 * <p>该测试不依赖网络请求，仅读取本地 seed.db 文件。
 * 当发现核心表为空时测试失败，提示需要重建或修复 seed.db。
 */
class SeedDbInspectTest {

    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        Path seedPath = Paths.get("").toAbsolutePath()
            .resolve("../app-ui/src/main/resources/seed.db").normalize();
        assertTrue(Files.exists(seedPath), "seed.db not found at: " + seedPath);

        DatabaseManager.testMode = true;
        DatabaseManager.testDbPath = seedPath.toString();
        DatabaseManager.reset();
        conn = DatabaseManager.getInstance().getConnection();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
        DatabaseManager.getInstance().shutdown();
        DatabaseManager.testMode = false;
        DatabaseManager.testDbPath = null;
        DatabaseManager.reset();
    }

    @Test
    @DisplayName("验证 seed.db 核心表不为空")
    void coreTablesNotBeEmpty() throws Exception {
        List<String> problems = SeedDbInspector.findEmptyCoreTables(
            conn, SeedDbInspector.DEFAULT_CORE_TABLES);

        System.out.println(SeedDbInspector.buildReport(conn));

        assertTrue(problems.isEmpty(),
            "核心表不应为空，请检查 seed.db 或运行 SeedDbBuilder 重建: " + problems);
    }

    @Test
    @DisplayName("列出所有空表及其 data_version 状态")
    void listEmptyTables() throws Exception {
        System.out.println("=== 数据为空的表 + data_version 状态 ===");

        List<String> empty = SeedDbInspector.findEmptyTables(conn);
        if (empty.isEmpty()) {
            System.out.println("  (所有表均包含数据)");
        } else {
            for (String table : empty) {
                String dv = SeedDbInspector.getDataVersion(conn, table);
                System.out.printf("  %-45s data_version=%s%n", table,
                    dv != null ? dv : "(无记录)");
            }
        }
    }

    @Test
    @DisplayName("输出所有表行数统计")
    void printAllTableCounts() throws Exception {
        System.out.println("=== 所有表行数 ===");

        for (var entry : SeedDbInspector.sortedByRows(conn)) {
            String table = entry.getKey();
            int rows = entry.getValue();
            String dv = SeedDbInspector.getDataVersion(conn, table);
            System.out.printf("  %-45s %,8d rows  dv=%s%n", table, rows,
                dv != null ? dv : "-");
        }
    }
}
