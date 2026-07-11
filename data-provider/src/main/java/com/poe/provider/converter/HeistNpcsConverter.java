package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.HeistNpcs;

public class HeistNpcsConverter implements DataConverter<HeistNpcs> {

    @Override
    public HeistNpcs convert(JsonNode row) {
        HeistNpcs h = new HeistNpcs();
        h.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        h.setPageName(row.path("_pageName").asText());
        h.setNpcId(row.path("id").asText());
        h.setJobId(row.path("job_id").asText());
        h.setName(row.path("name").asText());
        h.setStatText(row.path("stat_text").asText());
        return h;
    }
}
