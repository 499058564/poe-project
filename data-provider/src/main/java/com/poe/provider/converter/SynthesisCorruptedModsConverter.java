package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.SynthesisCorruptedMods;

public class SynthesisCorruptedModsConverter implements DataConverter<SynthesisCorruptedMods> {

    @Override
    public SynthesisCorruptedMods convert(JsonNode row) {
        SynthesisCorruptedMods s = new SynthesisCorruptedMods();
        s.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        s.setPageName(row.path("_pageName").asText());
        s.setItemClassId(row.path("item_class_id").asText());
        s.setModIds(row.path("mod_ids").asText());
        return s;
    }
}
