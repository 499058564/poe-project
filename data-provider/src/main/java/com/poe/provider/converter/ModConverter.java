package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Mod;

/**
 * 将 Wiki Cargo {@code mods} 表行转换为 {@link Mod} 模型。
 * <p>
 * Cargo 字段映射：{@code generation_type} 区分前缀/后缀/基底/附魔，
 * {@code spawn_weights}、{@code spawn_tags}、{@code stats} 以 JSON 字符串存储。
 */
public class ModConverter implements DataConverter<Mod> {

    @Override
    public Mod convert(JsonNode row) {
        Mod mod = new Mod();

        mod.setId(ItemConverter.parseIntSafe(row, "_pageID"));
        mod.setName(row.path("name").asText());
        mod.setNameZh(null);
        mod.setModType(row.path("generation_type").asText());
        mod.setDomain(row.path("domain").asText());
        mod.setGenerationType(row.path("generation_type").asText());
        mod.setModGroup(ItemConverter.nullToNull(row.path("mod_group").asText()));
        mod.setStats(ItemConverter.toJsonOrNull(row.path("stat_text")));
        mod.setSpawnTags(ItemConverter.toJsonOrNull(row.path("spawn_tags")));
        mod.setSpawnWeights(ItemConverter.toJsonOrNull(row.path("spawn_weights")));
        mod.setRequiredLevel(ItemConverter.parseIntSafe(row, "required_level"));
        mod.setVersion("");

        return mod;
    }
}
