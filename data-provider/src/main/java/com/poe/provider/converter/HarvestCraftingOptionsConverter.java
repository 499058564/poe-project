package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.HarvestCraftingOptions;

public class HarvestCraftingOptionsConverter implements DataConverter<HarvestCraftingOptions> {

    @Override
    public HarvestCraftingOptions convert(JsonNode row) {
        HarvestCraftingOptions h = new HarvestCraftingOptions();
        h.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        h.setPageName(row.path("_pageName").asText());
        h.setOptionId(row.path("id").asText());
        h.setCostPrimal(ItemConverter.parseIntSafe(row, "cost_primal"));
        h.setCostRancour(ItemConverter.parseIntSafe(row, "cost_rancour"));
        h.setCostSacred(ItemConverter.parseIntSafe(row, "cost_sacred"));
        h.setCostVivid(ItemConverter.parseIntSafe(row, "cost_vivid"));
        h.setCostWild(ItemConverter.parseIntSafe(row, "cost_wild"));
        h.setEffect(row.path("effect").asText());
        h.setEffectHtml(row.path("effect_html").asText());
        h.setOrdinal(ItemConverter.parseIntSafe(row, "ordinal"));
        return h;
    }
}
