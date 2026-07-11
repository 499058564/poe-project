package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.SpawnWeight;

public class SpawnWeightConverter implements DataConverter<SpawnWeight> {
    @Override
    public SpawnWeight convert(JsonNode row) {
        SpawnWeight v = new SpawnWeight();
        v.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        v.setPageName(ItemConverter.nullableText(row, "_pageName"));
        v.setOrdinal(ItemConverter.parseIntSafe(row, "ordinal"));
        v.setTag(ItemConverter.nullableText(row, "tag"));
        v.setWeight(ItemConverter.parseIntSafe(row, "weight"));
        return v;
    }
}
