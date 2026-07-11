package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Idol;

public class IdolConverter implements DataConverter<Idol> {
    @Override
    public Idol convert(JsonNode row) {
        Idol v = new Idol();
        v.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        v.setPageName(ItemConverter.nullableText(row, "_pageName"));
        v.setIdolLimit(ItemConverter.nullableText(row, "idol_limit"));
        return v;
    }
}
