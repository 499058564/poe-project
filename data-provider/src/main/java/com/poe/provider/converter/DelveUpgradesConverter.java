package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.DelveUpgrades;

public class DelveUpgradesConverter implements DataConverter<DelveUpgrades> {

    @Override
    public DelveUpgrades convert(JsonNode row) {
        DelveUpgrades d = new DelveUpgrades();
        d.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        d.setPageName(row.path("_pageName").asText());
        d.setCost(ItemConverter.parseIntSafe(row, "cost"));
        d.setLevel(ItemConverter.parseIntSafe(row, "level"));
        d.setType(row.path("type").asText());
        return d;
    }
}
