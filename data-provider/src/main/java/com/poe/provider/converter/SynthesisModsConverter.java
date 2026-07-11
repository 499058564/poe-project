package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.SynthesisMods;

public class SynthesisModsConverter implements DataConverter<SynthesisMods> {

    @Override
    public SynthesisMods convert(JsonNode row) {
        SynthesisMods s = new SynthesisMods();
        s.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        s.setPageName(row.path("_pageName").asText());
        s.setItemClassIds(row.path("item_class_ids").asText());
        s.setModIds(row.path("mod_ids").asText());
        s.setStatId(row.path("stat_id").asText());
        s.setStatText(row.path("stat_text").asText());
        s.setStatValue(ItemConverter.parseDoubleSafe(row, "stat_value"));
        return s;
    }
}
