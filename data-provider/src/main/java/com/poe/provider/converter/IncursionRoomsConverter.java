package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.IncursionRooms;

public class IncursionRoomsConverter implements DataConverter<IncursionRooms> {

    @Override
    public IncursionRooms convert(JsonNode row) {
        IncursionRooms i = new IncursionRooms();
        i.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        i.setPageName(row.path("_pageName").asText());
        i.setArchitectMetadataId(row.path("architect_metadata_id").asText());
        i.setArchitectName(row.path("architect_name").asText());
        i.setDescription(ItemConverter.nullableText(row, "description"));
        i.setFlavourText(ItemConverter.nullableText(row, "flavour_text"));
        i.setIcon(row.path("icon").asText());
        i.setRoomId(row.path("id").asText());
        i.setMinLevel(ItemConverter.parseIntSafe(row, "min_level"));
        i.setModifierIds(row.path("modifier_ids").asText());
        i.setName(row.path("name").asText());
        i.setStatText(row.path("stat_text").asText());
        i.setTier(ItemConverter.parseIntSafe(row, "tier"));
        i.setUpgradeRoomId(row.path("upgrade_room_id").asText());
        return i;
    }
}
