package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.PantheonStats;

public class PantheonStatsConverter implements DataConverter<PantheonStats> {

    @Override
    public PantheonStats convert(JsonNode row) {
        PantheonStats p = new PantheonStats();
        p.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        p.setPageName(row.path("_pageName").asText());
        p.setStatId(row.path("id").asText());
        p.setOrdinal(ItemConverter.parseIntSafe(row, "ordinal"));
        p.setPantheonId(row.path("pantheon_id").asText());
        p.setPantheonOrdinal(ItemConverter.parseIntSafe(row, "pantheon_ordinal"));
        p.setValue(row.path("value").asText());
        return p;
    }
}
