package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Jewel;

/**
 * 将 Wiki Cargo {@code jewels} 子表行转换为 {@link Jewel} 模型。
 */
public class JewelConverter implements DataConverter<Jewel> {

    /**
     * 将 Cargo 单行 JSON 转换为 Jewel 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 珠宝实体，jewel_limit / radius_html 缺失时返回 null
     */
    @Override
    public Jewel convert(JsonNode row) {
        Jewel j = new Jewel();
        j.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        j.setPageName(row.path("_pageName").asText());
        j.setJewelLimit(ItemConverter.nullableText(row, "jewel_limit"));
        j.setRadiusHtml(ItemConverter.nullableText(row, "radius_html"));
        return j;
    }
}
