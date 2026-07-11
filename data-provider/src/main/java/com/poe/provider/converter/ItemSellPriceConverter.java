package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.ItemSellPrice;

/**
 * 将 Wiki Cargo {@code item_sell_prices} 子表行转换为 {@link ItemSellPrice} 模型。
 * <p>
 * Cargo 字段：{@code amount}, {@code name}→currencyName。
 * 通过 _pageID 关联 base_items 表。
 */
public class ItemSellPriceConverter implements DataConverter<ItemSellPrice> {

    @Override
    public ItemSellPrice convert(JsonNode row) {
        ItemSellPrice p = new ItemSellPrice();
        p.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        p.setPageName(row.path("_pageName").asText());
        p.setAmount(ItemConverter.parseIntSafe(row, "amount"));
        p.setCurrencyName(row.path("name").asText());
        return p;
    }
}
