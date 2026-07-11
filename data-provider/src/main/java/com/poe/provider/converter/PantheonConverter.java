package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Pantheon;

public class PantheonConverter implements DataConverter<Pantheon> {

    @Override
    public Pantheon convert(JsonNode row) {
        Pantheon p = new Pantheon();
        p.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        p.setPageName(row.path("_pageName").asText());
        p.setGodName(row.path("id").asText());
        p.setMajorGod(ItemConverter.parseBooleanSafe(row, "is_major_god"));
        return p;
    }
}
