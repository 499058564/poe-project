package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Weapon;

/**
 * 将 Wiki Cargo {@code weapons} 子表行转换为 {@link Weapon} 模型。
 * <p>
 * Cargo 字段：attack_speed, critical_strike_chance, weapon_range,
 * physical_damage_min/max, fire/cold/lightning/chaos_damage_min/max。
 */
public class WeaponConverter implements DataConverter<Weapon> {

    @Override
    public Weapon convert(JsonNode row) {
        Weapon w = new Weapon();
        w.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        w.setPageName(row.path("_pageName").asText());
        w.setAttackSpeed(parseDoubleSafe(row, "attack_speed"));
        w.setCriticalStrikeChance(parseDoubleSafe(row, "critical_strike_chance"));
        w.setWeaponRange(parseDoubleSafe(row, "weapon_range"));
        w.setPhysicalDamageMin(ItemConverter.parseIntSafe(row, "physical_damage_min"));
        w.setPhysicalDamageMax(ItemConverter.parseIntSafe(row, "physical_damage_max"));
        w.setFireDamageMin(ItemConverter.parseIntSafe(row, "fire_damage_min"));
        w.setFireDamageMax(ItemConverter.parseIntSafe(row, "fire_damage_max"));
        w.setColdDamageMin(ItemConverter.parseIntSafe(row, "cold_damage_min"));
        w.setColdDamageMax(ItemConverter.parseIntSafe(row, "cold_damage_max"));
        w.setLightningDamageMin(ItemConverter.parseIntSafe(row, "lightning_damage_min"));
        w.setLightningDamageMax(ItemConverter.parseIntSafe(row, "lightning_damage_max"));
        w.setChaosDamageMin(ItemConverter.parseIntSafe(row, "chaos_damage_min"));
        w.setChaosDamageMax(ItemConverter.parseIntSafe(row, "chaos_damage_max"));
        return w;
    }

    static double parseDoubleSafe(JsonNode node, String field) {
        String text = node.path(field).asText();
        if (text.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
