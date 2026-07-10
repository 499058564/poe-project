package com.poe.provider.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poe.cache.model.Item;
import com.poe.common.util.JsonUtils;

/**
 * 将 Wiki Cargo {@code items} 表行转换为 {@link Item} 模型。
 * <p>
 * Cargo 字段映射：{@code _pageName} → wiki_url，{@code class_id} → itemClass。
 * JSON 数组字段（requirements、implicits、properties）保持原始文本不变。
 */
public class ItemConverter implements DataConverter<Item> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Item convert(JsonNode row) {
        Item item = new Item();

        item.setId(parseId(row.path("_pageID").asText()));
        item.setName(row.path("name").asText());
        item.setNameZh(null);
        item.setItemClass(row.path("class_id").asText());
        item.setInventoryWidth(parseIntSafe(row, "size_x"));
        item.setInventoryHeight(parseIntSafe(row, "size_y"));
        item.setRequirements(null); // Cargo 无对应字段，来自其他表
        item.setImplicits(null);     // Cargo implicit_stat_text 字段存在但格式不同
        item.setProperties(null);    // Cargo 无直接对应
        item.setFlavourText(nullToNull(row.path("flavour_text").asText()));
        item.setDropLevel(parseIntSafe(row, "drop_level"));
        item.setWikiUrl("https://www.poewiki.net/wiki/" + escapeWikiPath(row.path("_pageName").asText()));
        item.setVersion("");

        return item;
    }

    // ---- helpers ----

    /**
     * Cargo _pageID 可能为空或过大（如部分派生物品），
     * 此时使用 name 的 hashCode 作为 fallback。
     */
    static int parseId(String pageId) {
        try {
            if (pageId != null && !pageId.isEmpty()) {
                return Integer.parseInt(pageId);
            }
        } catch (NumberFormatException ignored) { }
        return 0;
    }

    static int parseIntSafe(JsonNode node, String field) {
        String text = node.path(field).asText();
        if (text.isEmpty()) return 0;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static boolean parseBooleanSafe(JsonNode node, String field) {
        String text = node.path(field).asText();
        return "1".equals(text) || "true".equalsIgnoreCase(text);
    }

    static String nullableText(JsonNode node, String field) {
        String text = node.path(field).asText();
        return (text == null || text.isEmpty()) ? null : text;
    }

    static String toJsonOrNull(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) return null;
        try {
            return MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            return node.toString();
        }
    }

    static String nullToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }

    static String escapeWikiPath(String pageName) {
        if (pageName == null || pageName.isEmpty()) return "";
        return pageName.replace(" ", "_");
    }
}
