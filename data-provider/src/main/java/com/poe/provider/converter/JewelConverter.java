package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Jewel;

/**
 * 将 Wiki Cargo {@code jewels} 子表行转换为 {@link Jewel} 模型。
 */
public class JewelConverter implements DataConverter<Jewel> {

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
