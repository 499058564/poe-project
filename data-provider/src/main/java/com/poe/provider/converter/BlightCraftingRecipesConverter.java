package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.BlightCraftingRecipes;

public class BlightCraftingRecipesConverter implements DataConverter<BlightCraftingRecipes> {

    @Override
    public BlightCraftingRecipes convert(JsonNode row) {
        BlightCraftingRecipes b = new BlightCraftingRecipes();
        b.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        b.setPageName(row.path("_pageName").asText());
        b.setRecipeId(row.path("id").asText());
        b.setModifierId(row.path("modifier_id").asText());
        b.setPassiveId(row.path("passive_id").asText());
        b.setType(row.path("type").asText());
        return b;
    }
}
