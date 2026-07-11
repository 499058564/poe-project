package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.BlightCraftingRecipesItems;

public class BlightCraftingRecipesItemsConverter implements DataConverter<BlightCraftingRecipesItems> {

    @Override
    public BlightCraftingRecipesItems convert(JsonNode row) {
        BlightCraftingRecipesItems b = new BlightCraftingRecipesItems();
        b.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        b.setPageName(row.path("_pageName").asText());
        b.setItemId(row.path("item_id").asText());
        b.setOrdinal(ItemConverter.parseIntSafe(row, "ordinal"));
        b.setRecipeId(row.path("recipe_id").asText());
        return b;
    }
}
