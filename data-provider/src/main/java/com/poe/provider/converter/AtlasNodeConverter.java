package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.AtlasNode;

/**
 * 将 Wiki Cargo {@code atlas_nodes} 表行转换为 {@link AtlasNode} 模型。
 * <p>
 * Cargo 字段：area_id, connections, div_cards, id, is_off_atlas,
 * region_connections_0, region_connections_1, region_connections_2,
 * region_connections_3, region_connections_4, region_id, region_minimum,
 * series_id, tier_0, tier_1, tier_2, tier_3, tier_4。
 */
public class AtlasNodeConverter implements DataConverter<AtlasNode> {

    /**
     * 将 Cargo 单行 JSON 转换为 AtlasNode 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 异界图鉴节点实体，字段缺失时使用默认值
     */
    @Override
    public AtlasNode convert(JsonNode row) {
        AtlasNode an = new AtlasNode();
        an.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        an.setPageName(row.path("_pageName").asText());
        an.setAreaId(row.path("area_id").asText());
        an.setConnections(ItemConverter.nullableText(row, "connections"));
        an.setDivCards(ItemConverter.nullableText(row, "div_cards"));
        an.setId(row.path("id").asText());
        an.setOffAtlas(ItemConverter.parseBooleanSafe(row, "is_off_atlas"));
        an.setRegionConnections0(ItemConverter.nullableText(row, "region_connections_0"));
        an.setRegionConnections1(ItemConverter.nullableText(row, "region_connections_1"));
        an.setRegionConnections2(ItemConverter.nullableText(row, "region_connections_2"));
        an.setRegionConnections3(ItemConverter.nullableText(row, "region_connections_3"));
        an.setRegionConnections4(ItemConverter.nullableText(row, "region_connections_4"));
        an.setRegionId(row.path("region_id").asText());
        an.setRegionMinimum(ItemConverter.parseIntSafe(row, "region_minimum"));
        an.setSeriesId(ItemConverter.parseIntSafe(row, "series_id"));
        an.setTier0(ItemConverter.parseIntSafe(row, "tier_0"));
        an.setTier1(ItemConverter.parseIntSafe(row, "tier_1"));
        an.setTier2(ItemConverter.parseIntSafe(row, "tier_2"));
        an.setTier3(ItemConverter.parseIntSafe(row, "tier_3"));
        an.setTier4(ItemConverter.parseIntSafe(row, "tier_4"));
        return an;
    }
}
