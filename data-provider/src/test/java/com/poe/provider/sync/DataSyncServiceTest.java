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

        // 配置 DatabaseManager 使用内存数据库
        DatabaseManager.testMode = true;
        DatabaseManager.testDbPath = ":memory:";
        DatabaseManager.reset();

        // 初始化表结构
        try {
            DatabaseManager.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // 使用 MockWebServer 地址的 WikiApiClient
        OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(5))
            .build();
        WikiApiClient wikiClient = new WikiApiClient(httpClient,
            com.google.common.util.concurrent.RateLimiter.create(1000.0),
            mockServer.url("/w/api.php").toString());

        syncService = new DataSyncService(wikiClient, DatabaseManager.getInstance());

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
    @DisplayName("全量同步 4 张核心表，数据应正确写入 SQLite")
    void shouldSyncAllTablesAndInsertData() throws Exception {
        // 为每张表设置 mock 响应
        enqueueCargoResponse("items", 2, itemJson());
        enqueueCargoResponse("skill_gems", 1, skillGemJson());
        enqueueCargoResponse("passive_skills", 1, passiveSkillJson());
        enqueueCargoResponse("mods", 1, modJson());

        Map<String, SyncResult> results = syncService.syncAll();

        assertEquals(4, results.size());
        results.values().forEach(r -> {
            assertFalse(r.isSkipped(), "Tables should not be skipped on first sync");
            assertTrue(r.getSyncedRecords() > 0);
        });

        // 验证数据写入（不关闭共享连接，否则 :memory: 数据丢失）
        Connection conn = DatabaseManager.getInstance().getConnection();
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

        // 设置 600 条数据（会分 2 批，每批 500）
        enqueueCountResponse("items", 600);
        // 第一批 500
        enqueueDataResponse(itemJsonBatch(0, 500));
        // 第二批 100
        enqueueDataResponse(itemJsonBatch(500, 100));

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
        assertTrue(progressCount >= 2, "Should have at least 2 progress events (2 batches)");
    }

    // ==================== hasUpdates 测试 ====================

    @Test
    @DisplayName("hasUpdates 应在远程数据变化时返回 true")
    void shouldDetectRemoteUpdates() throws Exception {
        // 本地 0 条，远程 10 条 → 有更新
        enqueueCountResponse("items", 10);
        enqueueCountResponse("skill_gems", 0);
        enqueueCountResponse("passive_skills", 0);
        enqueueCountResponse("mods", 0);

        assertTrue(syncService.hasUpdates());
    }

    @Test
    @DisplayName("hasUpdates 应在数据一致时返回 false")
    void shouldReturnFalseWhenNoUpdates() throws Exception {
        // 先同步 items 表
        enqueueCargoResponse("items", 5, itemJsonBatch(0, 5));
        syncService.syncTable("items");

        // 再检查 — 为 hasUpdates() 的 4 个表各入队 COUNT 响应
        enqueueCountResponse("items", 5);
        enqueueCountResponse("skill_gems", 0);
        enqueueCountResponse("passive_skills", 0);
        enqueueCountResponse("mods", 0);

        assertFalse(syncService.hasUpdates());
    }

    // ==================== 表验证 ====================

    @Test
    @DisplayName("配置表数量应为 4 张核心表")
    void shouldHaveFourCoreTables() {
        Map<String, SyncResult> results = syncService.syncAll();
        assertEquals(4, results.size());
        assertTrue(results.containsKey("items"));
        assertTrue(results.containsKey("skill_gems"));
        assertTrue(results.containsKey("passive_skills"));
        assertTrue(results.containsKey("mods"));
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
