package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Stackable;

/**
 * 将 Wiki Cargo {@code stackables} 子表行转换为 {@link Stackable} 模型。
 */
public class StackableConverter implements DataConverter<Stackable> {

    /**
     * 将 Cargo 单行 JSON 转换为 Stackable 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 可堆叠物品实体，字段缺失时使用默认值 0
     */
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
