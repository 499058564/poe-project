package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.SkillQualityStats;

/**
 * 将 Wiki Cargo {@code skill_quality_stats} 表行转换为 {@link SkillQualityStats} 模型。
 * <p>
 * Cargo 字段：id→statId, set_id, value。
 */
public class SkillQualityStatsConverter implements DataConverter<SkillQualityStats> {

    /**
     * 将 Cargo 单行 JSON 转换为 SkillQualityStats 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 技能品质属性实体，字段缺失时使用默认值
     */
    @Override
    public SkillQualityStats convert(JsonNode row) {
        SkillQualityStats s = new SkillQualityStats();
        s.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        s.setPageName(row.path("_pageName").asText());
        s.setStatId(row.path("id").asText());
        s.setSetId(ItemConverter.parseIntSafe(row, "set_id"));
        s.setValue(ItemConverter.parseIntSafe(row, "value"));
        return s;
    }
}
