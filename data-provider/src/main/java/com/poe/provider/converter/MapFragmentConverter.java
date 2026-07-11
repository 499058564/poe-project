package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.MapFragment;

/**
 * 将 Wiki Cargo {@code map_fragments} 子表行转换为 {@link MapFragment} 模型。
 */
public class MapFragmentConverter implements DataConverter<MapFragment> {

    /**
     * 将 Cargo 单行 JSON 转换为 MapFragment 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 地图碎片实体，字段缺失时使用默认值 0
     */
    @Override
    public MapFragment convert(JsonNode row) {
        MapFragment mf = new MapFragment();
        mf.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        mf.setPageName(row.path("_pageName").asText());
        mf.setMapFragmentLimit(ItemConverter.parseIntSafe(row, "map_fragment_limit"));
        return mf;
    }
}
