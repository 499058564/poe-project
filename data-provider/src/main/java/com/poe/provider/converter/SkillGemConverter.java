package com.poe.provider.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poe.cache.model.SkillGem;

/**
 * 将 Wiki Cargo {@code skill_gems} 表行转换为 {@link SkillGem} 模型。
 * <p>
 * Cargo 字段映射：{@code skill_id}→name, {@code gem_tags}（逗号分隔列表）→gemTags,
 * {@code primary_attribute}→primaryAttribute, {@code max_level}→requiredLevel。
 * 注意：description / quality_stats / level_stats 不在 skill_gems 表，来自 skill 系列其他表。
 */
public class SkillGemConverter implements DataConverter<SkillGem> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public SkillGem convert(JsonNode row) {
        SkillGem gem = new SkillGem();

        gem.setId(ItemConverter.parseIntSafe(row, "_pageID"));
        gem.setName(row.path("skill_id").asText());
        gem.setNameZh(null);
        gem.setGemType(null);
        gem.setGemTags(row.path("gem_tags").asText());
        gem.setPrimaryAttribute(row.path("primary_attribute").asText());
        gem.setDescription(null);
        gem.setQualityStats(null);
        gem.setLevelStats(null);
        gem.setRequiredLevel(ItemConverter.parseIntSafe(row, "max_level"));
        gem.setVaalSkillGem(ItemConverter.parseBooleanSafe(row, "is_vaal_skill_gem"));
        gem.setSupportGemLetter(row.path("support_gem_letter").asText());
        gem.setSupportGemLetterHtml(row.path("support_gem_letter_html").asText());
        gem.setRequiresIntelligence(ItemConverter.parseBooleanSafe(row, "requires_intelligence"));
        gem.setRequiresDexterity(ItemConverter.parseBooleanSafe(row, "requires_dexterity"));
        gem.setRequiresStrength(ItemConverter.parseBooleanSafe(row, "requires_strength"));
        gem.setAwakenedVariantId(ItemConverter.nullableText(row, "awakened_variant_id"));
        gem.setRegularVariantId(ItemConverter.nullableText(row, "regular_variant_id"));
        gem.setVaalVariantId(ItemConverter.nullableText(row, "vaal_variant_id"));
        gem.setSecondarySkillId(ItemConverter.nullableText(row, "secondary_skill_id"));
        gem.setRuthlessSkillId(ItemConverter.nullableText(row, "ruthless_skill_id"));
        gem.setRuthlessSecondarySkillId(ItemConverter.nullableText(row, "ruthless_secondary_skill_id"));
        gem.setVersion("");

        return gem;
    }
}
