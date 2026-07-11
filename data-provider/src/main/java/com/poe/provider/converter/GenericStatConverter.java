package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.GenericStat;

public class GenericStatConverter implements DataConverter<GenericStat> {
    @Override
    public GenericStat convert(JsonNode row) {
        GenericStat v = new GenericStat();
        v.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        v.setPageName(ItemConverter.nullableText(row, "_pageName"));
        v.setId(ItemConverter.nullableText(row, "id"));
        v.setName(ItemConverter.nullableText(row, "name"));
        v.setStatText(ItemConverter.nullableText(row, "stat_text"));
        v.setValue(ItemConverter.parseIntSafe(row, "value"));
        return v;
    }
}
