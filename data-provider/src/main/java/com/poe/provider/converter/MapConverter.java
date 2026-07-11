package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.GameMap;

/**
 * 将 Wiki Cargo {@code maps} 子表行转换为 {@link GameMap} 模型。
 */
public class MapConverter implements DataConverter<GameMap> {

    /**
     * 将 Cargo 单行 JSON 转换为 GameMap 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 地图实体，字段缺失时使用默认值（0 / null）
     */
    @Override
    public GameMap convert(JsonNode row) {
        GameMap m = new GameMap();
        m.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        m.setPageName(row.path("_pageName").asText());
        m.setAreaId(ItemConverter.nullableText(row, "area_id"));
        m.setAreaLevel(ItemConverter.parseIntSafe(row, "area_level"));
        m.setGuildCharacter(ItemConverter.nullableText(row, "guild_character"));
        m.setSeries(ItemConverter.nullableText(row, "series"));
        m.setTier(ItemConverter.parseIntSafe(row, "tier"));
        m.setUniqueAreaId(ItemConverter.nullableText(row, "unique_area_id"));
        m.setUniqueAreaLevel(ItemConverter.parseIntSafe(row, "unique_area_level"));
        m.setUniqueGuildCharacter(ItemConverter.nullableText(row, "unique_guild_character"));
        return m;
    }
}
