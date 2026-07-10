package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.PassiveSkill;

/**
 * 将 Wiki Cargo {@code passive_skills} 表行转换为 {@link PassiveSkill} 模型。
 * <p>
 * Wiki 的 {@code is_keystone}、{@code is_notable}、{@code is_jewel_socket}
 * 可能以字符串 "1"/"0" 或布尔值返回，统一按非空/非零处理。
 */
public class PassiveSkillConverter implements DataConverter<PassiveSkill> {

    @Override
    public PassiveSkill convert(JsonNode row) {
        PassiveSkill ps = new PassiveSkill();

        ps.setId(ItemConverter.parseIntSafe(row, "_pageID"));
        ps.setName(row.path("name").asText());
        ps.setNameZh(null);
        ps.setPassiveClass(row.path("passive_class").asText());
        ps.setAscendancy(ItemConverter.nullToNull(row.path("ascendancy").asText()));
        ps.setStats(ItemConverter.toJsonOrNull(row.path("stats")));
        ps.setKeystone(parseBool(row, "is_keystone"));
        ps.setNotable(parseBool(row, "is_notable"));
        ps.setJewelSocket(parseBool(row, "is_jewel_socket"));
        ps.setX(parseDoubleSafe(row, "x"));
        ps.setY(parseDoubleSafe(row, "y"));
        ps.setConnections(ItemConverter.toJsonOrNull(row.path("connections")));
        ps.setVersion("");

        return ps;
    }

    private static boolean parseBool(JsonNode node, String field) {
        JsonNode val = node.path(field);
        if (val.isBoolean()) return val.asBoolean();
        String text = val.asText();
        return "1".equals(text) || "true".equalsIgnoreCase(text);
    }

    private static double parseDoubleSafe(JsonNode node, String field) {
        String text = node.path(field).asText();
        if (text.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
