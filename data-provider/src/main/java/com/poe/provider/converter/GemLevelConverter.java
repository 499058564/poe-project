package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.GemLevel;

/**
 * 将 Wiki Cargo {@code gem_levels} 表行转换为 {@link GemLevel} 模型。
 * <p>
 * Cargo 字段：experience, level, required_dexterity, required_intelligence,
 * required_level, required_strength。
 */
public class GemLevelConverter implements DataConverter<GemLevel> {

    /**
     * 将 Cargo 单行 JSON 转换为 GemLevel 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 宝石等级实体，字段缺失时使用默认值
     */
    @Override
    public GemLevel convert(JsonNode row) {
        GemLevel gl = new GemLevel();
        gl.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        gl.setPageName(row.path("_pageName").asText());
        gl.setExperience(ItemConverter.parseIntSafe(row, "experience"));
        gl.setLevel(ItemConverter.parseIntSafe(row, "level"));
        gl.setRequiredDexterity(ItemConverter.parseIntSafe(row, "required_dexterity"));
        gl.setRequiredIntelligence(ItemConverter.parseIntSafe(row, "required_intelligence"));
        gl.setRequiredLevel(ItemConverter.parseIntSafe(row, "required_level"));
        gl.setRequiredStrength(ItemConverter.parseIntSafe(row, "required_strength"));
        return gl;
    }
}
