package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.HeistNpcStats;

public class HeistNpcStatsConverter implements DataConverter<HeistNpcStats> {

    @Override
    public HeistNpcStats convert(JsonNode row) {
        HeistNpcStats h = new HeistNpcStats();
        h.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        h.setPageName(row.path("_pageName").asText());
        h.setNpcId(row.path("npc_id").asText());
        h.setStatId(row.path("stat_id").asText());
        h.setValue(ItemConverter.parseDoubleSafe(row, "value"));
        return h;
    }
}
