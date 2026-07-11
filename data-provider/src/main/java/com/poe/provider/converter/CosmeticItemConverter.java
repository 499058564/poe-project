package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.CosmeticItem;

public class CosmeticItemConverter implements DataConverter<CosmeticItem> {
    @Override
    public CosmeticItem convert(JsonNode row) {
        CosmeticItem v = new CosmeticItem();
        v.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        v.setPageName(ItemConverter.nullableText(row, "_pageName"));
        v.setCosmeticType(ItemConverter.nullableText(row, "cosmetic_type"));
        v.setTarget(ItemConverter.nullableText(row, "target"));
        v.setTheme(ItemConverter.nullableText(row, "theme"));
        return v;
    }
}
