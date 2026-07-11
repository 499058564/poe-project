package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.PantheonSouls;

public class PantheonSoulsConverter implements DataConverter<PantheonSouls> {

    @Override
    public PantheonSouls convert(JsonNode row) {
        PantheonSouls p = new PantheonSouls();
        p.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        p.setPageName(row.path("_pageName").asText());
        p.setSoulId(row.path("id").asText());
        p.setItemId(row.path("item_id").asText());
        p.setName(row.path("name").asText());
        p.setOrdinal(ItemConverter.parseIntSafe(row, "ordinal"));
        p.setStatText(row.path("stat_text").asText());
        p.setTargetAreaId(row.path("target_area_id").asText());
        p.setTargetMonsterId(row.path("target_monster_id").asText());
        return p;
    }
}
