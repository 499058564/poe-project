package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.ModStat;

/**
 * 将 Wiki Cargo {@code mod_stats} 子表行转换为 {@link ModStat} 模型。
 * <p>
 * Cargo 字段：{@code id}→statId, {@code min}→minValue, {@code max}→maxValue。
 * 通过 _pageID 关联 mods 表。
 */
public class ModStatConverter implements DataConverter<ModStat> {

    @Override
    public ModStat convert(JsonNode row) {
        ModStat s = new ModStat();
        s.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        s.setPageName(row.path("_pageName").asText());
        s.setStatId(row.path("id").asText());
        s.setMinValue(ItemConverter.parseIntSafe(row, "min"));
        s.setMaxValue(ItemConverter.parseIntSafe(row, "max"));
        return s;
    }
}
