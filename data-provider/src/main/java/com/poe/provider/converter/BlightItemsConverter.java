package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.BlightItems;

public class BlightItemsConverter implements DataConverter<BlightItems> {

    @Override
    public BlightItems convert(JsonNode row) {
        BlightItems b = new BlightItems();
        b.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        b.setPageName(row.path("_pageName").asText());
        b.setTier(row.path("tier").asText());
        return b;
    }
}
