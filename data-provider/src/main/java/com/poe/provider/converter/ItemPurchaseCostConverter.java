package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.ItemPurchaseCost;

/**
 * 将 Wiki Cargo {@code item_purchase_costs} 子表行转换为 {@link ItemPurchaseCost} 模型。
 * <p>
 * Cargo 字段：{@code amount}, {@code name}→currencyName, {@code rarity}。
 * 通过 _pageID 关联 base_items 表。
 */
public class ItemPurchaseCostConverter implements DataConverter<ItemPurchaseCost> {

    @Override
    public ItemPurchaseCost convert(JsonNode row) {
        ItemPurchaseCost c = new ItemPurchaseCost();
        c.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        c.setPageName(row.path("_pageName").asText());
        c.setAmount(ItemConverter.parseIntSafe(row, "amount"));
        c.setCurrencyName(row.path("name").asText());
        c.setRarity(ItemConverter.nullableText(row, "rarity"));
        return c;
    }
}
