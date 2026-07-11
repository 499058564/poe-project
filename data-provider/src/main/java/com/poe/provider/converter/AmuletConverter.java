package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Amulet;

/**
 * 将 Wiki Cargo {@code amulets} 子表行转换为 {@link Amulet} 模型。
 */
public class AmuletConverter implements DataConverter<Amulet> {

    @Override
    public Amulet convert(JsonNode row) {
        Amulet a = new Amulet();
        a.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        a.setPageName(row.path("_pageName").asText());
        a.setTalisman(ItemConverter.parseBooleanSafe(row, "is_talisman"));
        a.setTalismanTier(ItemConverter.parseIntSafe(row, "talisman_tier"));
        return a;
    }
}
