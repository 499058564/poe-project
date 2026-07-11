package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Amulet;

/**
 * 将 Wiki Cargo {@code amulets} 子表行转换为 {@link Amulet} 模型。
 */
public class AmuletConverter implements DataConverter<Amulet> {

    /**
     * 将 Cargo 单行 JSON 转换为 Amulet 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 护身符实体，字段缺失时使用默认值（0 / false）
     */
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
