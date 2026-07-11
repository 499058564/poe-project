package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.ItemMod;

/**
 * 将 Wiki Cargo {@code item_mods} 子表行转换为 {@link ItemMod} 模型。
 * <p>
 * Cargo 字段：{@code id}→modId, {@code is_explicit}, {@code is_implicit},
 * {@code is_map_fragment_bonus}, {@code is_random}, {@code text}。
 * 通过 _pageID 关联 base_items 表。
 */
public class ItemModConverter implements DataConverter<ItemMod> {

    @Override
    public ItemMod convert(JsonNode row) {
        ItemMod m = new ItemMod();
        m.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        m.setPageName(row.path("_pageName").asText());
        m.setModId(row.path("id").asText());
        m.setExplicit(ItemConverter.parseBooleanSafe(row, "is_explicit"));
        m.setImplicit(ItemConverter.parseBooleanSafe(row, "is_implicit"));
        m.setMapFragmentBonus(ItemConverter.parseBooleanSafe(row, "is_map_fragment_bonus"));
        m.setRandom(ItemConverter.parseBooleanSafe(row, "is_random"));
        m.setText(ItemConverter.nullableText(row, "text"));
        return m;
    }
}
