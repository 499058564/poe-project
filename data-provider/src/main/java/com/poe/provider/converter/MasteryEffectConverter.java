package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.MasteryEffect;

/**
 * 将 Wiki Cargo {@code mastery_effects} 表行转换为 {@link MasteryEffect} 模型。
 * <p>
 * Cargo 字段：id→effectId, stat_ids（逗号分隔列表）, stat_text,
 * stat_text_raw, stat_values（逗号分隔列表）。
 */
public class MasteryEffectConverter implements DataConverter<MasteryEffect> {

    /**
     * 将 Cargo 单行 JSON 转换为 MasteryEffect 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 专精效果实体，字段缺失时使用默认值
     */
    @Override
    public MasteryEffect convert(JsonNode row) {
        MasteryEffect me = new MasteryEffect();
        me.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        me.setPageName(row.path("_pageName").asText());
        me.setEffectId(row.path("id").asText());
        me.setStatIds(row.path("stat_ids").asText());
        me.setStatText(ItemConverter.nullableText(row, "stat_text"));
        me.setStatTextRaw(ItemConverter.nullableText(row, "stat_text_raw"));
        me.setStatValues(row.path("stat_values").asText());
        return me;
    }
}
