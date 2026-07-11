package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.HeistNpcSkills;

public class HeistNpcSkillsConverter implements DataConverter<HeistNpcSkills> {

    @Override
    public HeistNpcSkills convert(JsonNode row) {
        HeistNpcSkills h = new HeistNpcSkills();
        h.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        h.setPageName(row.path("_pageName").asText());
        h.setJobId(row.path("job_id").asText());
        h.setLevel(row.path("level").asText());
        h.setNpcId(row.path("npc_id").asText());
        return h;
    }
}
