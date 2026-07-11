package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.MapSeries;

/**
 * 将 Wiki Cargo {@code map_series} 子表行转换为 {@link MapSeries} 模型。
 * <p>
 * Cargo 字段：id (=_pageName 下的唯一标识), name, ordinal。
 */
public class MapSeriesConverter implements DataConverter<MapSeries> {

    @Override
    public MapSeries convert(JsonNode row) {
        MapSeries ms = new MapSeries();
        ms.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        ms.setPageName(row.path("_pageName").asText());
        ms.setSeriesId(ItemConverter.nullableText(row, "id"));
        ms.setName(ItemConverter.nullableText(row, "name"));
        ms.setOrdinal(ItemConverter.parseIntSafe(row, "ordinal"));
        return ms;
    }
}
