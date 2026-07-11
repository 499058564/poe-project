package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Fossil;

/**
 * 将 Wiki Cargo {@code fossils} 表行转换为 {@link Fossil} 模型。
 * <p>
 * Cargo 包含 12 个字段，包括添加/禁止的标签、强制词缀、品质/附魔能力等。
 * 名称来自 _pageName。通过 _pageID 关联 base_items 表。
 */
public class FossilConverter implements DataConverter<Fossil> {

    @Override
    public Fossil convert(JsonNode row) {
        Fossil f = new Fossil();
        f.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        f.setPageName(row.path("_pageName").asText());
        f.setAddedModifierIds(ItemConverter.toJsonOrNull(row.path("added_modifier_ids")));
        f.setAllowedTags(ItemConverter.toJsonOrNull(row.path("allowed_tags")));
        f.setBaseItemId(ItemConverter.nullableText(row, "base_item_id"));
        f.setCanEnchant(ItemConverter.parseBooleanSafe(row, "can_enchant"));
        f.setCanMirror(ItemConverter.parseBooleanSafe(row, "can_mirror"));
        f.setCanQuality(ItemConverter.parseBooleanSafe(row, "can_quality"));
        f.setCanRollWhiteSockets(ItemConverter.parseBooleanSafe(row, "can_roll_white_sockets"));
        f.setCorruptedEssenceChance(ItemConverter.parseIntSafe(row, "corrupted_essence_chance"));
        f.setForbiddenTags(ItemConverter.toJsonOrNull(row.path("forbidden_tags")));
        f.setForcedModifierIds(ItemConverter.toJsonOrNull(row.path("forced_modifier_ids")));
        f.setLucky(ItemConverter.parseBooleanSafe(row, "is_lucky"));
        f.setSellPriceModifierIds(ItemConverter.toJsonOrNull(row.path("sell_price_modifier_ids")));
        return f;
    }
}
