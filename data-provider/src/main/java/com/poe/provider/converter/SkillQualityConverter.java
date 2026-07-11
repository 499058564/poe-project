package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.SkillQuality;

/**
 * 将 Wiki Cargo {@code skill_quality} 表行转换为 {@link SkillQuality} 模型。
 * <p>
 * Cargo 字段：set_id, stat_text, weight。
 */
public class SkillQualityConverter implements DataConverter<SkillQuality> {

    /**
     * 将 Cargo 单行 JSON 转换为 SkillQuality 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 技能品质实体，字段缺失时使用默认值
     */
    @Override
    public SkillQuality convert(JsonNode row) {
        SkillQuality sq = new SkillQuality();
        sq.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        sq.setPageName(row.path("_pageName").asText());
        sq.setSetId(ItemConverter.parseIntSafe(row, "set_id"));
        sq.setStatText(ItemConverter.nullableText(row, "stat_text"));
        sq.setWeight(ItemConverter.parseIntSafe(row, "weight"));
        return sq;
    }
}
