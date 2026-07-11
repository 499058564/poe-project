package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.CraftingBenchOption;

/**
 * 将 Wiki Cargo {@code crafting_bench_options} 表行转换为 {@link CraftingBenchOption} 模型。
 * <p>
 * Cargo 包含 20 个字段，包括工艺名称、词缀类型、等级要求、解锁条件等。
 * 使用 Cargo 自带的 {@code id} 作为 option_id 主键。
 */
public class CraftingBenchOptionConverter implements DataConverter<CraftingBenchOption> {

    @Override
    public CraftingBenchOption convert(JsonNode row) {
        CraftingBenchOption o = new CraftingBenchOption();
        o.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        o.setPageName(row.path("_pageName").asText());
        o.setOptionId(ItemConverter.parseIntSafe(row, "id"));
        o.setName(ItemConverter.nullableText(row, "name"));
        o.setAffixType(ItemConverter.nullableText(row, "affix_type"));
        o.setModId(ItemConverter.nullableText(row, "mod_id"));
        o.setModGroup(ItemConverter.nullableText(row, "mod_group"));
        o.setRank(ItemConverter.parseIntSafe(row, "rank"));
        o.setRequiredLevel(ItemConverter.parseIntSafe(row, "required_level"));
        o.setNpc(ItemConverter.nullableText(row, "npc"));
        o.setDescription(ItemConverter.nullableText(row, "description"));
        o.setRecipeUnlockLocation(ItemConverter.nullableText(row, "recipe_unlock_location"));
        o.setUnlockCategory(ItemConverter.nullableText(row, "crafting_bench_unlock_category"));
        o.setUnlockCategoryDescription(ItemConverter.nullableText(row, "crafting_bench_unlock_category_description"));
        o.setItemClassCategories(ItemConverter.toJsonOrNull(row.path("item_class_categories")));
        o.setItemClasses(ItemConverter.toJsonOrNull(row.path("item_classes")));
        o.setItemClassesIds(ItemConverter.toJsonOrNull(row.path("item_classes_ids")));
        o.setLinks(ItemConverter.parseIntSafe(row, "links"));
        o.setOrdinal(ItemConverter.parseIntSafe(row, "ordinal"));
        o.setSocketColours(ItemConverter.nullableText(row, "socket_colours"));
        o.setSockets(ItemConverter.parseIntSafe(row, "sockets"));
        o.setUnveilsRequired(ItemConverter.parseIntSafe(row, "unveils_required"));
        return o;
    }
}
