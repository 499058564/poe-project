package com.poe.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.util.concurrent.RateLimiter;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PoeNinjaClient 单元测试。
 *
 * <p>使用 MockWebServer 模拟 poe.ninja API 响应，验证：
 * <ul>
 *   <li>通货汇率查询（CurrencyOverview）</li>
 *   <li>物品价格查询（ItemOverview）</li>
 *   <li>URL 参数正确传递</li>
 *   <li>HTTP 5xx 重试</li>
 * </ul>
 */
class PoeNinjaClientTest {

    private MockWebServer mockServer;
    private PoeNinjaClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(5))
            .build();

        RateLimiter unlimited = RateLimiter.create(1000.0);
        client = new PoeNinjaClient(httpClient, unlimited,
            mockServer.url("/api/data").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    // ==================== CurrencyOverview ====================

    @Test
    @DisplayName("获取通货汇率应正确解析 JSON 响应")
    void shouldParseCurrencyOverview() {
        mockServer.enqueue(new MockResponse()
            .setBody("""
                {
                  "lines": [
                    { "currencyTypeName": "Divine Orb",
                      "chaosEquivalent": 235.5,
                      "receive": { "value": 235.5 } },
                    { "currencyTypeName": "Chaos Orb",
                      "chaosEquivalent": 1.0,
                      "receive": { "value": 1.0 } }
                  ],
                  "language": { "name": "English", "translations": {} }
                }""")
            .addHeader("Content-Type", "application/json"));

        JsonNode result = client.getCurrencyOverview("Settlers", "Currency");

        JsonNode lines = result.path("lines");
        assertTrue(lines.isArray(), "lines should be an array");
        assertEquals(2, lines.size());
        assertEquals("Divine Orb",
            lines.get(0).path("currencyTypeName").asText());
        assertEquals(235.5,
            lines.get(0).path("chaosEquivalent").asDouble(), 0.01);
    }

    @Test
    @DisplayName("获取通货汇率应正确传递 league 和 type 参数")
    void shouldPassLeagueAndTypeParams() throws Exception {
        mockServer.enqueue(new MockResponse()
            .setBody("{\"lines\":[]}")
            .addHeader("Content-Type", "application/json"));

        client.getCurrencyOverview("Affliction", "Fragment");

        RecordedRequest req = mockServer.takeRequest();
        String query = req.getRequestUrl().encodedQuery();
        assertTrue(query.contains("league=Affliction"),
            "URL should contain league=Affliction");
        assertTrue(query.contains("type=Fragment"),
            "URL should contain type=Fragment");
        assertEquals("GET", req.getMethod());
    }

    // ==================== ItemOverview ====================

    @Test
    @DisplayName("获取物品价格应正确解析 JSON 响应")
    void shouldParseItemOverview() {
        mockServer.enqueue(new MockResponse()
            .setBody("""
                {
                  "lines": [
                    { "name": "Mageblood",
                      "chaosValue": 35000.0,
                      "divineValue": 235.5,
                      "listingCount": 842 },
                    { "name": "Headhunter",
                      "chaosValue": 12000.0,
                      "divineValue": 80.7,
                      "listingCount": 1203 }
                  ],
                  "language": { "name": "English", "translations": {} }
                }""")
            .addHeader("Content-Type", "application/json"));

        JsonNode result = client.getItemOverview("Settlers", "UniqueBelt");

        JsonNode lines = result.path("lines");
        assertEquals(2, lines.size());
        assertEquals("Mageblood", lines.get(0).path("name").asText());
        assertEquals(35000.0,
            lines.get(0).path("chaosValue").asDouble(), 0.01);
    }

    @Test
    @DisplayName("查询空赛季应返回空 lines 数组")
    void shouldReturnEmptyLinesForMissingLeague() {
        mockServer.enqueue(new MockResponse()
            .setBody("{\"lines\":[]}")
            .addHeader("Content-Type", "application/json"));

        JsonNode result = client.getItemOverview("UnknownLeague", "Currency");

        JsonNode lines = result.path("lines");
        assertTrue(lines.isArray());
        assertEquals(0, lines.size());
    }

    // ==================== 错误处理 ====================

    @Test
    @DisplayName("HTTP 5xx 应触发重试")
    void shouldRetryOnServerError() {
        mockServer.enqueue(new MockResponse().setResponseCode(503));
        mockServer.enqueue(new MockResponse().setResponseCode(503));
        mockServer.enqueue(new MockResponse().setResponseCode(503));
        mockServer.enqueue(new MockResponse()
            .setBody("{\"lines\":[{\"currencyTypeName\":\"Chaos Orb\",\"chaosEquivalent\":1.0}]}")
            .addHeader("Content-Type", "application/json"));

        JsonNode result = client.getCurrencyOverview("Settlers", "Currency");
        assertEquals("Chaos Orb",
            result.path("lines").get(0).path("currencyTypeName").asText());
    }
}
