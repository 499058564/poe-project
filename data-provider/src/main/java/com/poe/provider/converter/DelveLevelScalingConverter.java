package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.DelveLevelScaling;

public class DelveLevelScalingConverter implements DataConverter<DelveLevelScaling> {

    @Override
    public DelveLevelScaling convert(JsonNode row) {
        DelveLevelScaling d = new DelveLevelScaling();
        d.setPageName(row.path("_pageName").asText());
        d.setDepth(ItemConverter.parseIntSafe(row, "depth"));
        d.setDarknessResistance(ItemConverter.parseIntSafe(row, "darkness_resistance"));
        d.setLightRadius(ItemConverter.parseDoubleSafe(row, "light_radius"));
        d.setMonsterDamage(ItemConverter.parseDoubleSafe(row, "monster_damage"));
        d.setMonsterLevel(ItemConverter.parseIntSafe(row, "monster_level"));
        d.setMonsterLife(ItemConverter.parseDoubleSafe(row, "monster_life"));
        d.setSulphiteCost(ItemConverter.parseIntSafe(row, "sulphite_cost"));
        return d;
    }
}
