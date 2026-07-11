package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Monster;

/**
 * 将 Wiki Cargo {@code monsters} 表行转换为 {@link Monster} 模型。
 * <p>
 * Cargo 字段：attack_speed, critical_strike_chance, damage_multiplier,
 * endgame_mod_ids, experience_multiplier, health_multiplier, is_boss,
 * maximum_attack_distance, metadata_id, minimum_attack_distance, mod_ids,
 * model_size_multiplier, monster_type_id, name, part1_mod_ids, part2_mod_ids,
 * rarity, rarity_id, size, skill_ids, tags。
 */
public class MonsterConverter implements DataConverter<Monster> {

    /**
     * 将 Cargo 单行 JSON 转换为 Monster 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 怪物实体，字段缺失时使用默认值
     */
    @Override
    public Monster convert(JsonNode row) {
        Monster m = new Monster();
        m.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        m.setPageName(row.path("_pageName").asText());
        m.setAttackSpeed(ItemConverter.parseIntSafe(row, "attack_speed"));
        m.setCriticalStrikeChance(ItemConverter.parseIntSafe(row, "critical_strike_chance"));
        m.setDamageMultiplier(ItemConverter.parseDoubleSafe(row, "damage_multiplier"));
        m.setEndgameModIds(ItemConverter.nullableText(row, "endgame_mod_ids"));
        m.setExperienceMultiplier(ItemConverter.parseDoubleSafe(row, "experience_multiplier"));
        m.setHealthMultiplier(ItemConverter.parseDoubleSafe(row, "health_multiplier"));
        m.setBoss(ItemConverter.parseBooleanSafe(row, "is_boss"));
        m.setMaximumAttackDistance(ItemConverter.parseIntSafe(row, "maximum_attack_distance"));
        m.setMetadataId(row.path("metadata_id").asText());
        m.setMinimumAttackDistance(ItemConverter.parseIntSafe(row, "minimum_attack_distance"));
        m.setModIds(ItemConverter.nullableText(row, "mod_ids"));
        m.setModelSizeMultiplier(ItemConverter.parseDoubleSafe(row, "model_size_multiplier"));
        m.setMonsterTypeId(row.path("monster_type_id").asText());
        m.setName(row.path("name").asText());
        m.setPart1ModIds(ItemConverter.nullableText(row, "part1_mod_ids"));
        m.setPart2ModIds(ItemConverter.nullableText(row, "part2_mod_ids"));
        m.setRarity(row.path("rarity").asText());
        m.setRarityId(row.path("rarity_id").asText());
        m.setSize(ItemConverter.parseIntSafe(row, "size"));
        m.setSkillIds(ItemConverter.nullableText(row, "skill_ids"));
        m.setTags(ItemConverter.nullableText(row, "tags"));
        return m;
    }
}
