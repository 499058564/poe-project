package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.MonsterBaseStat;

/**
 * 将 Wiki Cargo {@code monster_base_stats} 表行转换为 {@link MonsterBaseStat} 模型。
 * <p>
 * Cargo 字段：accuracy, armour, damage, evasion, experience, level, life, summon_life。
 * 以 level 作为自然主键。
 */
public class MonsterBaseStatConverter implements DataConverter<MonsterBaseStat> {

    /**
     * 将 Cargo 单行 JSON 转换为 MonsterBaseStat 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 怪物基础属性实体，字段缺失时使用默认值
     */
    @Override
    public MonsterBaseStat convert(JsonNode row) {
        MonsterBaseStat mbs = new MonsterBaseStat();
        mbs.setLevel(ItemConverter.parseIntSafe(row, "level"));
        mbs.setAccuracy(ItemConverter.parseIntSafe(row, "accuracy"));
        mbs.setArmour(ItemConverter.parseIntSafe(row, "armour"));
        mbs.setDamage(ItemConverter.parseDoubleSafe(row, "damage"));
        mbs.setEvasion(ItemConverter.parseIntSafe(row, "evasion"));
        mbs.setExperience(ItemConverter.parseIntSafe(row, "experience"));
        mbs.setLife(ItemConverter.parseIntSafe(row, "life"));
        mbs.setSummonLife(ItemConverter.parseIntSafe(row, "summon_life"));
        return mbs;
    }
}
