package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Flask;

/**
 * 将 Wiki Cargo {@code flasks} 子表行转换为 {@link Flask} 模型。
 */
public class FlaskConverter implements DataConverter<Flask> {

    /**
     * 将 Cargo 单行 JSON 转换为 Flask 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 药剂实体，数值字段缺失时使用默认值（0 / 0.0）
     */
    @Override
    public Flask convert(JsonNode row) {
        Flask f = new Flask();
        f.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        f.setPageName(row.path("_pageName").asText());
        f.setChargesMax(ItemConverter.parseIntSafe(row, "charges_max"));
        f.setChargesPerUse(ItemConverter.parseIntSafe(row, "charges_per_use"));
        f.setDuration(parseDoubleSafe(row, "duration"));
        f.setLife(ItemConverter.parseIntSafe(row, "life"));
        f.setMana(ItemConverter.parseIntSafe(row, "mana"));
        return f;
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
