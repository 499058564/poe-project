package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.HeistJobs;

public class HeistJobsConverter implements DataConverter<HeistJobs> {

    @Override
    public HeistJobs convert(JsonNode row) {
        HeistJobs h = new HeistJobs();
        h.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        h.setPageName(row.path("_pageName").asText());
        h.setJobId(row.path("id").asText());
        h.setName(row.path("name").asText());
        return h;
    }
}
