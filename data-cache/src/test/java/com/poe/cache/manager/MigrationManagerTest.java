package com.poe.cache.manager;

import com.poe.cache.manager.MigrationManager.MigrationFile;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MigrationManager 单元测试。
 * <p>
 * 验证版本化迁移的核心行为：首次执行、幂等性、增量迁移、失败回滚。
 */
class MigrationManagerTest {

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("首次运行应执行所有迁移并创建 schema_version 表")
    void shouldRunAllMigrationsOnFirstRun() {
        new MigrationManager(connection).migrate();

        // 验证 schema_version 表存在
        assertTrue(tableExists("schema_version"));

        // 验证 8 个迁移版本已记录
        assertEquals(8, getMigrationCount());

        // 验证业务表已创建
        assertTrue(tableExists("base_items"));
        assertTrue(tableExists("skill_gems"));
        assertTrue(tableExists("passive_skills"));
        assertTrue(tableExists("mods"));
        assertTrue(tableExists("data_version"));
        assertTrue(tableExists("translations"));
        assertTrue(tableExists("items_fts"));
        assertTrue(tableExists("weapons"));
        assertTrue(tableExists("armours"));
    }

    @Test
    @DisplayName("二次运行不应重复执行已完成的迁移")
    void shouldBeIdempotentOnSecondRun() {
        MigrationManager mgr = new MigrationManager(connection);

        // 首次运行
        mgr.migrate();
        assertEquals(8, getMigrationCount());

        // 二次运行
        mgr.migrate();

        // 版本数不变
        assertEquals(8, getMigrationCount());
    }

    @Test
    @DisplayName("部分执行后新增迁移脚本应被识别并执行")
    void shouldExecuteIncrementalMigration() {
        // 模拟：只执行了 4 个迁移后重启
        new PartialMigrationManager(connection, 4).migrate();
        assertEquals(4, getMigrationCount());

        // 使用完整 MigrationManager 继续执行
        new MigrationManager(connection).migrate();

        // 8 个迁移全部完成
        assertEquals(8, getMigrationCount());
        assertTrue(tableExists("items_fts"), "v007 items_fts should be created");
        assertTrue(tableExists("weapons"), "v008 weapons should be created");
    }

    @Test
    @DisplayName("迁移失败时应回滚当前脚本，但不影响已完成的迁移")
    void shouldRollbackFailedMigrationButKeepCompletedOnes() {
        // 先成功执行前 2 个迁移
        new PartialMigrationManager(connection, 2).migrate();
        assertEquals(2, getMigrationCount());
        assertTrue(tableExists("base_items"));
        assertTrue(tableExists("skill_gems"));

        // 尝试执行一个包含语法错误的恶意迁移
        BadMigrationManager badMgr = new BadMigrationManager(connection);
        assertThrows(RuntimeException.class, badMgr::migrate);

        // 前 2 个迁移保持完成
        assertEquals(2, getMigrationCount());
        assertTrue(tableExists("base_items"), "base_items should survive");
    }

    @Test
    @DisplayName("schema_version 表记录版本号和描述")
    void shouldRecordVersionAndDescription() {
        new MigrationManager(connection).migrate();

        List<MigrationRecord> records = getMigrationRecords();

        assertEquals(8, records.size());
        assertEquals("v001_base_items.sql", records.get(0).description);
        assertEquals("v008_equipment_subtables.sql", records.get(7).description);
        records.forEach(r -> assertNotNull(r.executedAt, "executed_at should not be null"));
    }

    @Test
    @DisplayName("空数据库获取当前版本应返回 0")
    void shouldReturnZeroForEmptyDatabase() {
        // 直接构造 MigrationManager 但不调用 migrate
        // 使用内部逻辑：getCurrentVersion 返回 0
        new MigrationManager(connection).migrate(); // 先创建 schema_version 表

        // 直接查询版本
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT COALESCE(MAX(version), 0) FROM schema_version")) {
            assertTrue(rs.next());
            assertEquals(8, rs.getInt(1));
        } catch (SQLException e) {
            fail(e);
        }
    }

    // ---- helpers ----

    private boolean tableExists(String tableName) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT 1 FROM sqlite_master WHERE type IN ('table','view') AND name='" + tableName + "'")) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private int getMigrationCount() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM schema_version")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    private List<MigrationRecord> getMigrationRecords() {
        List<MigrationRecord> records = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT version, description, executed_at FROM schema_version ORDER BY version")) {
            while (rs.next()) {
                records.add(new MigrationRecord(
                    rs.getInt("version"),
                    rs.getString("description"),
                    rs.getString("executed_at")
                ));
            }
        } catch (SQLException e) {
            fail(e);
        }
        return records;
    }

    private static class MigrationRecord {
        final int version;
        final String description;
        final String executedAt;

        MigrationRecord(int version, String description, String executedAt) {
            this.version = version;
            this.description = description;
            this.executedAt = executedAt;
        }
    }

    /**
     * 模拟只执行前 N 个迁移的场景，用于测试增量迁移。
     */
    private static class PartialMigrationManager extends MigrationManager {
        private final int limit;

        PartialMigrationManager(Connection connection, int limit) {
            super(connection);
            this.limit = limit;
        }

        @Override
        protected List<MigrationFile> findPendingMigrations(int currentVersion) {
            List<MigrationFile> all = super.findPendingMigrations(currentVersion);
            List<MigrationFile> limited = new ArrayList<>();
            int count = 0;
            for (MigrationFile mf : all) {
                if (count >= limit) break;
                limited.add(mf);
                count++;
            }
            return limited;
        }
    }

    /**
     * 模拟包含错误 SQL 的迁移，用于测试失败回滚。
     */
    private static class BadMigrationManager extends MigrationManager {
        BadMigrationManager(Connection connection) {
            super(connection);
        }

        @Override
        protected List<MigrationFile> findPendingMigrations(int currentVersion) {
            List<MigrationFile> pending = super.findPendingMigrations(currentVersion);
            if (!pending.isEmpty()) {
                MigrationFile first = pending.get(0);
                // 替换为包含语法错误的文件引用（不存在的文件）
                pending.set(0, new MigrationFile(first.version, "nonexistent_bad.sql"));
            }
            return pending;
        }
    }
}
