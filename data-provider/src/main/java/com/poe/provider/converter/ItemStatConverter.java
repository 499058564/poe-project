package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.ItemStat;

/**
 * 将 Wiki Cargo {@code item_stats} 子表行转换为 {@link ItemStat} 模型。
 * <p>
 * Cargo 字段：{@code avg}, {@code id}→statId, {@code max}→maxValue,
 * {@code min}→minValue, {@code mod_id}。
 * 通过 _pageID 关联 base_items 表。
 */
public class ItemStatConverter implements DataConverter<ItemStat> {

    @Override
    public ItemStat convert(JsonNode row) {
        ItemStat s = new ItemStat();
        s.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        s.setPageName(row.path("_pageName").asText());
        s.setAvg(ItemConverter.parseIntSafe(row, "avg"));
        s.setStatId(row.path("id").asText());
        s.setMaxValue(ItemConverter.parseIntSafe(row, "max"));
        s.setMinValue(ItemConverter.parseIntSafe(row, "min"));
        s.setModId(row.path("mod_id").asText());
        return s;
    }
}
