package com.poe.core.model;

import com.poe.cache.model.ItemSummary;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 物品完整详情，包含摘要信息和扩展属性。
 * <p>
 * requirements 从 JSON 字符串解析为 {@code Map<String, Integer>}，键为属性名（str/dex/int），
 * 值为属性需求值。
 */
public class ItemDetail {

    /** 基础摘要信息 */
    private final ItemSummary summary;

    /** 基底词缀（implicit modifiers），如 "+20 to maximum Life" */
    private final List<String> implicits;

    /** 属性需求，如 {str: 100, dex: 50} */
    private final Map<String, Integer> requirements;

    /** 显式词缀行，包含文本和数值范围 */
    private final List<ModLine> explicitMods;

    /** 装备背景文字 */
    private final String flavourText;

    /** PoE Wiki 页面 URL */
    private final String wikiUrl;

    /**
     * @param summary      物品摘要信息
     * @param implicits    基底词缀列表
     * @param requirements 属性需求映射（str/dex/int → 值）
     * @param explicitMods 显式词缀行
     * @param flavourText  装备背景文字
     * @param wikiUrl      PoE Wiki 页面 URL
     */
    public ItemDetail(ItemSummary summary, List<String> implicits,
                      Map<String, Integer> requirements, List<ModLine> explicitMods,
                      String flavourText, String wikiUrl) {
        this.summary = summary;
        this.implicits = implicits != null ? Collections.unmodifiableList(implicits) : Collections.emptyList();
        this.requirements = requirements != null ? Collections.unmodifiableMap(requirements) : Collections.emptyMap();
        this.explicitMods = explicitMods != null ? Collections.unmodifiableList(explicitMods) : Collections.emptyList();
        this.flavourText = flavourText;
        this.wikiUrl = wikiUrl;
    }

    public ItemSummary getSummary() { return summary; }

    public List<String> getImplicits() { return implicits; }

    public Map<String, Integer> getRequirements() { return requirements; }

    public List<ModLine> getExplicitMods() { return explicitMods; }

    public String getFlavourText() { return flavourText; }

    public String getWikiUrl() { return wikiUrl; }
}
