package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.ModSpawnWeight;

/**
 * 将 Wiki Cargo {@code mod_spawn_weights} 子表行转换为 {@link ModSpawnWeight} 模型。
 * <p>
 * Cargo 字段：{@code ordinal}, {@code tag}, {@code value}。
 * 通过 _pageID 关联 mods 表。
 */
public class ModSpawnWeightConverter implements DataConverter<ModSpawnWeight> {

    @Override
    public ModSpawnWeight convert(JsonNode row) {
        ModSpawnWeight w = new ModSpawnWeight();
        w.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        w.setPageName(row.path("_pageName").asText());
        w.setOrdinal(ItemConverter.parseIntSafe(row, "ordinal"));
        w.setTag(row.path("tag").asText());
        w.setValue(ItemConverter.parseIntSafe(row, "value"));
        return w;
    }
}
