package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.VendorReward;

/**
 * 将 Wiki Cargo {@code vendor_rewards} 子表行转换为 {@link VendorReward} 模型。
 * <p>
 * Cargo 字段：{@code act}, {@code class_ids}, {@code classes},
 * {@code npc}, {@code quest}, {@code quest_id}。
 * 物品名称来自 _pageName。通过 _pageID 关联 base_items 表。
 */
public class VendorRewardConverter implements DataConverter<VendorReward> {

    @Override
    public VendorReward convert(JsonNode row) {
        VendorReward r = new VendorReward();
        r.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        r.setPageName(row.path("_pageName").asText());
        r.setAct(ItemConverter.parseIntSafe(row, "act"));
        r.setClassIds(ItemConverter.toJsonOrNull(row.path("class_ids")));
        r.setClasses(ItemConverter.toJsonOrNull(row.path("classes")));
        r.setNpc(ItemConverter.nullableText(row, "npc"));
        r.setQuest(ItemConverter.nullableText(row, "quest"));
        r.setQuestId(ItemConverter.nullableText(row, "quest_id"));
        return r;
    }
}
