package com.poe.core.version;

import com.poe.cache.manager.DatabaseManager;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataCoordinator 单元测试。
 * 使用 in-memory SQLite 验证同步决策逻辑。
 */
class DataCoordinatorTest {

    private DataCoordinator coordinator;

    @BeforeAll
    static void setUpMode() {
        DatabaseManager.testMode = true;
    }

    @BeforeEach
    void setUp() {
        DatabaseManager.reset();
        DatabaseManager.testDbPath = ":memory:";
        coordinator = new DataCoordinator(DatabaseManager.getInstance());
    }

    @AfterAll
    static void tearDownMode() {
        DatabaseManager.reset();
        DatabaseManager.testMode = false;
        DatabaseManager.testDbPath = null;
    }

    // ==================== 表分类 ====================

    @Test
    @DisplayName("base_items 应被识别为 Wiki 表")
    void shouldIdentifyWikiTable() {
        assertTrue(coordinator.isWikiTable("base_items"));
    }

    @Test
    @DisplayName("skill_gems 应被识别为 Wiki 表")
    void shouldIdentifySkillGemsAsWiki() {
        assertTrue(coordinator.isWikiTable("skill_gems"));
    }

    @Test
    @DisplayName("mods 应被识别为 Wiki 表")
    void shouldIdentifyModsAsWiki() {
        assertTrue(coordinator.isWikiTable("mods"));
    }

    @Test
    @DisplayName("currency_prices 应被识别为 poe.ninja 表")
    void shouldIdentifyNinjaTable() {
        assertTrue(coordinator.isNinjaTable("currency_prices"));
    }

    @Test
    @DisplayName("item_prices 应被识别为 poe.ninja 表")
    void shouldIdentifyItemPricesAsNinja() {
        assertTrue(coordinator.isNinjaTable("item_prices"));
    }

    @Test
    @DisplayName("未知表既不是 Wiki 也不是 Ninja")
    void shouldNotIdentifyUnknownTable() {
        assertFalse(coordinator.isWikiTable("unknown_table"));
        assertFalse(coordinator.isNinjaTable("unknown_table"));
    }

    // ==================== decide() ====================

    @Test
    @DisplayName("Wiki 表应返回 source=wiki")
    void shouldReturnWikiSourceForWikiTable() {
        GameVersion version = new GameVersion("3.25", "Settlers", "GGG API", null);
        DataCoordinator.SyncDecision decision = coordinator.decide("base_items", version);

        assertEquals("base_items", decision.getTableName());
        assertEquals("wiki", decision.getSource());
        assertEquals("3.25", decision.getGameVersion().getVersion());
    }

    @Test
    @DisplayName("Ninja 表应返回 source=poe.ninja")
    void shouldReturnNinjaSourceForNinjaTable() {
        GameVersion version = new GameVersion("3.25", "Settlers", "GGG API", null);
        DataCoordinator.SyncDecision decision = coordinator.decide("currency_prices", version);

        assertEquals("poe.ninja", decision.getSource());
    }

    @Test
    @DisplayName("未知表应返回 source=unknown")
    void shouldReturnUnknownSourceForUnknownTable() {
        GameVersion version = new GameVersion("3.25", "Settlers", "GGG API", null);
        DataCoordinator.SyncDecision decision = coordinator.decide("some_table", version);

        assertEquals("unknown", decision.getSource());
    }

    // ==================== shouldSync() ====================

    @Test
    @DisplayName("unknown 版本时应返回需要同步")
    void shouldNeedSyncForUnknownVersion() {
        GameVersion unknown = GameVersion.unknown();
        assertTrue(coordinator.shouldSync("base_items", "wiki", unknown));
    }

    @Test
    @DisplayName("无缓存记录时应返回需要同步")
    void shouldNeedSyncWhenNoCachedRecord() {
        GameVersion version = new GameVersion("3.25", "Settlers", "GGG API", null);
        assertTrue(coordinator.shouldSync("base_items", "wiki", version));
    }

    @Test
    @DisplayName("版本一致时应返回不需要同步")
    void shouldNotNeedSyncWhenVersionMatches() {
        GameVersion version = new GameVersion("3.25", "Settlers", "GGG API", null);
        coordinator.recordSync("base_items", "wiki", "3.25", 1500);

        assertFalse(coordinator.shouldSync("base_items", "wiki", version));
    }

    @Test
    @DisplayName("版本不一致时应返回需要同步")
    void shouldNeedSyncWhenVersionDiffers() {
        GameVersion version = new GameVersion("3.25", "Settlers", "GGG API", null);
        coordinator.recordSync("base_items", "wiki", "3.24", 1000);

        assertTrue(coordinator.shouldSync("base_items", "wiki", version));
    }

    // ==================== recordSync() ====================

    @Test
    @DisplayName("recordSync 应能成功记录和更新")
    void shouldRecordAndUpdateSync() {
        coordinator.recordSync("base_items", "wiki", "3.25", 1500);

        coordinator.recordSync("base_items", "wiki", "3.25", 2000);

        assertDoesNotThrow(() ->
            coordinator.recordSync("base_items", "wiki", "3.25", 2000)
        );
    }
}
