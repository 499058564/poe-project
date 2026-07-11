package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Graft;

public class GraftConverter implements DataConverter<Graft> {
    @Override
    public Graft convert(JsonNode row) {
        Graft v = new Graft();
        v.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        v.setPageName(ItemConverter.nullableText(row, "_pageName"));
        v.setSkillId(ItemConverter.nullableText(row, "skill_id"));
        return v;
    }
}
