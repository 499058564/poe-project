package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.PassiveSkillConnection;

/**
 * 将 Wiki Cargo {@code passive_skill_connections} 表行转换为 {@link PassiveSkillConnection} 模型。
 * <p>
 * Cargo 字段：node_ids（逗号分隔列表）, tree_id。
 */
public class PassiveSkillConnectionConverter implements DataConverter<PassiveSkillConnection> {

    /**
     * 将 Cargo 单行 JSON 转换为 PassiveSkillConnection 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 天赋连接实体，字段缺失时使用默认值
     */
    @Override
    public PassiveSkillConnection convert(JsonNode row) {
        PassiveSkillConnection psc = new PassiveSkillConnection();
        psc.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        psc.setPageName(row.path("_pageName").asText());
        psc.setNodeIds(row.path("node_ids").asText());
        psc.setTreeId(row.path("tree_id").asText());
        return psc;
    }
}
