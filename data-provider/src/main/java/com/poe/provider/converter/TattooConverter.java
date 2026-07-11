package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Tattoo;

public class TattooConverter implements DataConverter<Tattoo> {
    @Override
    public Tattoo convert(JsonNode row) {
        Tattoo v = new Tattoo();
        v.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        v.setPageName(ItemConverter.nullableText(row, "_pageName"));
        v.setMaxAdjacent(ItemConverter.parseIntSafe(row, "max_adjacent"));
        v.setMinAdjacent(ItemConverter.parseIntSafe(row, "min_adjacent"));
        v.setSkillId(ItemConverter.nullableText(row, "skill_id"));
        v.setTarget(ItemConverter.nullableText(row, "target"));
        v.setTattooLimit(ItemConverter.nullableText(row, "tattoo_limit"));
        v.setTribe(ItemConverter.parseIntSafe(row, "tribe"));
        return v;
    }
}
