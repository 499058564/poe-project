package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Essence;

/**
 * 将 Wiki Cargo {@code essences} 子表行转换为 {@link Essence} 模型。
 * <p>
 * Cargo 字段：{@code category}, {@code level}, {@code level_restriction}, {@code type}。
 * 名称来自 _pageName。通过 _pageID 关联 base_items 表。
 */
public class EssenceConverter implements DataConverter<Essence> {

    @Override
    public Essence convert(JsonNode row) {
        Essence e = new Essence();
        e.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        e.setPageName(row.path("_pageName").asText());
        e.setCategory(ItemConverter.nullableText(row, "category"));
        e.setLevel(ItemConverter.parseIntSafe(row, "level"));
        e.setLevelRestriction(ItemConverter.parseIntSafe(row, "level_restriction"));
        e.setType(ItemConverter.parseIntSafe(row, "type"));
        return e;
    }
}
