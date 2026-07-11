package com.poe.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.util.concurrent.RateLimiter;
import okhttp3.OkHttpClient;

import java.time.Duration;

/**
 * poe.ninja 公共 REST API 客户端。
 *
 * <h3>数据范围</h3>
 * 提供当前赛季的经济数据：物品价格、流行度、通货汇率等。
 * 弥补 Wiki 数据缺少实时价格信息的不足。
 *
 * <h3>使用方式</h3>
 * <pre>{@code
 * PoeNinjaClient client = new PoeNinjaClient();
 * JsonNode items = client.getItemOverview("Settlers", "Currency");
 * JsonNode currencies = client.getCurrencyOverview("Settlers", "Currency");
 * }</pre>
 *
 * <h3>API 约定</h3>
 * <ul>
 *   <li>无需认证，公开 REST API</li>
 *   <li>社区约定限流 ~1 req/s</li>
 *   <li>数据按 league 名分区</li>
 *   <li>连接超时 15 秒，读取超时 30 秒</li>
 * </ul>
 *
 * <h3>支持的 ItemOverview 类型</h3>
 * Currency, Fragment, Scarab, Fossil, Resonator, Essence,
 * DivinationCard, SkillGem, BaseType, UniqueMap, UniqueJewel,
 * UniqueFlask, UniqueWeapon, UniqueArmour, UniqueAccessory,
 * Beast, Vial, Invitation, Artifact, Omen, Tattoo, Memory,
 * ClusterJewel, SanctumRelic, Sentinel, AllflameEmber
 *
 * @see <a href="https://poe.ninja/api/data">poe.ninja API 文档</a>
 */
public class PoeNinjaClient extends AbstractApiClient {

    /** poe.ninja API 基础地址。 */
    static final String POE_NINJA_API = "https://poe.ninja/api/data";

    /** 社区约定限流速率。 */
    private static final double RATE_LIMIT = 1.0;

    /**
     * 使用默认超时配置创建客户端。
     * <ul>
     *   <li>连接超时：15 秒</li>
     *   <li>读取超时：30 秒</li>
     *   <li>限流速率：1 req/s</li>
     * </ul>
     */
    public PoeNinjaClient() {
        super(POE_NINJA_API, RATE_LIMIT,
              Duration.ofSeconds(15), Duration.ofSeconds(30));
    }

    /**
     * 测试用构造器，注入 MockWebServer。
     */
    PoeNinjaClient(OkHttpClient httpClient, RateLimiter rateLimiter, String baseUrl) {
        super(httpClient, rateLimiter, baseUrl);
    }

    // ==================== 公开方法 ====================

    /**
     * 获取指定赛季的通货汇率概览。
     *
     * @param league 赛季名，如 "Settlers"、"Affliction"
     * @param type   通货类型，如 "Currency"、"Fragment"
     * @return 包含 {@code lines[]} 数组的 JSON，每项的字段包括
     *         {@code currencyTypeName, chaosEquivalent, receive} 等
     */
    public JsonNode getCurrencyOverview(String league, String type) {
        String path = String.format(
            "/CurrencyOverview?league=%s&type=%s&language=en",
            league, type);
        return executeGet(path);
    }

    /**
     * 获取指定赛季的物品价格概览。
     *
     * @param league   赛季名
     * @param itemType 物品类型，如 "UniqueWeapon"、"DivinationCard"、"Scarab"
     * @return 包含 {@code lines[]} 数组的 JSON，每项的字段包括
     *         {@code name, chaosValue, exaltedValue, divineValue, listingCount} 等
     */
    public JsonNode getItemOverview(String league, String itemType) {
        String path = String.format(
            "/ItemOverview?league=%s&type=%s&language=en",
            league, itemType);
        return executeGet(path);
    }
}
