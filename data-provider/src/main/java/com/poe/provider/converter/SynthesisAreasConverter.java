package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.SynthesisAreas;

public class SynthesisAreasConverter implements DataConverter<SynthesisAreas> {

    @Override
    public SynthesisAreas convert(JsonNode row) {
        SynthesisAreas s = new SynthesisAreas();
        s.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        s.setPageName(row.path("_pageName").asText());
        s.setAreaId(row.path("id").asText());
        s.setMaxLevel(ItemConverter.parseIntSafe(row, "max_level"));
        s.setMinLevel(ItemConverter.parseIntSafe(row, "min_level"));
        s.setName(row.path("name").asText());
        s.setSize(ItemConverter.parseIntSafe(row, "size"));
        s.setWeight(ItemConverter.parseIntSafe(row, "weight"));
        return s;
    }
}
