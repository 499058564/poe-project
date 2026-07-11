package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Mod;

/**
 * 将 Wiki Cargo {@code mods} 表行转换为 {@link Mod} 模型。
 * <p>
 * Cargo 字段：{@code id}, {@code name}, {@code domain}, {@code generation_type},
 * {@code mod_groups}→modGroup, {@code stat_text}→stats, {@code tags}→spawnTags,
 * {@code required_level}, {@code mod_type}, {@code tier_text}, {@code granted_buff_id}。
 * 注意：spawn_weights 不在 Cargo mods 表。
 */
public class ModConverter implements DataConverter<Mod> {

    /**
     * 将 Cargo 单行 JSON 转换为 Mod 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 词缀实体，stats 序列化为 JSON 字符串，spawn_weights 固定为 null
     */
    @Override
    public Mod convert(JsonNode row) {
        Mod mod = new Mod();

        mod.setId(ItemConverter.parseIntSafe(row, "_pageID"));
        mod.setName(row.path("name").asText());
        mod.setNameZh(null);
        mod.setModType(ItemConverter.nullableText(row, "mod_type"));
        mod.setDomain(row.path("domain").asText());
        mod.setGenerationType(row.path("generation_type").asText());
        mod.setModGroup(ItemConverter.nullableText(row, "mod_groups"));
        mod.setStats(ItemConverter.toJsonOrNull(row.path("stat_text")));
        mod.setSpawnTags(ItemConverter.nullableText(row, "tags"));
        mod.setSpawnWeights(null); // Cargo 无 spawn_weights
        mod.setRequiredLevel(ItemConverter.parseIntSafe(row, "required_level"));
        mod.setVersion("");

        return mod;
    }
}
