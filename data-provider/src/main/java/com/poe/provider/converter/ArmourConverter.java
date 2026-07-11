package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Armour;

/**
 * 将 Wiki Cargo {@code armours} 子表行转换为 {@link Armour} 模型。
 */
public class ArmourConverter implements DataConverter<Armour> {

    /**
     * 将 Cargo 单行 JSON 转换为 Armour 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 护甲实体，防御属性缺失时使用默认值 0
     */
    @Override
    public Armour convert(JsonNode row) {
        Armour a = new Armour();
        a.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        a.setPageName(row.path("_pageName").asText());
        a.setArmourMin(ItemConverter.parseIntSafe(row, "armour_min"));
        a.setArmourMax(ItemConverter.parseIntSafe(row, "armour_max"));
        a.setEvasionMin(ItemConverter.parseIntSafe(row, "evasion_min"));
        a.setEvasionMax(ItemConverter.parseIntSafe(row, "evasion_max"));
        a.setEnergyShieldMin(ItemConverter.parseIntSafe(row, "energy_shield_min"));
        a.setEnergyShieldMax(ItemConverter.parseIntSafe(row, "energy_shield_max"));
        a.setWardMin(ItemConverter.parseIntSafe(row, "ward_min"));
        a.setWardMax(ItemConverter.parseIntSafe(row, "ward_max"));
        a.setMovementSpeed(ItemConverter.parseIntSafe(row, "movement_speed"));
        return a;
    }
}
