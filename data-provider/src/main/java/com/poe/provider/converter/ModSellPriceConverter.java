package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.ModSellPrice;

/**
 * 将 Wiki Cargo {@code mod_sell_prices} 子表行转换为 {@link ModSellPrice} 模型。
 * <p>
 * Cargo 字段：{@code amount}, {@code name}→currencyName。
 * 通过 _pageID 关联 mods 表。
 */
public class ModSellPriceConverter implements DataConverter<ModSellPrice> {

    @Override
    public ModSellPrice convert(JsonNode row) {
        ModSellPrice p = new ModSellPrice();
        p.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        p.setPageName(row.path("_pageName").asText());
        p.setAmount(ItemConverter.parseIntSafe(row, "amount"));
        p.setCurrencyName(row.path("name").asText());
        return p;
    }
}
