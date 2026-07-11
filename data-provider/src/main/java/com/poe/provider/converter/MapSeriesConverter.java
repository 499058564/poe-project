package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.MapSeries;

/**
 * 将 Wiki Cargo {@code map_series} 子表行转换为 {@link MapSeries} 模型。
 * <p>
 * Cargo 字段：id (=_pageName 下的唯一标识), name, ordinal。
 */
public class MapSeriesConverter implements DataConverter<MapSeries> {

    /**
     * 将 Cargo 单行 JSON 转换为 MapSeries 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 地图系列实体，字段缺失时使用默认值（0 / null）
     */
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
