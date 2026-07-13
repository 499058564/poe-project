package com.poe.provider.sync;

import com.google.common.eventbus.Subscribe;
import com.poe.cache.manager.DatabaseManager;
import com.poe.core.event.*;
import com.poe.provider.WikiApiClient;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.*;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataSyncService 集成测试。
 * <p>
 * 使用 MockWebServer 模拟 Wiki API，内存 SQLite 模拟本地数据库，
 * 验证完整同步流程。
 */
class DataSyncServiceTest {

    private MockWebServer mockServer;
    private DataSyncService syncService;
    private final List<Object> receivedEvents = new ArrayList<>();

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        DatabaseManager.reset();

        // 使用 MockWebServer 地址的 WikiApiClient
        OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(5))
            .build();
        WikiApiClient wikiClient = new WikiApiClient(httpClient,
            com.google.common.util.concurrent.RateLimiter.create(1000.0),
            mockServer.url("/w/api.php").toString());

        syncService = new DataSyncService(wikiClient, DatabaseManager.getInstance());

        // 测试环境关闭表间延迟
        syncService.setInterTableDelayMs(0);
        syncService.setFailureDelayMs(0);
        syncService.setRetryCooldownMs(0);

        // 注册事件收集器
        receivedEvents.clear();
        AppEventBus.register(this);
    }

    @AfterEach
    void tearDown() throws IOException {
        AppEventBus.unregister(this);
        DatabaseManager.reset();
        mockServer.shutdown();
    }

    @Subscribe
    void onDataSyncStart(DataSyncStartEvent e) { receivedEvents.add(e); }

    @Subscribe
    void onDataSyncProgress(DataSyncProgressEvent e) { receivedEvents.add(e); }

    @Subscribe
    void onDataSyncComplete(DataSyncCompleteEvent e) { receivedEvents.add(e); }

    // ==================== 同步测试 ====================

    @Test
    @DisplayName("全量同步 91 张表，4 张核心表数据应正确写入 SQLite，其余跳过")
    void shouldSyncAllTablesAndInsertData() throws Exception {
        // 为 4 张核心表设置 mock 响应
        enqueueCargoResponse("items", 2, itemJson());
        enqueueCargoResponse("skill_gems", 1, skillGemJson());
        enqueueCargoResponse("passive_skills", 1, passiveSkillJson());
        enqueueCargoResponse("mods", 1, modJson());
        // 其余 87 张表 count=0，自动跳过
        enqueueEquipmentSubtableCountResponses();

        Map<String, SyncResult> results = syncService.syncAll();

        assertEquals(91, results.size());
        // 核心表不应跳过
        SyncResult itemsResult = results.get("items");
        assertFalse(itemsResult.isSkipped(), "items should not be skipped");
        assertTrue(itemsResult.getSyncedRecords() > 0);
        // skill_gems, passive_skills, mods 同理
        assertFalse(results.get("skill_gems").isSkipped());
        assertFalse(results.get("passive_skills").isSkipped());
        assertFalse(results.get("mods").isSkipped());

        // 验证数据写入
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            try (Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM base_items")) {
                assertTrue(rs.next());
                assertEquals(2, rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM skill_gems")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM passive_skills")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM mods")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }
            }

        // 验证 data_version 已更新
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM data_version")) {
            assertTrue(rs.next());
            assertEquals(4, rs.getInt(1));
        }
        }
    }

    @Test
    @DisplayName("远程无变化时应跳过同步")
    void shouldSkipWhenNoChanges() throws Exception {
        // 先同步一次写入数据
        enqueueCargoResponse("items", 2, itemJson());
        syncService.syncTable("items");

        // 二次同步 — 远程 count 与本地一致
        enqueueCargoResponse("items", 2, itemJson());
        SyncResult result = syncService.syncTable("items");

        assertTrue(result.isSkipped(),
            "Should skip when local count == remote count");
    }

    @Test
    @DisplayName("同步过程中应发送进度事件")
    void shouldSendProgressEvents() throws Exception {
        receivedEvents.clear();

        // 设置 600 条数据（items batchSize=100，会分 6 批）
        enqueueCountResponse("items", 600);
        // 6 批各 100 条
        for (int i = 0; i < 6; i++) {
            enqueueDataResponse(itemJsonBatch(i * 100, 100));
        }

        syncService.syncTable("items");

        // 检查事件类型
        long startCount = receivedEvents.stream()
            .filter(e -> e instanceof DataSyncStartEvent).count();
        long completeCount = receivedEvents.stream()
            .filter(e -> e instanceof DataSyncCompleteEvent).count();
        long progressCount = receivedEvents.stream()
            .filter(e -> e instanceof DataSyncProgressEvent).count();

        assertEquals(1, startCount, "Should have 1 start event");
        assertEquals(1, completeCount, "Should have 1 complete event");
        assertTrue(progressCount >= 6, "Should have at least 6 progress events (6 batches)");
    }

    // ==================== hasUpdates 测试 ====================

    @Test
    @DisplayName("hasUpdates 应在远程数据变化时返回 true")
    void shouldDetectRemoteUpdates() throws Exception {
        // items: 本地 0 条，远程 10 条 → 有更新
        enqueueAllCountResponses(Map.of("items", 10));
        assertTrue(syncService.hasUpdates());
    }

    @Test
    @DisplayName("hasUpdates 应在数据一致时返回 false")
    void shouldReturnFalseWhenNoUpdates() throws Exception {
        // 先同步 items 表
        enqueueCargoResponse("items", 5, itemJsonBatch(0, 5));
        syncService.syncTable("items");

        // 再检查 — 所有表 count=0（与本地一致）
        enqueueAllCountResponses(Map.of());
        assertFalse(syncService.hasUpdates());
    }

    // ==================== 表验证 ====================

    @Test
    @DisplayName("配置表数量应为 91 张表（4 核心 + 11 装备 + 15 词缀/工艺/经济 + 11 技能/天赋/职业 + 8 怪物/区域/异界 + 27 联盟机制 + 15 杂项）")
    void shouldHaveFifteenTables() throws Exception {
        // 所有表 count=0，快速跳过
        enqueueAllCountResponses(Map.of());
        Map<String, SyncResult> results = syncService.syncAll();
        assertEquals(91, results.size());
        assertTrue(results.containsKey("items"));
        assertTrue(results.containsKey("skill_gems"));
        assertTrue(results.containsKey("passive_skills"));
        assertTrue(results.containsKey("mods"));
        assertTrue(results.containsKey("weapons"));
        assertTrue(results.containsKey("divination_cards"));
        assertTrue(results.containsKey("versions"));
    }

    // ==================== 断点续传 ====================

    @Test
    @DisplayName("syncTable 错误时不应影响其他表")
    void shouldNotAffectOtherTablesOnError() throws Exception {
        // items 同步失败（返回错误 JSON）
        mockServer.enqueue(new MockResponse()
            .setBody("{\"error\":{\"code\":\"server-error\",\"info\":\"Server error\"}}")
            .addHeader("Content-Type", "application/json"));

        // skill_gems 正常同步
        enqueueCargoResponse("skill_gems", 1, skillGemJson());
        enqueueCargoResponse("passive_skills", 1, passiveSkillJson());
        enqueueCargoResponse("mods", 1, modJson());
        // 其余 72 张表 count=0
        enqueueEquipmentSubtableCountResponses();

        Map<String, SyncResult> results = syncService.syncAll();

        // items 应失败或跳过
        SyncResult itemsResult = results.get("items");
        assertNotNull(itemsResult);

        // skill_gems 应正常同步
        SyncResult gemsResult = results.get("skill_gems");
        assertNotNull(gemsResult);
        assertFalse(gemsResult.isSkipped());
    }

    // ==================== Helpers ====================

    private static final String[] ALL_CARGO_TABLES = {
        "items", "skill_gems", "passive_skills", "mods",
        "weapons", "armours", "shields", "amulets", "flasks",
        "jewels", "stackables", "maps", "map_fragments", "map_series", "divination_cards",
        "mod_stats", "mod_spawn_weights", "mod_generation_weights", "mod_sell_prices",
        "item_mods", "item_stats", "item_buffs",
        "crafting_bench_options", "crafting_bench_options_costs", "essences", "fossils", "fossil_weights",
        "vendor_rewards", "item_sell_prices", "item_purchase_costs",
        "skill", "skill_levels", "skill_stats_per_level", "skill_quality", "skill_quality_stats", "gem_levels",
        "passive_skill_connections", "mastery_effects", "mastery_groups",
        "character_classes", "ascendancy_classes",
        "monsters", "monster_types", "monster_base_stats", "monster_life_scaling",
        "monster_map_multipliers", "monster_resistances", "areas", "atlas_nodes",
        "delve_level_scaling", "delve_resources_per_level", "delve_upgrades", "delve_upgrade_stats",
        "heist_areas", "heist_jobs", "heist_npcs", "heist_npc_skills", "heist_npc_stats", "heist_equipment",
        "blight_crafting_recipes", "blight_crafting_recipes_items", "blight_items", "blight_towers",
        "harvest_crafting_options", "harvest_plant_boosters", "harvest_seeds",
        "synthesis_areas", "synthesis_corrupted_mods", "synthesis_global_mods", "synthesis_mods",
        "bestiary_recipes", "bestiary_recipe_components",
        "incursion_rooms",
        "pantheon", "pantheon_souls", "pantheon_stats",
        "versions", "legacy_variants", "prophecies", "quest_rewards",
        "spawn_weights", "generic_stats",
        "tattoos", "tinctures", "sentinels", "idols", "grafts", "corpse_items",
        "cosmetic_items", "hideout_doodads", "guides"
    };

    /**
     * Enqueue count=0 responses for all equipment subtables and mods/crafting tables so they are skipped.
     */
    private void enqueueEquipmentSubtableCountResponses() {
        String[] tables = {"weapons", "armours", "shields", "amulets", "flasks",
            "jewels", "stackables", "maps", "map_fragments", "map_series", "divination_cards",
            "mod_stats", "mod_spawn_weights", "mod_generation_weights", "mod_sell_prices",
            "item_mods", "item_stats", "item_buffs",
            "crafting_bench_options", "crafting_bench_options_costs", "essences", "fossils", "fossil_weights",
            "vendor_rewards", "item_sell_prices", "item_purchase_costs",
            "skill", "skill_levels", "skill_stats_per_level", "skill_quality", "skill_quality_stats", "gem_levels",
            "passive_skill_connections", "mastery_effects", "mastery_groups",
            "character_classes", "ascendancy_classes",
            "monsters", "monster_types", "monster_base_stats", "monster_life_scaling",
            "monster_map_multipliers", "monster_resistances", "areas", "atlas_nodes",
            "delve_level_scaling", "delve_resources_per_level", "delve_upgrades", "delve_upgrade_stats",
            "heist_areas", "heist_jobs", "heist_npcs", "heist_npc_skills", "heist_npc_stats", "heist_equipment",
            "blight_crafting_recipes", "blight_crafting_recipes_items", "blight_items", "blight_towers",
            "harvest_crafting_options", "harvest_plant_boosters", "harvest_seeds",
            "synthesis_areas", "synthesis_corrupted_mods", "synthesis_global_mods", "synthesis_mods",
            "bestiary_recipes", "bestiary_recipe_components",
            "incursion_rooms",
            "pantheon", "pantheon_souls", "pantheon_stats",
            "versions", "legacy_variants", "prophecies", "quest_rewards",
            "spawn_weights", "generic_stats",
            "tattoos", "tinctures", "sentinels", "idols", "grafts", "corpse_items",
            "cosmetic_items", "hideout_doodads", "guides"};
        for (String table : tables) {
            enqueueCountResponse(table, 0);
        }
    }

    /**
     * Enqueue count responses for all 30 tables.
     */
    private void enqueueAllCountResponses(Map<String, Integer> counts) {
        for (String table : ALL_CARGO_TABLES) {
            enqueueCountResponse(table, counts.getOrDefault(table, 0));
        }
    }

    /**
     * 入队 Cargo 查询响应（含 COUNT 和 DATA 两个请求）。
     * 第一个请求：COUNT(*) → 返回 count
     * 第二个请求：实际数据 → 返回 dataJson
     */
    private void enqueueCargoResponse(String table, int totalCount, String dataJson) {
        // COUNT(*) 响应
        enqueueCountResponse(table, totalCount);
        // 数据响应
        mockServer.enqueue(new MockResponse()
            .setBody(dataJson)
            .addHeader("Content-Type", "application/json"));
    }

    private void enqueueCountResponse(String table, int count) {
        mockServer.enqueue(new MockResponse()
            .setBody("{\"cargoquery\":[{\"title\":{\"COUNT(*)\":\"" + count + "\"}}]}")
            .addHeader("Content-Type", "application/json"));
    }

    private void enqueueDataResponse(String dataJson) {
        mockServer.enqueue(new MockResponse()
            .setBody(dataJson)
            .addHeader("Content-Type", "application/json"));
    }

    private String itemJson() {
        return """
            {"cargoquery":[
              {"title":{"_pageID":"1","_pageName":"Iron Ring","name":"Iron Ring",
                "class_id":"Ring","inventory_width":"1","inventory_height":"1",
                "requirements":"[]","implicits":"[]","properties":"[]",
                "flavour_text":"","drop_level":"1"}},
              {"title":{"_pageID":"2","_pageName":"Gold Ring","name":"Gold Ring",
                "class_id":"Ring","inventory_width":"1","inventory_height":"1",
                "requirements":"[]","implicits":"[]","properties":"[]",
                "flavour_text":"","drop_level":"20"}}
            ]}""";
    }

    private String itemJsonBatch(int startId, int count) {
        StringBuilder sb = new StringBuilder("{\"cargoquery\":[");
        for (int i = 0; i < count; i++) {
            int id = startId + i + 1;
            if (i > 0) sb.append(",");
            sb.append("{\"title\":{\"_pageID\":\"").append(id)
                .append("\",\"_pageName\":\"Item").append(id)
                .append("\",\"name\":\"Item ").append(id)
                .append("\",\"class_id\":\"Test\",\"inventory_width\":\"1\",\"inventory_height\":\"1\",")
                .append("\"requirements\":\"[]\",\"implicits\":\"[]\",\"properties\":\"[]\",")
                .append("\"flavour_text\":\"\",\"drop_level\":\"1\"}}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private String skillGemJson() {
        return """
            {"cargoquery":[
              {"title":{"_pageID":"100","name":"Fireball","gem_type":"active",
                "gem_tags":"[\\"spell\\",\\"fire\\"]","primary_attribute":"int",
                "description":"Launches a fireball","quality_stats":"[]","level_stats":"[]",
                "required_level":"1"}}
            ]}""";
    }

    private String passiveSkillJson() {
        return """
            {"cargoquery":[
              {"title":{"_pageID":"200","name":"Heart of the Warrior","passive_class":"Normal",
                "ascendancy":"","stats":"[]","is_keystone":"0","is_notable":"1",
                "is_jewel_socket":"0","x":"0","y":"0","connections":"[]"}}
            ]}""";
    }

    private String modJson() {
        return """
            {"cargoquery":[
              {"title":{"_pageID":"300","name":"of the Walrus","domain":"item",
                "generation_type":"suffix","mod_group":"Strength","stat_text":"[]",
                "spawn_tags":"[\\"ring\\"]","spawn_weights":"[{\\"tag\\":\\"ring\\",\\"weight\\":1000}]",
                "required_level":"1"}}
            ]}""";
    }
}
