package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Skill;

/**
 * 将 Wiki Cargo {@code skill} 表行转换为 {@link Skill} 模型。
 * <p>
 * Cargo 字段：active_skill_name, cast_time, description, is_support,
 * item_class_id_restriction, item_class_restriction, max_level, skill_id, stat_text。
 */
public class SkillConverter implements DataConverter<Skill> {

    /**
     * 将 Cargo 单行 JSON 转换为 Skill 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 技能实体，字段缺失时使用默认值
     */
    @Override
    public Skill convert(JsonNode row) {
        Skill s = new Skill();
        s.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        s.setPageName(row.path("_pageName").asText());
        s.setActiveSkillName(row.path("active_skill_name").asText());
        s.setCastTime(parseDoubleSafe(row, "cast_time"));
        s.setDescription(ItemConverter.nullableText(row, "description"));
        s.setSupport(ItemConverter.parseBooleanSafe(row, "is_support"));
        s.setItemClassIdRestriction(row.path("item_class_id_restriction").asText());
        s.setItemClassRestriction(row.path("item_class_restriction").asText());
        s.setMaxLevel(ItemConverter.parseIntSafe(row, "max_level"));
        s.setSkillId(row.path("skill_id").asText());
        s.setStatText(ItemConverter.nullableText(row, "stat_text"));
        return s;
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
