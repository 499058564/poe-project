package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.ItemBuff;

/**
 * 将 Wiki Cargo {@code item_buffs} 子表行转换为 {@link ItemBuff} 模型。
 * <p>
 * Cargo 字段：{@code buff_values} (逗号分隔), {@code icon},
 * {@code id}→buffId, {@code stat_text}。
 * 通过 _pageID 关联 base_items 表。
 */
public class ItemBuffConverter implements DataConverter<ItemBuff> {

    @Override
    public ItemBuff convert(JsonNode row) {
        ItemBuff b = new ItemBuff();
        b.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        b.setPageName(row.path("_pageName").asText());
        b.setBuffValues(ItemConverter.toJsonOrNull(row.path("buff_values")));
        b.setIcon(ItemConverter.nullableText(row, "icon"));
        b.setBuffId(row.path("id").asText());
        b.setStatText(ItemConverter.nullableText(row, "stat_text"));
        return b;
    }
}
