package com.poe.provider;

import com.google.common.util.concurrent.RateLimiter;
import com.poe.cache.manager.DatabaseManager;
import com.poe.core.version.GameVersion;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VersionDetector 单元测试。
 * 使用 MockWebServer 模拟 GGG API，in-memory SQLite 模拟 Wiki/缓存。
 */
class VersionDetectorTest {

    private MockWebServer mockServer;
    private VersionDetector detector;
    private GggApiClient gggClient;

    @BeforeAll
    static void setUpMode() {
        DatabaseManager.testMode = true;
    }

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(5))
            .build();
        RateLimiter unlimited = RateLimiter.create(1000.0);
        gggClient = new GggApiClient(httpClient, unlimited,
            mockServer.url("/").toString());

        DatabaseManager.reset();
        DatabaseManager.testDbPath = ":memory:";
        detector = new VersionDetector(gggClient, DatabaseManager.getInstance());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @AfterAll
    static void tearDownMode() {
        DatabaseManager.reset();
        DatabaseManager.testMode = false;
        DatabaseManager.testDbPath = null;
    }

    // ==================== GGG API 检测 ====================

    @Test
    @DisplayName("应能从 GGG API 赛季列表检测版本")
    void shouldDetectFromGggApi() {
        mockServer.enqueue(new MockResponse()
            .setBody("""
                [
                  {
                    "id": "Settlers",
                    "realm": "pc",
                    "description": "Settlers of Kalguur",
                    "startAt": "2024-07-26T20:00:00Z"
                  },
                  {
                    "id": "Necropolis",
                    "realm": "pc",
                    "description": "Necropolis",
                    "startAt": "2024-03-29T20:00:00Z"
                  }
                ]""")
            .addHeader("Content-Type", "application/json"));

        GameVersion version = detector.detectFromGggApi();
        assertFalse(version.isUnknown());
        assertEquals("3.25", version.getVersion());
        assertEquals("Settlers", version.getLeague());
        assertEquals("GGG API", version.getSource());
    }

    @Test
    @DisplayName("GGG API 返回空数组应返回 unknown")
    void shouldReturnUnknownForEmptyGggResponse() {
        mockServer.enqueue(new MockResponse()
            .setBody("[]")
            .addHeader("Content-Type", "application/json"));

        GameVersion version = detector.detectFromGggApi();
        assertTrue(version.isUnknown());
    }

    @Test
    @DisplayName("GGG API 不可用时应降级到其他源")
    void shouldFallbackWhenGggFails() {
        // GGG 返回 503
        mockServer.enqueue(new MockResponse().setResponseCode(503));
        mockServer.enqueue(new MockResponse().setResponseCode(503));
        mockServer.enqueue(new MockResponse().setResponseCode(503));
        mockServer.enqueue(new MockResponse().setResponseCode(503));

        // Wiki 和缓存也没有数据
        GameVersion version = detector.getCurrentVersion();
        // 最终应返回 unknown（所有源都不可用）
        assertTrue(version.isUnknown());
    }

    // ==================== Wiki 检测 ====================

    @Test
    @DisplayName("应能从 Wiki base_items 表检测版本")
    void shouldDetectFromWiki() throws SQLException {
        DatabaseManager.getInstance().getConnection(); // trigger migration
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO base_items (name, class, version) "
                + "VALUES ('Mageblood', 'Belt', '3.16.0')");
            stmt.execute("INSERT INTO base_items (name, class, version) "
                + "VALUES ('Headhunter', 'Belt', '0.9.9')");
        }

        GameVersion version = detector.detectFromWiki();
        assertFalse(version.isUnknown());
        assertEquals("3.16.0", version.getVersion());
        assertEquals("Wiki", version.getSource());
    }

    @Test
    @DisplayName("Wiki 表为空时应返回 unknown")
    void shouldReturnUnknownForEmptyWiki() {
        GameVersion version = detector.detectFromWiki();
        assertTrue(version.isUnknown());
    }

    // ==================== 缓存检测 ====================

    @Test
    @DisplayName("应能从 data_version 缓存检测版本")
    void shouldDetectFromCache() throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS data_version ("
                + "table_name TEXT PRIMARY KEY, last_sync TEXT, "
                + "record_count INTEGER DEFAULT 0, wiki_version TEXT, "
                + "source TEXT DEFAULT 'wiki', source_version TEXT)");
            stmt.execute("INSERT INTO data_version (table_name, source, source_version) "
                + "VALUES ('base_items', 'wiki', '3.25.0')");
        }

        GameVersion version = detector.detectFromCache();
        assertFalse(version.isUnknown());
        assertEquals("3.25.0", version.getVersion());
        assertEquals("data_version", version.getSource());
    }

    @Test
    @DisplayName("空 data_version 表应返回 unknown")
    void shouldReturnUnknownForEmptyCache() throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS data_version ("
                + "table_name TEXT PRIMARY KEY, last_sync TEXT, "
                + "record_count INTEGER DEFAULT 0, wiki_version TEXT, "
                + "source TEXT DEFAULT 'wiki', source_version TEXT)");
        }

        GameVersion version = detector.detectFromCache();
        assertTrue(version.isUnknown());
    }

    // ==================== getCurrentVersion() ====================

    @Test
    @DisplayName("getCurrentVersion 应优先使用 GGG API")
    void shouldPreferGggApiInFullFlow() {
        mockServer.enqueue(new MockResponse()
            .setBody("[{\"id\": \"Settlers\", \"realm\": \"pc\"}]")
            .addHeader("Content-Type", "application/json"));

        GameVersion version = detector.getCurrentVersion();
        assertEquals("3.25", version.getVersion());
        assertEquals("Settlers", version.getLeague());
        assertEquals("GGG API", version.getSource());
    }

    // ==================== isWikiDataStale() ====================

    @Test
    @DisplayName("Wiki 数据与 GGG 版本相同时不应过期")
    void shouldNotBeStaleWhenVersionsMatch() throws SQLException {
        mockServer.enqueue(new MockResponse()
            .setBody("[{\"id\": \"Settlers\", \"realm\": \"pc\"}]")
            .addHeader("Content-Type", "application/json"));

        DatabaseManager.getInstance().getConnection(); // trigger migration
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO base_items (name, class, version) "
                + "VALUES ('Mageblood', 'Belt', '3.25')");
        }

        assertFalse(detector.isWikiDataStale());
    }

    @Test
    @DisplayName("GGG API 不可用时 isWikiDataStale 应返回 false")
    void shouldNotBeStaleWhenGggUnavailable() {
        mockServer.enqueue(new MockResponse().setResponseCode(503));
        mockServer.enqueue(new MockResponse().setResponseCode(503));
        mockServer.enqueue(new MockResponse().setResponseCode(503));
        mockServer.enqueue(new MockResponse().setResponseCode(503));

        assertFalse(detector.isWikiDataStale());
    }
}
