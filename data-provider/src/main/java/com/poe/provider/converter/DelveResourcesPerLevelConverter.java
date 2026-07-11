package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.DelveResourcesPerLevel;

public class DelveResourcesPerLevelConverter implements DataConverter<DelveResourcesPerLevel> {

    @Override
    public DelveResourcesPerLevel convert(JsonNode row) {
        DelveResourcesPerLevel d = new DelveResourcesPerLevel();
        d.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        d.setPageName(row.path("_pageName").asText());
        d.setAreaLevel(ItemConverter.parseIntSafe(row, "area_level"));
        d.setSulphite(ItemConverter.parseIntSafe(row, "sulphite"));
        return d;
    }
}
