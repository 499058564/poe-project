package com.poe.provider.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poe.cache.model.SkillGem;

/**
 * 将 Wiki Cargo {@code skill_gems} 表行转换为 {@link SkillGem} 模型。
 * <p>
 * Cargo 字段映射：{@code gem_tags}（JSON 数组）、{@code quality_stats}、{@code level_stats}
 * 以 JSON 字符串存储，与 Wiki API 返回格式一致。
 */
public class SkillGemConverter implements DataConverter<SkillGem> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public SkillGem convert(JsonNode row) {
        SkillGem gem = new SkillGem();

        gem.setId(ItemConverter.parseIntSafe(row, "_pageID"));
        gem.setName(row.path("name").asText());
        gem.setNameZh(null);
        gem.setGemType(row.path("gem_type").asText());
        gem.setGemTags(ItemConverter.toJsonOrNull(row.path("gem_tags")));
        gem.setPrimaryAttribute(row.path("primary_attribute").asText());
        gem.setDescription(row.path("description").asText());
        gem.setQualityStats(ItemConverter.toJsonOrNull(row.path("quality_stats")));
        gem.setLevelStats(ItemConverter.toJsonOrNull(row.path("level_stats")));
        gem.setRequiredLevel(ItemConverter.parseIntSafe(row, "required_level"));
        gem.setVersion("");

        return gem;
    }
}
