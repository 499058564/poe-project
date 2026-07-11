package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.DelveUpgradeStats;

public class DelveUpgradeStatsConverter implements DataConverter<DelveUpgradeStats> {

    @Override
    public DelveUpgradeStats convert(JsonNode row) {
        DelveUpgradeStats d = new DelveUpgradeStats();
        d.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        d.setPageName(row.path("_pageName").asText());
        d.setCargoId(row.path("id").asText());
        d.setLevel(ItemConverter.parseIntSafe(row, "level"));
        d.setType(row.path("type").asText());
        d.setValue(ItemConverter.parseDoubleSafe(row, "value"));
        return d;
    }
}
