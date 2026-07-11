package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.MonsterLifeScaling;

/**
 * 将 Wiki Cargo {@code monster_life_scaling} 表行转换为 {@link MonsterLifeScaling} 模型。
 * <p>
 * Cargo 字段：level, magic, rare。
 * 以 level 作为自然主键。
 */
public class MonsterLifeScalingConverter implements DataConverter<MonsterLifeScaling> {

    /**
     * 将 Cargo 单行 JSON 转换为 MonsterLifeScaling 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 怪物生命倍率实体，字段缺失时使用默认值
     */
    @Override
    public MonsterLifeScaling convert(JsonNode row) {
        MonsterLifeScaling mls = new MonsterLifeScaling();
        mls.setLevel(ItemConverter.parseIntSafe(row, "level"));
        mls.setMagic(ItemConverter.parseIntSafe(row, "magic"));
        mls.setRare(ItemConverter.parseIntSafe(row, "rare"));
        return mls;
    }
}
