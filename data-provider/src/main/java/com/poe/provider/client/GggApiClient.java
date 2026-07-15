package com.poe.provider.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.util.concurrent.RateLimiter;
import okhttp3.OkHttpClient;

import java.time.Duration;

/**
 * Grinding Gear Games 官方 API 客户端。
 *
 * <h3>数据范围</h3>
 * <ul>
 *   <li><b>赛季列表</b>（公开）：当前及历史赛季名、状态</li>
 *   <li><b>天梯数据</b>（公开）：指定赛季前 N 名玩家</li>
 *   <li><b>账号信息</b>（OAuth2）：用户 Profile（需授权）</li>
 * </ul>
 *
 * <h3>核心价值</h3>
 * 提供权威的赛季版本检测——Wiki 数据版本与 GGG 当前赛季比对，
 * 判断数据是否过期。
 *
 * <h3>使用方式</h3>
 * <pre>{@code
 * GggApiClient client = new GggApiClient();
 * JsonNode leagues = client.getLeagues();
 * // 查找当前标准赛季
 * String currentLeague = leagues.get(0).get("id").asText();
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>⚠️ 不提供游戏数据（物品、技能、天赋等），仅提供元数据</li>
 *   <li>OAuth2 端点需用户授权，当前版本预留骨架</li>
 *   <li>限流：~2 req/s（GGG 官方建议）</li>
 * </ul>
 *
 * @see <a href="https://www.pathofexile.com/developer/docs">GGG Developer API</a>
 */
public class GggApiClient extends AbstractApiClient {

    /** GGG API 基础地址。 */
    static final String GGG_API = "https://api.pathofexile.com";

    /** GGG 官方建议限流速率。 */
    private static final double RATE_LIMIT = 2.0;

    /**
     * 使用默认超时配置创建客户端。
     * <ul>
     *   <li>连接超时：15 秒</li>
     *   <li>读取超时：30 秒</li>
     *   <li>限流速率：2 req/s</li>
     * </ul>
     */
    public GggApiClient() {
        super(GGG_API, RATE_LIMIT,
              Duration.ofSeconds(15), Duration.ofSeconds(30));
    }

    /**
     * 测试用构造器，注入 MockWebServer。
     */
    GggApiClient(OkHttpClient httpClient, RateLimiter rateLimiter, String baseUrl) {
        super(httpClient, rateLimiter, baseUrl);
    }

    // ==================== 公开端点（无需认证） ====================

    /**
     * 获取当前及历史赛季列表。
     *
     * @return 包含赛季对象数组的 JSON，每项的字段包括
     *         {@code id, realm, description, registerAt, startAt, endAt} 等
     */
    public JsonNode getLeagues() {
        return executeGet("/public-stash-tabs");
    }

    /**
     * 获取指定赛季的天梯排名。
     *
     * @param league 赛季名，如 "Settlers"
     * @param limit  返回条目数，上限 200
     * @return 包含 {@code entries[]} 数组的 JSON，每项含
     *         {@code rank, character.name, character.level, account.name} 等
     */
    public JsonNode getLadder(String league, int limit) {
        String path = String.format("/ladders/%s?limit=%d", league, Math.min(limit, 200));
        return executeGet(path);
    }

    // ==================== OAuth2 端点（预留骨架） ====================

    /**
     * 获取已授权用户的账号信息（需 OAuth2 access token）。
     *
     * <p>当前为预留骨架，需要先完成 OAuth2 授权流程获取 token。
     *
     * @param accessToken OAuth2 Bearer token
     * @return 包含 {@code uuid, name, realm, guild} 等的 JSON
     */
    public JsonNode getProfile(String accessToken) {
        // TODO: 实现 OAuth2 授权流程后启用
        throw new UnsupportedOperationException(
            "getProfile requires OAuth2 — not yet implemented");
    }
}
