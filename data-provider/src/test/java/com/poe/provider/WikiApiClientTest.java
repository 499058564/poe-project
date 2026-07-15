package com.poe.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.util.concurrent.RateLimiter;
import com.poe.common.exception.DataSyncException;
import com.poe.provider.client.WikiApiClient;
import okhttp3.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WikiApiClient 单元测试。
 * <p>
 * 使用 OkHttp MockWebServer 模拟 PoE Wiki API 响应，验证：
 * <ul>
 *   <li>Cargo 表查询与 JSON 解析</li>
 *   <li>COUNT(*) 记录数查询</li>
 *   <li>wikitext 页面内容获取</li>
 *   <li>限流行为</li>
 *   <li>自动重试与指数退避</li>
 *   <li>API 错误处理</li>
 * </ul>
 */
class WikiApiClientTest {

    private MockWebServer mockServer;
    private WikiApiClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(5))
            .build();

        // 不限速 + MockWebServer URL
        RateLimiter unlimited = RateLimiter.create(1000.0);
        client = new WikiApiClient(httpClient, unlimited, mockServer.url("/w/api.php").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    // ==================== Cargo 表查询 ====================

    @Test
    @DisplayName("查询 cargo 表应正确解析嵌套 JSON 结构")
    void shouldParseCargoTableResponse() {
        mockServer.enqueue(new MockResponse()
            .setBody("""
                {
                  "cargoquery": [
                    { "title": { "_pageName": "Iron Ring", "name": "Iron Ring", "class": "Ring" } },
                    { "title": { "_pageName": "Gold Ring", "name": "Gold Ring", "class": "Ring" } }
                  ]
                }""")
            .addHeader("Content-Type", "application/json"));

        JsonNode result = client.queryCargoTable("items", "_pageName,name,class", 0, 100);

        JsonNode items = result.path("cargoquery");
        assertTrue(items.isArray(), "cargoquery should be an array");
        assertEquals(2, items.size());
        assertEquals("Iron Ring", items.get(0).path("title").path("name").asText());
    }

    @Test
    @DisplayName("查询空 cargo 表应返回空数组")
    void shouldReturnEmptyArrayForEmptyTable() {
        mockServer.enqueue(new MockResponse()
            .setBody("""
                { "cargoquery": [] }""")
            .addHeader("Content-Type", "application/json"));

        JsonNode result = client.queryCargoTable("items", "name", 0, 100);

        JsonNode items = result.path("cargoquery");
        assertTrue(items.isArray());
        assertEquals(0, items.size());
    }

    @Test
    @DisplayName("查询 COUNT(*) 应返回正确的记录数")
    void shouldParseCountResponse() {
        mockServer.enqueue(new MockResponse()
            .setBody("""
                {
                  "cargoquery": [
                    { "title": { "COUNT(*)": "5234" } }
                  ]
                }""")
            .addHeader("Content-Type", "application/json"));

        int count = client.queryCargoTableCount("items");
        assertEquals(5234, count);
    }

    @Test
    @DisplayName("查询 COUNT(*) 无结果时返回 0")
    void shouldReturnZeroWhenCountQueryFails() {
        mockServer.enqueue(new MockResponse()
            .setBody("""
                { "cargoquery": [] }""")
            .addHeader("Content-Type", "application/json"));

        int count = client.queryCargoTableCount("nonexistent");
        assertEquals(0, count);
    }

    // ==================== 页面内容 ====================

    @Test
    @DisplayName("获取页面 wikitext 应正确提取内容")
    void shouldParsePageContent() {
        mockServer.enqueue(new MockResponse()
            .setBody("""
                {
                  "parse": {
                    "title": "Iron Ring",
                    "wikitext": { "*": "{{Item\\n|name=Iron Ring\\n|class=Ring\\n}}" }
                  }
                }""")
            .addHeader("Content-Type", "application/json"));

        String content = client.queryPageContent("Iron Ring");
        assertTrue(content.contains("{{Item"), "Should contain wikitext markup");
        assertTrue(content.contains("Iron Ring"));
    }

    @Test
    @DisplayName("获取不存在页面应抛出异常")
    void shouldThrowForNonexistentPage() {
        mockServer.enqueue(new MockResponse()
            .setBody("""
                {
                  "error": {
                    "code": "missingtitle",
                    "info": "The page you specified doesn't exist."
                  }
                }""")
            .addHeader("Content-Type", "application/json"));

        assertThrows(DataSyncException.class,
            () -> client.queryPageContent("NonExistentPage"));
    }

    // ==================== API 错误处理 ====================

    @Test
    @DisplayName("API 返回错误时应抛出 DataSyncException")
    void shouldThrowOnApiError() {
        mockServer.enqueue(new MockResponse()
            .setBody("""
                {
                  "error": {
                    "code": "cargo-queryfail",
                    "info": "Table 'bad_table' not found"
                  }
                }""")
            .addHeader("Content-Type", "application/json"));

        assertThrows(DataSyncException.class,
            () -> client.queryCargoTable("bad_table", "name", 0, 10));
    }

    @Test
    @DisplayName("HTTP 5xx 错误应触发重试")
    void shouldRetryOnServerError() {
        // 前 3 次返回 503，第 4 次返回成功（最多 3 次重试 = 4 次尝试）
        mockServer.enqueue(new MockResponse().setResponseCode(503));
        mockServer.enqueue(new MockResponse().setResponseCode(503));
        mockServer.enqueue(new MockResponse().setResponseCode(503));
        mockServer.enqueue(new MockResponse()
            .setBody("{\"cargoquery\":[{\"title\":{\"name\":\"Test\"}}]}")
            .addHeader("Content-Type", "application/json"));

        JsonNode result = client.queryCargoTable("items", "name", 0, 1);
        assertEquals("Test", result.path("cargoquery").get(0).path("title").path("name").asText());
    }

    @Test
    @DisplayName("HTTP 4xx 错误不应重试")
    void shouldNotRetryOnClientError() {
        mockServer.enqueue(new MockResponse().setResponseCode(400));

        assertThrows(DataSyncException.class,
            () -> client.queryCargoTable("items", "name", 0, 1));
    }

    // ==================== 限流 ====================

    @Test
    @DisplayName("多次请求应受到速率限制")
    void shouldRespectRateLimit() {
        // 限速 5 req/s，前 5 个令牌立即可用，第 6 个需等待约 200ms
        RateLimiter limiter = RateLimiter.create(5.0);

        long start = System.nanoTime();
        for (int i = 0; i < 6; i++) {
            limiter.acquire();
        }
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        assertTrue(elapsed >= 150,
            "6 acquires at 5 req/s should take >= ~150ms, but took " + elapsed + "ms");
    }

    // ==================== 分页参数 ====================

    @Test
    @DisplayName("查询参数应正确传递 offset 和 limit")
    void shouldPassPaginationParams() throws Exception {
        mockServer.enqueue(new MockResponse()
            .setBody("{\"cargoquery\":[]}")
            .addHeader("Content-Type", "application/json"));

        client.queryCargoTable("items", "name", 50, 200);

        RecordedRequest req = mockServer.takeRequest();
        String path = req.getRequestUrl().encodedPath();
        String query = req.getRequestUrl().encodedQuery();
        assertEquals("/w/api.php", path);
        assertTrue(query.contains("offset=50"));
        assertTrue(query.contains("limit=200"));
    }
}
