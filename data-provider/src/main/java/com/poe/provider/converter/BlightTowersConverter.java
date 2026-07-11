package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.BlightTowers;

public class BlightTowersConverter implements DataConverter<BlightTowers> {

    @Override
    public BlightTowers convert(JsonNode row) {
        BlightTowers b = new BlightTowers();
        b.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        b.setPageName(row.path("_pageName").asText());
        b.setCost(ItemConverter.parseIntSafe(row, "cost"));
        b.setDescription(ItemConverter.nullableText(row, "description"));
        b.setIcon(row.path("icon").asText());
        b.setTowerId(row.path("id").asText());
        b.setName(row.path("name").asText());
        b.setRadius(ItemConverter.parseIntSafe(row, "radius"));
        b.setTier(row.path("tier").asText());
        return b;
    }
}
