package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.CharacterClass;

/**
 * 将 Wiki Cargo {@code character_classes} 表行转换为 {@link CharacterClass} 模型。
 * <p>
 * Cargo 字段：dexterity, flavour_text, id→classId, intelligence,
 * name, str_id, strength。
 */
public class CharacterClassConverter implements DataConverter<CharacterClass> {

    /**
     * 将 Cargo 单行 JSON 转换为 CharacterClass 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 职业实体，字段缺失时使用默认值
     */
    @Override
    public CharacterClass convert(JsonNode row) {
        CharacterClass cc = new CharacterClass();
        cc.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        cc.setPageName(row.path("_pageName").asText());
        cc.setDexterity(ItemConverter.nullableText(row, "dexterity"));
        cc.setFlavourText(ItemConverter.nullableText(row, "flavour_text"));
        cc.setClassId(ItemConverter.parseIntSafe(row, "id"));
        cc.setIntelligence(ItemConverter.nullableText(row, "intelligence"));
        cc.setName(row.path("name").asText());
        cc.setStrId(row.path("str_id").asText());
        cc.setStrength(ItemConverter.nullableText(row, "strength"));
        return cc;
    }
}
