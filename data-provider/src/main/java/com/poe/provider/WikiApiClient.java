package com.poe.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import com.poe.common.exception.DataSyncException;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * PoE Wiki Cargo 数据 API 的 HTTP 客户端。
 *
 * <h3>功能</h3>
 * <ul>
 *   <li>查询 Cargo 表数据（分页）</li>
 *   <li>获取表总记录数</li>
 *   <li>获取 Wiki 页面 wikitext 原始内容</li>
 * </ul>
 *
 * <h3>限流与重试</h3>
 * <ul>
 *   <li>请求速率限制为 2 req/s（通过 Guava {@link RateLimiter} 实现）</li>
 *   <li>请求失败时自动重试最多 3 次，普通错误退避 1s→2s→4s</li>
 *   <li>Cloudflare 429 限流使用更激进的退避：5s→15s→45s</li>
 *   <li>连接超时 10 秒，读取超时 30 秒</li>
 * </ul>
 */
public class WikiApiClient {

    private static final Logger log = LoggerFactory.getLogger(WikiApiClient.class);

    /** PoE Wiki API 基础地址。 */
    public static final String WIKI_API = "https://www.poewiki.net/w/api.php";

    /** User-Agent，模拟浏览器避免 Cloudflare 拦截。 */
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) PoEProject/1.0";

    /** 最大重试次数。 */
    private static final int MAX_RETRIES = 3;

    /** 普通错误退避延迟（毫秒）：1s, 2s, 4s。 */
    private static final long[] RETRY_DELAYS_MS = {1000, 2000, 4000};

    /** 429 限流专用退避延迟（毫秒）：5s, 15s, 45s，更激进的冷却。 */
    private static final long[] RATE_LIMIT_DELAYS_MS = {5000, 15000, 45000};

    /** Cloudflare 质询页面特征标记，用于检测 HTTP 200 但返回 HTML 质询页的情况。 */
    private static final String CF_CHALLENGE_MARKER = "_cf_chl_opt";
    private static final String CF_CHALLENGE_TITLE = "<title>Just a moment...</title>";

    private final OkHttpClient httpClient;
    private final RateLimiter rateLimiter;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    /**
     * 使用默认超时配置创建客户端。
     * <ul>
     *   <li>连接超时：10 秒</li>
     *   <li>读取超时：30 秒</li>
     *   <li>限流速率：2 req/s</li>
     * </ul>
     */
    public WikiApiClient() {
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .protocols(java.util.Collections.singletonList(okhttp3.Protocol.HTTP_1_1))
            .build();
        this.rateLimiter = RateLimiter.create(2.0);
        this.objectMapper = new ObjectMapper();
        this.baseUrl = WIKI_API;
    }

    /**
     * 使用自定义 {@link OkHttpClient}、{@link RateLimiter} 和 baseUrl 创建客户端。
     * <p>
     * baseUrl 用于测试时将请求指向 MockWebServer。
     *
     * @param httpClient  自定义 HTTP 客户端
     * @param rateLimiter 自定义限流器
     * @param baseUrl     API 基础地址
     */
    public WikiApiClient(OkHttpClient httpClient, RateLimiter rateLimiter, String baseUrl) {
        this.httpClient = httpClient;
        this.rateLimiter = rateLimiter;
        this.objectMapper = new ObjectMapper();
        this.baseUrl = baseUrl;
    }

    // ==================== 表与字段发现 ====================

    /**
     * 获取 Wiki 上所有可用的 Cargo 表名。
     *
     * @return Cargo 表名集合
     * @throws DataSyncException 网络错误或 API 返回错误时抛出
     */
    public Set<String> queryCargoTables() {
        HttpUrl url = HttpUrl.parse(baseUrl).newBuilder()
            .addQueryParameter("action", "cargotables")
            .addQueryParameter("format", "json")
            .build();

        Request request = new Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build();

        rateLimiter.acquire();
        String json = executeWithRetry(request);
        JsonNode root = parseAndCheckError(json);

        Set<String> tables = new LinkedHashSet<>();
        JsonNode cargoTables = root.path("cargotables");
        if (cargoTables.isArray()) {
            for (JsonNode node : cargoTables) {
                tables.add(node.asText());
            }
        }
        return tables;
    }

    /**
     * 获取指定 Cargo 表的字段定义。
     * <p>
     * 返回字段名列表，可直接用于构建 cargoquery 的 {@code fields} 参数，
     * 避免因硬编码不存在字段而触发 MWException。
     *
     * @param table Cargo 表名（如 "items"）
     * @return 字段名列表（按 wiki 返回顺序）
     * @throws DataSyncException 网络错误或 API 返回错误时抛出
     */
    public List<CargoField> queryCargoFields(String table) {
        HttpUrl url = HttpUrl.parse(baseUrl).newBuilder()
            .addQueryParameter("action", "cargofields")
            .addQueryParameter("format", "json")
            .addQueryParameter("table", table)
            .build();

        Request request = new Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build();

        rateLimiter.acquire();
        String json = executeWithRetry(request);
        JsonNode root = parseAndCheckError(json);

        List<CargoField> fields = new ArrayList<>();
        JsonNode cargoFields = root.path("cargofields");
        // cargofields 返回 JSON 对象 {fieldName: {type: "String", ...}}，不是数组
        if (cargoFields.isObject()) {
            var it = cargoFields.fields();
            while (it.hasNext()) {
                var entry = it.next();
                String name = entry.getKey();
                String type = entry.getValue().path("type").asText("");
                fields.add(new CargoField(name, type));
            }
        }
        return fields;
    }

    /**
     * Cargo 字段定义（名称 + 类型）。
     */
    public record CargoField(String name, String type) {}

    // ==================== 公开方法 ====================

    /**
     * 查询 Cargo 表数据（offset 分页）。
     *
     * @param tableName 表名，如 "items"、"skill_gems"
     * @param fields    需要的字段，逗号分隔，如 "_pageName,name,class"
     * @param offset    偏移量
     * @param limit     每批数量（最大 500）
     * @return 解析后的 JSON 响应根节点，数据位于 {@code cargoquery[].title} 路径下
     * @throws DataSyncException 网络错误或 API 返回错误时抛出
     */
    public JsonNode queryCargoTable(String tableName, String fields, int offset, int limit) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl).newBuilder()
            .addQueryParameter("action", "cargoquery")
            .addQueryParameter("format", "json")
            .addQueryParameter("tables", tableName)
            .addQueryParameter("fields", fields)
            .addQueryParameter("offset", String.valueOf(offset))
            .addQueryParameter("limit", String.valueOf(limit));

        return executeCargoQuery(urlBuilder.build());
    }

    /**
     * 查询 Cargo 表数据（键值游标分页）。
     * <p>
     * 使用 {@code WHERE keyField >= lastKey ORDER BY keyField} 替代 offset 分页，
     * 避免深度 offset 导致 MySQL 查询超时（如 items 表 offset>=2000 时触发 500 错误）。
     *
     * @param tableName 表名
     * @param fields    需要的字段，逗号分隔
     * @param keyField  游标字段名（如 "name"）
     * @param lastKey   上一批最后一个键值（首次传空字符串表示无下限）
     * @param limit     每批数量（最大 500）
     * @return 解析后的 JSON 响应根节点
     * @throws DataSyncException 网络错误或 API 返回错误时抛出
     */
    public JsonNode queryCargoTableByKey(String tableName, String fields,
                                          String keyField, String lastKey, int limit) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl).newBuilder()
            .addQueryParameter("action", "cargoquery")
            .addQueryParameter("format", "json")
            .addQueryParameter("tables", tableName)
            .addQueryParameter("fields", fields)
            .addQueryParameter("order_by", keyField + " ASC")
            .addQueryParameter("limit", String.valueOf(limit));

        if (lastKey != null && !lastKey.isEmpty()) {
            // Cargo where 参数接受 SQL WHERE 片段
            // 对键值中的特殊字符做 URL 编码，防止破坏查询
            String escapedKey = lastKey.replace("\"", "\\\"");
            urlBuilder.addQueryParameter("where",
                keyField + ">=\"" + escapedKey + "\"");
        }

        return executeCargoQuery(urlBuilder.build());
    }

    /**
     * 查询 Cargo 表数据（键值游标分页，严格大于模式）。
     * <p>
     * 与 {@link #queryCargoTableByKey} 类似，但使用 {@code >} 而非 {@code >=}，
     * 用于跳过重复键值的所有剩余行。
     */
    public JsonNode queryCargoTableByKeyGt(String tableName, String fields,
                                            String keyField, String lastKey, int limit) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl).newBuilder()
            .addQueryParameter("action", "cargoquery")
            .addQueryParameter("format", "json")
            .addQueryParameter("tables", tableName)
            .addQueryParameter("fields", fields)
            .addQueryParameter("order_by", keyField + " ASC")
            .addQueryParameter("limit", String.valueOf(limit));

        if (lastKey != null && !lastKey.isEmpty()) {
            String escapedKey = lastKey.replace("\"", "\\\"");
            urlBuilder.addQueryParameter("where",
                keyField + ">\"" + escapedKey + "\"");
        }

        return executeCargoQuery(urlBuilder.build());
    }

    /**
     * 执行 Cargo 查询请求的公共逻辑。
     */
    private JsonNode executeCargoQuery(HttpUrl url) {
        Request request = new Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build();

        String json = executeWithRetry(request);
        return parseAndCheckError(json);
    }

    /**
     * 获取 Cargo 表总记录数。
     * <p>
     * 通过 {@code fields=COUNT(*)} 查询实现。
     *
     * @param tableName 表名
     * @return 记录总数，查询失败返回 0
     */
    public int queryCargoTableCount(String tableName) {
        try {
            JsonNode root = queryCargoTable(tableName, "COUNT(*)", 0, 1);
            JsonNode cargoquery = root.path("cargoquery");
            if (cargoquery.isArray() && cargoquery.size() > 0) {
                JsonNode title = cargoquery.get(0).path("title");
                String countStr = title.path("COUNT(*)").asText();
                if (!countStr.isEmpty()) {
                    return Integer.parseInt(countStr);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to query table count for {}: {}", tableName, e.getMessage());
        }
        return 0;
    }

    /**
     * 获取 Wiki 页面的原始 wikitext 内容。
     *
     * @param pageTitle 页面标题（Wiki 页面名）
     * @return wikitext 原始文本
     * @throws DataSyncException 网络错误或页面不存在时抛出
     */
    public String queryPageContent(String pageTitle) {
        HttpUrl url = HttpUrl.parse(baseUrl).newBuilder()
            .addQueryParameter("action", "parse")
            .addQueryParameter("page", pageTitle)
            .addQueryParameter("prop", "wikitext")
            .addQueryParameter("format", "json")
            .build();

        Request request = new Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build();

        String json = executeWithRetry(request);
        JsonNode root = parseAndCheckError(json);

        JsonNode wikitext = root.at("/parse/wikitext");
        if (wikitext.isMissingNode()) {
            JsonNode error = root.path("error");
            String errorInfo = error.path("info").asText("Page not found or no wikitext available");
            throw new DataSyncException("Failed to get page content for '" + pageTitle + "': " + errorInfo);
        }

        return wikitext.path("*").asText();
    }

    // ==================== 内部方法 ====================

    /**
     * 执行 HTTP 请求，含限流和自动重试。
     * <p>
     * 重试策略：最多重试 3 次，指数退避（1s → 2s → 4s）。
     * 仅对 IOException 和 5xx 服务端错误进行重试，客户端错误（4xx）不重试。
     *
     * @param request HTTP 请求
     * @return 响应体字符串
     * @throws DataSyncException 所有重试耗尽后仍失败时抛出
     */
    private String executeWithRetry(Request request) {
        IOException lastException = null;

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            // 每次尝试（包括首次）都需要获取限流许可
            rateLimiter.acquire();

            try {
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body == null) {
                            throw new IOException("Empty response body");
                        }
                        String responseBody = body.string();

                        // 检测 Cloudflare 质询页面（HTTP 200 但返回 HTML 质询）
                        if (responseBody.contains(CF_CHALLENGE_MARKER)
                                || responseBody.contains(CF_CHALLENGE_TITLE)) {
                            throw new IOException("Cloudflare challenge page (treat as 429): "
                                    + responseBody.substring(0, Math.min(200, responseBody.length())));
                        }

                        // 快速验证 JSON 完整性：检测截断响应
                        if (responseBody.startsWith("{")) {
                            try {
                                objectMapper.readTree(responseBody);
                            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                                String msg = e.getMessage();
                                // 截断响应（Unexpected end-of-input）→ 可重试
                                if (msg != null && (msg.contains("Unexpected end-of-input")
                                        || msg.contains("EOF") || msg.contains("end-of-input"))) {
                                    throw new IOException("Truncated JSON response (likely connection cut): "
                                            + msg);
                                }
                                // 其他 JSON 错误 → 可能是 CF HTML 页面未被标记检测到
                                if (responseBody.contains("<html") || responseBody.contains("<!DOCTYPE")) {
                                    throw new IOException("HTML response disguised as JSON: "
                                            + responseBody.substring(0, Math.min(200, responseBody.length())));
                                }
                                // 真正的 JSON 格式错误 → 不重试，直接失败
                                throw new DataSyncException("Invalid JSON response: " + msg, e);
                            }
                        }

                        return responseBody;
                    }

                    // 429 限流或 5xx 服务端错误 → 可重试
                    if (response.code() == 429 || response.code() >= 500) {
                        String errorBody = response.body() != null ? response.body().string() : "";
                        throw new IOException("Server error " + response.code() + ": " + errorBody);
                    }

                    // 4xx 客户端错误 → 不重试
                    String errorBody = response.body() != null ? response.body().string() : "";
                    throw new DataSyncException(
                        "Wiki API returned " + response.code() + " for " + request.url().encodedPath()
                        + ": " + errorBody);
                }
            } catch (IOException e) {
                lastException = e;

                if (attempt < MAX_RETRIES) {
                    // 429 限流使用更长的退避延迟
                    boolean isRateLimit = e.getMessage() != null && e.getMessage().contains("429");
                    long delay = isRateLimit ? RATE_LIMIT_DELAYS_MS[attempt] : RETRY_DELAYS_MS[attempt];
                    log.warn("Request failed (attempt {}/{}), retrying in {}ms: {}",
                        attempt + 1, MAX_RETRIES, delay, e.getMessage());
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
            "Request failed after " + MAX_RETRIES + " retries: " + request.url(),
            lastException);
    }

    /**
     * 解析 JSON 并检查 API 级错误。
     *
     * @param json 原始 JSON 字符串
     * @return 解析后的根节点
     * @throws DataSyncException JSON 无效或 API 返回错误时抛出
     */
    private JsonNode parseAndCheckError(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);

            // 检查 MediaWiki API 级错误
            JsonNode errorNode = root.path("error");
            if (!errorNode.isMissingNode()) {
                String code = errorNode.path("code").asText("unknown");
                String info = errorNode.path("info").asText("Unknown error");
                throw new DataSyncException("Wiki API error [" + code + "]: " + info);
            }

            return root;
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new DataSyncException("Failed to parse Wiki API response", e);
        }
    }


    public static void main(String[] args) {
        //TODO yzy 测试拉取数据
    }
}
