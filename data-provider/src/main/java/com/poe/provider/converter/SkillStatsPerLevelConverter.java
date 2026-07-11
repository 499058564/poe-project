package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.SkillStatsPerLevel;

/**
 * 将 Wiki Cargo {@code skill_stats_per_level} 表行转换为 {@link SkillStatsPerLevel} 模型。
 * <p>
 * Cargo 字段：id→statId, level, value。
 */
public class SkillStatsPerLevelConverter implements DataConverter<SkillStatsPerLevel> {

    /**
     * 将 Cargo 单行 JSON 转换为 SkillStatsPerLevel 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 技能属性实体，字段缺失时使用默认值
     */
    @Override
    public SkillStatsPerLevel convert(JsonNode row) {
        SkillStatsPerLevel s = new SkillStatsPerLevel();
        s.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        s.setPageName(row.path("_pageName").asText());
        s.setStatId(row.path("id").asText());
        s.setLevel(ItemConverter.parseIntSafe(row, "level"));
        s.setValue(ItemConverter.parseIntSafe(row, "value"));
        return s;
    }
}
