package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.PassiveSkill;

/**
 * 将 Wiki Cargo {@code passive_skills} 表行转换为 {@link PassiveSkill} 模型。
 * <p>
 * Cargo 字段：{@code id}→passiveId, {@code name}, {@code ascendancy_class}→ascendancy,
 * {@code stat_text}→stats, {@code is_keystone/notable/jewel_socket},
 * {@code connections}（逗号分隔列表）, {@code mastery_id}, {@code flavour_text}, {@code skill_points}。
 * 注意：x/y 不在 Cargo 表，passive_class 字段不存在。
 */
public class PassiveSkillConverter implements DataConverter<PassiveSkill> {

    /**
     * 将 Cargo 单行 JSON 转换为 PassiveSkill 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 天赋实体，x/y 固定为 0.0（需从 POB tree.json 补充），passive_class 固定为 null
     */
    @Override
    public PassiveSkill convert(JsonNode row) {
        PassiveSkill ps = new PassiveSkill();

        ps.setId(ItemConverter.parseIntSafe(row, "_pageID"));
        ps.setName(row.path("name").asText());
        ps.setNameZh(null);
        ps.setPassiveClass(null); // Cargo 无 passive_class 字段
        ps.setAscendancy(ItemConverter.nullableText(row, "ascendancy_class"));
        ps.setStats(ItemConverter.toJsonOrNull(row.path("stat_text")));
        ps.setKeystone(parseBool(row, "is_keystone"));
        ps.setNotable(parseBool(row, "is_notable"));
        ps.setJewelSocket(parseBool(row, "is_jewel_socket"));
        ps.setX(0.0); // Cargo 无 x/y 字段，需从 POB tree.json 补充
        ps.setY(0.0);
        ps.setConnections(row.path("connections").asText()); // Cargo: 逗号分隔
        ps.setVersion("");

        return ps;
    }

    /** 解析布尔字段，支持原生布尔值或 "1"/"true" 文本（不区分大小写） */
    private static boolean parseBool(JsonNode node, String field) {
        JsonNode val = node.path(field);
        if (val.isBoolean()) return val.asBoolean();
        String text = val.asText();
        return "1".equals(text) || "true".equalsIgnoreCase(text);
    }
}
