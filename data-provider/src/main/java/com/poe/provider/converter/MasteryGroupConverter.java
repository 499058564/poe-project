package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.MasteryGroup;

/**
 * 将 Wiki Cargo {@code mastery_groups} 表行转换为 {@link MasteryGroup} 模型。
 * <p>
 * Cargo 字段：icon(Page), id→groupId, name。
 */
public class MasteryGroupConverter implements DataConverter<MasteryGroup> {

    /**
     * 将 Cargo 单行 JSON 转换为 MasteryGroup 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 专精组实体，字段缺失时使用默认值
     */
    @Override
    public MasteryGroup convert(JsonNode row) {
        MasteryGroup mg = new MasteryGroup();
        mg.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        mg.setPageName(row.path("_pageName").asText());
        mg.setIcon(row.path("icon").asText());
        mg.setGroupId(row.path("id").asText());
        mg.setName(row.path("name").asText());
        return mg;
    }
}
