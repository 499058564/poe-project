package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.MonsterType;

/**
 * 将 Wiki Cargo {@code monster_types} 表行转换为 {@link MonsterType} 模型。
 * <p>
 * Cargo 字段：armour_multiplier, damage_spread, energy_shield_multiplier,
 * evasion_multiplier, id, monster_resistance_id, tags。
 */
public class MonsterTypeConverter implements DataConverter<MonsterType> {

    /**
     * 将 Cargo 单行 JSON 转换为 MonsterType 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 怪物类型实体，字段缺失时使用默认值
     */
    @Override
    public MonsterType convert(JsonNode row) {
        MonsterType mt = new MonsterType();
        mt.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        mt.setPageName(row.path("_pageName").asText());
        mt.setArmourMultiplier(ItemConverter.parseIntSafe(row, "armour_multiplier"));
        mt.setDamageSpread(ItemConverter.parseDoubleSafe(row, "damage_spread"));
        mt.setEnergyShieldMultiplier(ItemConverter.parseIntSafe(row, "energy_shield_multiplier"));
        mt.setEvasionMultiplier(ItemConverter.parseIntSafe(row, "evasion_multiplier"));
        mt.setId(row.path("id").asText());
        mt.setMonsterResistanceId(row.path("monster_resistance_id").asText());
        mt.setTags(ItemConverter.nullableText(row, "tags"));
        return mt;
    }
}
