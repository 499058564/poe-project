package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.SkillLevel;

/**
 * 将 Wiki Cargo {@code skill_levels} 表行转换为 {@link SkillLevel} 模型。
 * <p>
 * Cargo 字段：attack_speed_multiplier, attack_time, cooldown, cost_amounts,
 * cost_multiplier, cost_types, critical_strike_chance, damage_effectiveness,
 * damage_multiplier, dexterity_requirement, duration, experience,
 * intelligence_requirement, level, level_requirement, life_reservation_flat,
 * life_reservation_percent, mana_reservation_flat, mana_reservation_percent,
 * skill_level, stat_text, stored_uses, strength_requirement,
 * vaal_soul_gain_prevention_time, vaal_souls_requirement, vaal_stored_uses。
 */
public class SkillLevelConverter implements DataConverter<SkillLevel> {

    /**
     * 将 Cargo 单行 JSON 转换为 SkillLevel 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 技能等级实体，字段缺失时使用默认值
     */
    @Override
    public SkillLevel convert(JsonNode row) {
        SkillLevel sl = new SkillLevel();
        sl.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        sl.setPageName(row.path("_pageName").asText());
        sl.setAttackSpeedMultiplier(ItemConverter.parseIntSafe(row, "attack_speed_multiplier"));
        sl.setAttackTime(parseDoubleSafe(row, "attack_time"));
        sl.setCooldown(parseDoubleSafe(row, "cooldown"));
        sl.setCostAmounts(row.path("cost_amounts").asText());
        sl.setCostMultiplier(parseDoubleSafe(row, "cost_multiplier"));
        sl.setCostTypes(row.path("cost_types").asText());
        sl.setCriticalStrikeChance(parseDoubleSafe(row, "critical_strike_chance"));
        sl.setDamageEffectiveness(parseDoubleSafe(row, "damage_effectiveness"));
        sl.setDamageMultiplier(parseDoubleSafe(row, "damage_multiplier"));
        sl.setDexterityRequirement(ItemConverter.parseIntSafe(row, "dexterity_requirement"));
        sl.setDuration(parseDoubleSafe(row, "duration"));
        sl.setExperience(ItemConverter.parseIntSafe(row, "experience"));
        sl.setIntelligenceRequirement(ItemConverter.parseIntSafe(row, "intelligence_requirement"));
        sl.setLevel(ItemConverter.parseIntSafe(row, "level"));
        sl.setLevelRequirement(ItemConverter.parseIntSafe(row, "level_requirement"));
        sl.setLifeReservationFlat(ItemConverter.parseIntSafe(row, "life_reservation_flat"));
        sl.setLifeReservationPercent(ItemConverter.parseIntSafe(row, "life_reservation_percent"));
        sl.setManaReservationFlat(ItemConverter.parseIntSafe(row, "mana_reservation_flat"));
        sl.setManaReservationPercent(ItemConverter.parseIntSafe(row, "mana_reservation_percent"));
        sl.setSkillLevel(ItemConverter.parseIntSafe(row, "skill_level"));
        sl.setStatText(ItemConverter.nullableText(row, "stat_text"));
        sl.setStoredUses(ItemConverter.parseIntSafe(row, "stored_uses"));
        sl.setStrengthRequirement(ItemConverter.parseIntSafe(row, "strength_requirement"));
        sl.setVaalSoulGainPreventionTime(parseDoubleSafe(row, "vaal_soul_gain_prevention_time"));
        sl.setVaalSoulsRequirement(ItemConverter.parseIntSafe(row, "vaal_souls_requirement"));
        sl.setVaalStoredUses(ItemConverter.parseIntSafe(row, "vaal_stored_uses"));
        return sl;
    }

    /** 安全解析 double 字段，空值或解析失败返回 0.0 */
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
