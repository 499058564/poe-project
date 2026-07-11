package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.BestiaryRecipeComponents;

public class BestiaryRecipeComponentsConverter implements DataConverter<BestiaryRecipeComponents> {

    @Override
    public BestiaryRecipeComponents convert(JsonNode row) {
        BestiaryRecipeComponents b = new BestiaryRecipeComponents();
        b.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        b.setPageName(row.path("_pageName").asText());
        b.setAmount(ItemConverter.parseIntSafe(row, "amount"));
        b.setComponentId(row.path("component_id").asText());
        b.setRecipeId(row.path("recipe_id").asText());
        return b;
    }
}
