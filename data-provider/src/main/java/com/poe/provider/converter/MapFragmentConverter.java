package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.MapFragment;

/**
 * 将 Wiki Cargo {@code map_fragments} 子表行转换为 {@link MapFragment} 模型。
 */
public class MapFragmentConverter implements DataConverter<MapFragment> {

    @Override
    public MapFragment convert(JsonNode row) {
        MapFragment mf = new MapFragment();
        mf.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        mf.setPageName(row.path("_pageName").asText());
        mf.setMapFragmentLimit(ItemConverter.parseIntSafe(row, "map_fragment_limit"));
        return mf;
    }
}
