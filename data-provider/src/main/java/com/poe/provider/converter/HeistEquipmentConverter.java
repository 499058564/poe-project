package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.HeistEquipment;

public class HeistEquipmentConverter implements DataConverter<HeistEquipment> {

    @Override
    public HeistEquipment convert(JsonNode row) {
        HeistEquipment h = new HeistEquipment();
        h.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        h.setPageName(row.path("_pageName").asText());
        h.setRequiredJobId(row.path("required_job_id").asText());
        h.setRequiredJobLevel(ItemConverter.parseIntSafe(row, "required_job_level"));
        return h;
    }
}
