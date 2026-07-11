package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.SynthesisGlobalMods;

public class SynthesisGlobalModsConverter implements DataConverter<SynthesisGlobalMods> {

    @Override
    public SynthesisGlobalMods convert(JsonNode row) {
        SynthesisGlobalMods s = new SynthesisGlobalMods();
        s.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        s.setPageName(row.path("_pageName").asText());
        s.setMaxLevel(ItemConverter.parseIntSafe(row, "max_level"));
        s.setMinLevel(ItemConverter.parseIntSafe(row, "min_level"));
        s.setModId(row.path("mod_id").asText());
        s.setWeight(ItemConverter.parseIntSafe(row, "weight"));
        return s;
    }
}
