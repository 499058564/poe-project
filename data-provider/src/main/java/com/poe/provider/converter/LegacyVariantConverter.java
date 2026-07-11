package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.LegacyVariant;

public class LegacyVariantConverter implements DataConverter<LegacyVariant> {
    @Override
    public LegacyVariant convert(JsonNode row) {
        LegacyVariant v = new LegacyVariant();
        v.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        v.setPageName(ItemConverter.nullableText(row, "_pageName"));
        v.setRemovalVersion(ItemConverter.nullableText(row, "removal_version"));
        v.setImplicitStatText(ItemConverter.nullableText(row, "implicit_stat_text"));
        v.setExplicitStatText(ItemConverter.nullableText(row, "explicit_stat_text"));
        v.setStatText(ItemConverter.nullableText(row, "stat_text"));
        v.setBaseItem(ItemConverter.nullableText(row, "base_item"));
        v.setRequiredLevel(ItemConverter.parseIntSafe(row, "required_level"));
        return v;
    }
}
