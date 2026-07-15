package com.poe.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.util.concurrent.RateLimiter;
import com.poe.provider.client.GggApiClient;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GggApiClient 单元测试。
 *
 * <p>使用 MockWebServer 模拟 GGG API 响应，验证：
 * <ul>
 *   <li>赛季列表查询</li>
 *   <li>天梯数据查询</li>
 *   <li>URL 参数正确传递</li>
 *   <li>OAuth2 端点预留检测</li>
 * </ul>
 */
class GggApiClientTest {

    private MockWebServer mockServer;
    private GggApiClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(5))
            .build();

        RateLimiter unlimited = RateLimiter.create(1000.0);
        client = new GggApiClient(httpClient, unlimited,
            mockServer.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    // ==================== 赛季列表 ====================

    @Test
    @DisplayName("获取赛季列表应正确解析 JSON 响应")
    void shouldParseLeaguesResponse() {
        mockServer.enqueue(new MockResponse()
            .setBody("""
                [
                  { "id": "Settlers",
                    "realm": "pc",
                    "description": "Settlers of Kalguur",
                    "startAt": "2024-07-26T20:00:00Z" },
                  { "id": "Standard",
                    "realm": "pc",
                    "description": "Standard League",
                    "startAt": "2013-01-23T00:00:00Z" }
                ]""")
            .addHeader("Content-Type", "application/json"));

        JsonNode result = client.getLeagues();

        assertTrue(result.isArray(), "response should be an array");
        assertEquals(2, result.size());
        assertEquals("Settlers", result.get(0).path("id").asText());
        assertEquals("pc", result.get(0).path("realm").asText());
    }

    @Test
    @DisplayName("赛季列表为空时应返回空数组")
    void shouldReturnEmptyArrayForNoLeagues() {
        mockServer.enqueue(new MockResponse()
            .setBody("[]")
            .addHeader("Content-Type", "application/json"));

        JsonNode result = client.getLeagues();
        assertTrue(result.isArray());
        assertEquals(0, result.size());
    }

    // ==================== 天梯数据 ====================

    @Test
    @DisplayName("获取天梯数据应正确解析 JSON 响应")
    void shouldParseLadderResponse() {
        mockServer.enqueue(new MockResponse()
            .setBody("""
                {
                  "total": 15000,
                  "entries": [
                    { "rank": 1,
                      "dead": false,
                      "character": { "name": "TopPlayer", "level": 100, "class": "Trickster" },
                      "account": { "name": "ProGamer" } },
                    { "rank": 2,
                      "dead": false,
                      "character": { "name": "SecondPlace", "level": 98, "class": "Deadeye" },
                      "account": { "name": "AlmostPro" } }
                  ]
                }""")
            .addHeader("Content-Type", "application/json"));

        JsonNode result = client.getLadder("Settlers", 2);

        assertEquals(15000, result.path("total").asInt());
        JsonNode entries = result.path("entries");
        assertEquals(2, entries.size());
        assertEquals(1, entries.get(0).path("rank").asInt());
        assertEquals("TopPlayer",
            entries.get(0).path("character").path("name").asText());
    }

    @Test
    @DisplayName("天梯查询应正确传递 league 和 limit 参数")
    void shouldPassLeagueAndLimitParams() throws Exception {
        mockServer.enqueue(new MockResponse()
            .setBody("{\"total\":0,\"entries\":[]}")
            .addHeader("Content-Type", "application/json"));

        client.getLadder("Affliction", 100);

        RecordedRequest req = mockServer.takeRequest();
        String path = req.getRequestUrl().encodedPath();
        assertTrue(path.contains("Affliction"),
            "Path should contain league name 'Affliction'");
        String query = req.getRequestUrl().encodedQuery();
        assertTrue(query.contains("limit=100"),
            "Query should contain limit=100");
    }

    // ==================== OAuth2 端点 ====================

    @Test
    @DisplayName("getProfile 当前应抛出 UnsupportedOperationException")
    void shouldThrowForGetProfile() {
        assertThrows(UnsupportedOperationException.class,
            () -> client.getProfile("fake-token"),
            "getProfile should throw until OAuth2 is implemented");
    }

    // ==================== 错误处理 ====================

    @Test
    @DisplayName("HTTP 5xx 应触发重试")
    void shouldRetryOnServerError() {
        mockServer.enqueue(new MockResponse().setResponseCode(503));
        mockServer.enqueue(new MockResponse().setResponseCode(503));
        mockServer.enqueue(new MockResponse().setResponseCode(503));
        mockServer.enqueue(new MockResponse()
            .setBody("[{\"id\":\"Settlers\",\"realm\":\"pc\"}]")
            .addHeader("Content-Type", "application/json"));

        JsonNode result = client.getLeagues();
        assertEquals(1, result.size());
        assertEquals("Settlers", result.get(0).path("id").asText());
    }
}
