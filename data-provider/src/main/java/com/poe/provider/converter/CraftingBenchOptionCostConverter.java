package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.CraftingBenchOptionCost;

/**
 * 将 Wiki Cargo {@code crafting_bench_options_costs} 子表行转换为 {@link CraftingBenchOptionCost} 模型。
 * <p>
 * Cargo 字段：{@code amount}, {@code name}→currencyName, {@code option_id}。
 * 通过 option_id 关联 crafting_bench_options.id。
 */
public class CraftingBenchOptionCostConverter implements DataConverter<CraftingBenchOptionCost> {

    @Override
    public CraftingBenchOptionCost convert(JsonNode row) {
        CraftingBenchOptionCost c = new CraftingBenchOptionCost();
        c.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        c.setPageName(row.path("_pageName").asText());
        c.setOptionId(ItemConverter.parseIntSafe(row, "option_id"));
        c.setAmount(ItemConverter.parseIntSafe(row, "amount"));
        c.setCurrencyName(row.path("name").asText());
        return c;
    }
}
