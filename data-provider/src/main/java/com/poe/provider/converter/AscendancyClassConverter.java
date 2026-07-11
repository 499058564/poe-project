package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.AscendancyClass;

/**
 * 将 Wiki Cargo {@code ascendancy_classes} 表行转换为 {@link AscendancyClass} 模型。
 * <p>
 * Cargo 字段：character_class, character_id, flavour_text, id→ascendancyId, name。
 */
public class AscendancyClassConverter implements DataConverter<AscendancyClass> {

    /**
     * 将 Cargo 单行 JSON 转换为 AscendancyClass 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 升华职业实体，字段缺失时使用默认值
     */
    @Override
    public AscendancyClass convert(JsonNode row) {
        AscendancyClass ac = new AscendancyClass();
        ac.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        ac.setPageName(row.path("_pageName").asText());
        ac.setCharacterClass(row.path("character_class").asText());
        ac.setCharacterId(ItemConverter.parseIntSafe(row, "character_id"));
        ac.setFlavourText(ItemConverter.nullableText(row, "flavour_text"));
        ac.setAscendancyId(ItemConverter.parseIntSafe(row, "id"));
        ac.setName(row.path("name").asText());
        return ac;
    }
}
