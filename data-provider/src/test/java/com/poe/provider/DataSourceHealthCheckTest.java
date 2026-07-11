package com.poe.provider;

import com.google.common.util.concurrent.RateLimiter;
import com.poe.cache.manager.DatabaseManager;
import com.poe.core.version.DataSourceHealth;
import com.poe.core.version.DataSourceHealth.Status;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataSourceHealthCheck 单元测试。
 * 使用 MockWebServer 模拟远程 API，in-memory SQLite 做 Wiki 检查。
 */
class DataSourceHealthCheckTest {

    private MockWebServer mockGgg;
    private MockWebServer mockNinja;
    private DataSourceHealthCheck healthCheck;
    private GggApiClient gggClient;
    private PoeNinjaClient ninjaClient;

    @BeforeAll
    static void setUpMode() {
        DatabaseManager.testMode = true;
    }

    @BeforeEach
    void setUp() throws IOException {
        mockGgg = new MockWebServer();
        mockNinja = new MockWebServer();
        mockGgg.start();
        mockNinja.start();

        OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(5))
            .build();
        RateLimiter unlimited = RateLimiter.create(1000.0);

        gggClient = new GggApiClient(httpClient, unlimited,
            mockGgg.url("/").toString());
        ninjaClient = new PoeNinjaClient(httpClient, unlimited,
            mockNinja.url("/api/data").toString());

        PobDataExtractor pobExtractor = new PobDataExtractor(
            Paths.get("nonexistent_pob_path"));

        DatabaseManager.reset();
        DatabaseManager.testDbPath = ":memory:";

        healthCheck = new DataSourceHealthCheck(
            gggClient, ninjaClient, pobExtractor,
            DatabaseManager.getInstance());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockGgg.shutdown();
        mockNinja.shutdown();
    }

    @AfterAll
    static void tearDownMode() {
        DatabaseManager.reset();
        DatabaseManager.testMode = false;
        DatabaseManager.testDbPath = null;
    }

    // ==================== checkAll() ====================

    @Test
    @DisplayName("所有源可用时应返回 4 项健康报告")
    void shouldReturnFourHealthReports() {
        // GGG 正常
        mockGgg.enqueue(new MockResponse()
            .setBody("[{\"id\": \"Settlers\", \"realm\": \"pc\"}]")
            .addHeader("Content-Type", "application/json"));

        // poe.ninja 正常
        mockNinja.enqueue(new MockResponse()
            .setBody("{\"lines\": [{\"currencyTypeName\": \"Chaos Orb\", \"chaosEquivalent\": 1.0}]}")
            .addHeader("Content-Type", "application/json"));

        List<DataSourceHealth> results = healthCheck.checkAll();
        assertEquals(4, results.size());
    }

    // ==================== GGG API ====================

    @Test
    @DisplayName("GGG API 响应正常时应为 HEALTHY")
    void shouldReportGggHealthy() {
        mockGgg.enqueue(new MockResponse()
            .setBody("[{\"id\": \"Settlers\", \"realm\": \"pc\"}]")
            .addHeader("Content-Type", "application/json"));

        DataSourceHealth health = healthCheck.checkGggApi();
        assertEquals(Status.HEALTHY, health.getStatus());
        assertEquals("GGG API", health.getSourceName());
        assertTrue(health.isAvailable());
        assertTrue(health.getMessage().contains("leagues"));
    }

    @Test
    @DisplayName("GGG API 返回空响应时应为 DEGRADED")
    void shouldReportGggDegradedForEmpty() {
        mockGgg.enqueue(new MockResponse()
            .setBody("[]")
            .addHeader("Content-Type", "application/json"));

        DataSourceHealth health = healthCheck.checkGggApi();
        assertEquals(Status.DEGRADED, health.getStatus());
        assertTrue(health.isAvailable());
    }

    @Test
    @DisplayName("GGG API 不可用时应为 UNAVAILABLE")
    void shouldReportGggUnavailable() {
        mockGgg.enqueue(new MockResponse().setResponseCode(503));
        mockGgg.enqueue(new MockResponse().setResponseCode(503));
        mockGgg.enqueue(new MockResponse().setResponseCode(503));
        mockGgg.enqueue(new MockResponse().setResponseCode(503));

        DataSourceHealth health = healthCheck.checkGggApi();
        assertEquals(Status.UNAVAILABLE, health.getStatus());
        assertFalse(health.isAvailable());
    }

    // ==================== poe.ninja ====================

    @Test
    @DisplayName("poe.ninja 响应正常时应为 HEALTHY")
    void shouldReportNinjaHealthy() {
        mockNinja.enqueue(new MockResponse()
            .setBody("{\"lines\": [{\"currencyTypeName\": \"Chaos Orb\", \"chaosEquivalent\": 1.0}]}")
            .addHeader("Content-Type", "application/json"));

        DataSourceHealth health = healthCheck.checkPoeNinja();
        assertEquals(Status.HEALTHY, health.getStatus());
        assertEquals("poe.ninja", health.getSourceName());
        assertTrue(health.getMessage().contains("currency entries"));
    }

    @Test
    @DisplayName("poe.ninja 缺少 lines 字段时应为 DEGRADED")
    void shouldReportNinjaDegradedWithoutLines() {
        mockNinja.enqueue(new MockResponse()
            .setBody("{}")
            .addHeader("Content-Type", "application/json"));

        DataSourceHealth health = healthCheck.checkPoeNinja();
        assertEquals(Status.DEGRADED, health.getStatus());
    }

    @Test
    @DisplayName("poe.ninja 不可用时应为 UNAVAILABLE")
    void shouldReportNinjaUnavailable() {
        mockNinja.enqueue(new MockResponse().setResponseCode(503));
        mockNinja.enqueue(new MockResponse().setResponseCode(503));
        mockNinja.enqueue(new MockResponse().setResponseCode(503));
        mockNinja.enqueue(new MockResponse().setResponseCode(503));

        DataSourceHealth health = healthCheck.checkPoeNinja();
        assertEquals(Status.UNAVAILABLE, health.getStatus());
    }

    // ==================== Wiki 缓存 ====================

    @Test
    @DisplayName("Wiki 缓存有数据时应为 HEALTHY")
    void shouldReportWikiCacheHealthy() throws Exception {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO base_items (name, class, version) "
                + "VALUES ('Test Item', 'Test', '1.0')");
        }

        DataSourceHealth health = healthCheck.checkWikiCache();
        assertEquals(Status.HEALTHY, health.getStatus());
        assertTrue(health.getMessage().contains("items cached"));
    }

    @Test
    @DisplayName("Wiki 缓存为空时应为 DEGRADED")
    void shouldReportWikiCacheDegradedWhenEmpty() {
        DataSourceHealth health = healthCheck.checkWikiCache();
        assertEquals(Status.DEGRADED, health.getStatus());
        assertTrue(health.getMessage().contains("empty"));
    }

    // ==================== POB ====================

    @Test
    @DisplayName("POB 数据不可用时应为 DEGRADED")
    void shouldReportPobDegradedWhenUnavailable() {
        DataSourceHealth health = healthCheck.checkPob();
        assertEquals(Status.DEGRADED, health.getStatus());
        assertTrue(health.getMessage().contains("not found"));
    }
}
