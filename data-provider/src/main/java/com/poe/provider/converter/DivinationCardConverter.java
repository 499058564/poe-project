package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.DivinationCard;

/**
 * 将 Wiki Cargo {@code divination_cards} 子表行转换为 {@link DivinationCard} 模型。
 * <p>
 * Cargo 字段：card_art (Page), card_background (List of Integer，逗号分隔)。
 */
public class DivinationCardConverter implements DataConverter<DivinationCard> {

    /**
     * 将 Cargo 单行 JSON 转换为 DivinationCard 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 命运卡实体，card_art / card_background 缺失时返回 null
     */
    @Override
    public DivinationCard convert(JsonNode row) {
        DivinationCard dc = new DivinationCard();
        dc.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        dc.setPageName(row.path("_pageName").asText());
        dc.setCardArt(ItemConverter.nullableText(row, "card_art"));
        // card_background 在 Cargo 中为 List，API 返回逗号拼接字符串
        dc.setCardBackground(ItemConverter.nullableText(row, "card_background"));
        return dc;
    }
}
