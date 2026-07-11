package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Shield;

/**
 * 将 Wiki Cargo {@code shields} 子表行转换为 {@link Shield} 模型。
 */
public class ShieldConverter implements DataConverter<Shield> {

    @Override
    public Shield convert(JsonNode row) {
        Shield s = new Shield();
        s.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        s.setPageName(row.path("_pageName").asText());
        s.setBlock(ItemConverter.parseIntSafe(row, "block"));
        return s;
    }
}
