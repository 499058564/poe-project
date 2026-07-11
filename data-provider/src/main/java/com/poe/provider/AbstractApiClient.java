package com.poe.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import com.poe.common.exception.DataSyncException;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;

/**
 * 多数据源 API 客户端的抽象基类，提供统一的 HTTP 请求、限流与重试基础设施。
 *
 * <h3>设计原则</h3>
 * 子类只需指定 baseUrl、速率限制和超时配置，无需重复实现重试逻辑。
 *
 * <h3>共享能力</h3>
 * <ul>
 *   <li>Guava {@link RateLimiter} 限流（速率由子类指定）</li>
 *   <li>自动重试（可配置次数与退避延迟），仅对 IOException / 5xx 重试</li>
 *   <li>Jackson JSON 解析与错误检查（子类可覆盖 {@link #parseAndCheckError}）</li>
 *   <li>GET / JSON 响应的便捷方法 {@link #executeGet(String)}</li>
 * </ul>
 */
public abstract class AbstractApiClient {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** 默认最大重试次数。 */
    protected static final int DEFAULT_MAX_RETRIES = 3;

    /** 默认指数退避延迟（毫秒）：1s, 2s, 4s。 */
    protected static final long[] DEFAULT_RETRY_DELAYS_MS = {1000, 2000, 4000};

    /** HTTP 客户端。 */
    protected final OkHttpClient httpClient;

    /** 限流器。 */
    protected final RateLimiter rateLimiter;

    /** JSON 解析器。 */
    protected final ObjectMapper objectMapper;

    /** API 基础地址。 */
    protected final String baseUrl;

    /** 最大重试次数。 */
    protected final int maxRetries;

    /** 重试延迟数组（毫秒）。 */
    protected final long[] retryDelaysMs;

    /**
     * 创建 API 客户端。
     *
     * @param baseUrl        API 基础地址
     * @param rateLimit      每秒请求数上限
     * @param connectTimeout 连接超时时间
     * @param readTimeout    读取超时时间
     */
    protected AbstractApiClient(String baseUrl, double rateLimit,
                                Duration connectTimeout, Duration readTimeout) {
        this(baseUrl, rateLimit, connectTimeout, readTimeout,
             DEFAULT_MAX_RETRIES, DEFAULT_RETRY_DELAYS_MS);
    }

    /**
     * 创建 API 客户端（完整参数，用于自定义重试策略）。
     */
    protected AbstractApiClient(String baseUrl, double rateLimit,
                                Duration connectTimeout, Duration readTimeout,
                                int maxRetries, long[] retryDelaysMs) {
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(connectTimeout)
            .readTimeout(readTimeout)
            .build();
        this.rateLimiter = RateLimiter.create(rateLimit);
        this.objectMapper = new ObjectMapper();
        this.baseUrl = baseUrl;
        this.maxRetries = maxRetries;
        this.retryDelaysMs = retryDelaysMs;
    }

    /**
     * 创建 API 客户端（测试用：注入自定义 OkHttpClient、RateLimiter 和 baseUrl）。
     *
     * @param httpClient 自定义 HTTP 客户端（通常指向 MockWebServer）
     * @param rateLimiter 自定义限流器
     * @param baseUrl     API 基础地址
     */
    protected AbstractApiClient(OkHttpClient httpClient, RateLimiter rateLimiter,
                                String baseUrl) {
        this(httpClient, rateLimiter, baseUrl,
             DEFAULT_MAX_RETRIES, DEFAULT_RETRY_DELAYS_MS);
    }

    /**
     * 创建 API 客户端（测试用，可指定重试参数）。
     */
    protected AbstractApiClient(OkHttpClient httpClient, RateLimiter rateLimiter,
                                String baseUrl, int maxRetries, long[] retryDelaysMs) {
        this.httpClient = httpClient;
        this.rateLimiter = rateLimiter;
        this.objectMapper = new ObjectMapper();
        this.baseUrl = baseUrl;
        this.maxRetries = maxRetries;
        this.retryDelaysMs = retryDelaysMs;
    }

    // ==================== 公开方法 ====================

    /**
     * 向指定路径发起 GET 请求，返回 JSON 响应节点。
     *
     * @param path 相对于 baseUrl 的路径（含查询参数），如 "/api/data/ItemOverview?league=Settlers&type=Currency"
     * @return 解析后的 JSON 根节点
     * @throws DataSyncException 请求失败或 API 返回错误时抛出
     */
    public JsonNode executeGet(String path) {
        HttpUrl url = HttpUrl.parse(baseUrl + path);
        if (url == null) {
            throw new DataSyncException("Invalid URL: " + baseUrl + path);
        }

        Request request = new Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("Accept-Encoding", "gzip")
            .build();

        String json = executeWithRetry(request);
        return parseJson(json);
    }

    // ==================== 内部方法 ====================

    /**
     * 执行 HTTP 请求，含限流和自动重试。
     *
     * <p>重试策略：仅对 IOException 和 5xx 服务端错误进行重试，
     * 客户端错误（4xx）不重试。
     *
     * @param request HTTP 请求
     * @return 响应体字符串
     * @throws DataSyncException 所有重试耗尽后仍失败时抛出
     */
    protected String executeWithRetry(Request request) {
        IOException lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            rateLimiter.acquire();

            try {
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body == null) {
                            throw new IOException("Empty response body");
                        }
                        return body.string();
                    }

                    if (response.code() >= 500) {
                        String errorBody = response.body() != null ? response.body().string() : "";
                        throw new IOException("Server error " + response.code() + ": " + errorBody);
                    }

                    String errorBody = response.body() != null ? response.body().string() : "";
                    throw new DataSyncException(
                        "API returned " + response.code() + " for " + request.url().encodedPath()
                        + ": " + errorBody);
                }
            } catch (IOException e) {
                lastException = e;

                if (attempt < maxRetries) {
                    long delay = retryDelaysMs[Math.min(attempt, retryDelaysMs.length - 1)];
                    log.warn("Request failed (attempt {}/{}), retrying in {}ms: {}",
                        attempt + 1, maxRetries, delay, e.getMessage());
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new DataSyncException("Retry interrupted", ie);
                    }
                }
            }
        }

        throw new DataSyncException(
            "Request failed after " + maxRetries + " retries: " + request.url(),
            lastException);
    }

    /**
     * 解析 JSON 字符串。子类可覆盖此方法以添加自定义错误检查。
     *
     * @param json 原始 JSON 字符串
     * @return 解析后的根节点
     * @throws DataSyncException JSON 无效时抛出
     */
    protected JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new DataSyncException("Failed to parse API response", e);
        }
    }

    /**
     * 解析 JSON 并调用 {@link #parseAndCheckError(JsonNode)} 进行响应级错误检查。
     *
     * @param json 原始 JSON 字符串
     * @return 解析后的根节点
     */
    protected JsonNode parseAndCheckError(String json) {
        JsonNode root = parseJson(json);
        parseAndCheckError(root);
        return root;
    }

    /**
     * 检查 API 响应中的错误信息。
     * 默认实现为空——子类可根据各自 API 的响应格式覆盖。
     *
     * @param root 已解析的 JSON 根节点
     * @throws DataSyncException 响应包含错误时抛出
     */
    protected void parseAndCheckError(JsonNode root) {
        // 默认无操作，子类覆盖
    }
}
