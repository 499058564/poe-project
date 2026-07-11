package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.FossilWeight;

/**
 * 将 Wiki Cargo {@code fossil_weights} 子表行转换为 {@link FossilWeight} 模型。
 * <p>
 * Cargo 字段：{@code base_item_id}, {@code ordinal}, {@code tag},
 * {@code type}→weightType, {@code weight}。
 * 通过 base_item_id 关联 fossils 表。
 */
public class FossilWeightConverter implements DataConverter<FossilWeight> {

    @Override
    public FossilWeight convert(JsonNode row) {
        FossilWeight w = new FossilWeight();
        w.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        w.setPageName(row.path("_pageName").asText());
        w.setBaseItemId(ItemConverter.nullableText(row, "base_item_id"));
        w.setOrdinal(ItemConverter.parseIntSafe(row, "ordinal"));
        w.setTag(row.path("tag").asText());
        w.setWeightType(row.path("type").asText());
        w.setWeight(ItemConverter.parseIntSafe(row, "weight"));
        return w;
    }
}
