package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.BestiaryRecipes;

public class BestiaryRecipesConverter implements DataConverter<BestiaryRecipes> {

    @Override
    public BestiaryRecipes convert(JsonNode row) {
        BestiaryRecipes b = new BestiaryRecipes();
        b.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        b.setPageName(row.path("_pageName").asText());
        b.setRecipeId(row.path("id").asText());
        b.setGameMode(row.path("game_mode").asText());
        b.setHeader(row.path("header").asText());
        b.setNotes(row.path("notes").asText());
        b.setSubheader(row.path("subheader").asText());
        return b;
    }
}
