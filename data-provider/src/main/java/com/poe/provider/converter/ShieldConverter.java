package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Shield;

/**
 * 将 Wiki Cargo {@code shields} 子表行转换为 {@link Shield} 模型。
 */
public class ShieldConverter implements DataConverter<Shield> {

    /**
     * 将 Cargo 单行 JSON 转换为 Shield 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 盾牌实体，block 缺失时使用默认值 0
     */
    @Override
    public Shield convert(JsonNode row) {
        Shield s = new Shield();
        s.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        s.setPageName(row.path("_pageName").asText());
        s.setBlock(ItemConverter.parseIntSafe(row, "block"));
        return s;
    }
}
