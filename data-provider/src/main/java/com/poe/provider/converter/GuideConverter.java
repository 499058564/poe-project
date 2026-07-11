package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Guide;

public class GuideConverter implements DataConverter<Guide> {
    @Override
    public Guide convert(JsonNode row) {
        Guide v = new Guide();
        v.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        v.setPageName(ItemConverter.nullableText(row, "_pageName"));
        v.setDate(ItemConverter.nullableText(row, "date"));
        v.setSubject(ItemConverter.nullableText(row, "subject"));
        v.setVersion(ItemConverter.nullableText(row, "version"));
        return v;
    }
}
