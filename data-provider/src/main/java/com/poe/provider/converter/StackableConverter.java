package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Stackable;

/**
 * 将 Wiki Cargo {@code stackables} 子表行转换为 {@link Stackable} 模型。
 */
public class StackableConverter implements DataConverter<Stackable> {

    @Override
    public Stackable convert(JsonNode row) {
        Stackable s = new Stackable();
        s.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        s.setPageName(row.path("_pageName").asText());
        s.setStackSize(ItemConverter.parseIntSafe(row, "stack_size"));
        s.setStackSizeCurrencyTab(ItemConverter.parseIntSafe(row, "stack_size_currency_tab"));
        return s;
    }
}
