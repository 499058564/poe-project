package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.HeistAreas;

public class HeistAreasConverter implements DataConverter<HeistAreas> {

    @Override
    public HeistAreas convert(JsonNode row) {
        HeistAreas h = new HeistAreas();
        h.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        h.setPageName(row.path("_pageName").asText());
        h.setAreaId(row.path("id").asText());
        h.setAreaIds(row.path("area_ids").asText());
        h.setBlueprintId(row.path("blueprint_id").asText());
        h.setContractId(row.path("contract_id").asText());
        h.setJobIds(row.path("job_ids").asText());
        h.setRewardText(row.path("reward_text").asText());
        return h;
    }
}
