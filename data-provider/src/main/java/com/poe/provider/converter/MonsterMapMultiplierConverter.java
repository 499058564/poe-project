package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.MonsterMapMultiplier;

/**
 * 将 Wiki Cargo {@code monster_map_multipliers} 表行转换为 {@link MonsterMapMultiplier} 模型。
 * <p>
 * Cargo 字段：boss_damage, boss_item_quantity, boss_item_rarity,
 * boss_life, damage, level, life。
 * 以 level 作为自然主键。
 */
public class MonsterMapMultiplierConverter implements DataConverter<MonsterMapMultiplier> {

    /**
     * 将 Cargo 单行 JSON 转换为 MonsterMapMultiplier 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 地图怪物倍率实体，字段缺失时使用默认值
     */
    @Override
    public MonsterMapMultiplier convert(JsonNode row) {
        MonsterMapMultiplier mmm = new MonsterMapMultiplier();
        mmm.setLevel(ItemConverter.parseIntSafe(row, "level"));
        mmm.setBossDamage(ItemConverter.parseIntSafe(row, "boss_damage"));
        mmm.setBossItemQuantity(ItemConverter.parseIntSafe(row, "boss_item_quantity"));
        mmm.setBossItemRarity(ItemConverter.parseIntSafe(row, "boss_item_rarity"));
        mmm.setBossLife(ItemConverter.parseIntSafe(row, "boss_life"));
        mmm.setDamage(ItemConverter.parseIntSafe(row, "damage"));
        mmm.setLife(ItemConverter.parseIntSafe(row, "life"));
        return mmm;
    }
}
