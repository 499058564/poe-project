package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.HideoutDoodad;

public class HideoutDoodadConverter implements DataConverter<HideoutDoodad> {
    @Override
    public HideoutDoodad convert(JsonNode row) {
        HideoutDoodad v = new HideoutDoodad();
        v.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        v.setPageName(ItemConverter.nullableText(row, "_pageName"));
        v.setIsMasterDoodad(ItemConverter.parseIntSafe(row, "is_master_doodad"));
        v.setVariationCount(ItemConverter.parseIntSafe(row, "variation_count"));
        return v;
    }
}
