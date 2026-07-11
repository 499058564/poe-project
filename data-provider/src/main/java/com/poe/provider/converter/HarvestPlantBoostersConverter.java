package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.HarvestPlantBoosters;

public class HarvestPlantBoostersConverter implements DataConverter<HarvestPlantBoosters> {

    @Override
    public HarvestPlantBoosters convert(JsonNode row) {
        HarvestPlantBoosters h = new HarvestPlantBoosters();
        h.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        h.setPageName(row.path("_pageName").asText());
        h.setAdditionalCraftingOptions(row.path("additional_crafting_options").asText());
        h.setExtraChances(row.path("extra_chances").asText());
        h.setLifeforce(row.path("lifeforce").asText());
        h.setRadius(ItemConverter.parseIntSafe(row, "radius"));
        return h;
    }
}
