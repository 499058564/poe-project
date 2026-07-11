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

    /** JSON 序列化工具，用于将数组字段转为 JSON 字符串 */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 将 Cargo 单行 JSON 转换为 Item 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 物品实体，字段缺失时使用默认值（0 / "" / null）
     */
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

    // ---- 公开辅助方法（被其他 Converter 复用） ----

    /**
     * 解析 Cargo 的 _pageID 字符串为整数。
     * <p>
     * _pageID 可能为空或格式异常（如部分派生物品），
     * 此时返回 0 作为 fallback。
     *
     * @param pageId _pageID 原始字符串
     * @return 解析后的整数 ID，解析失败返回 0
     */
    static int parseId(String pageId) {
        try {
            if (pageId != null && !pageId.isEmpty()) {
                return Integer.parseInt(pageId);
            }
        } catch (NumberFormatException ignored) { }
        return 0;
    }

    /** 安全解析 int 字段，空值或解析失败返回 0 */
    static int parseIntSafe(JsonNode node, String field) {
        String text = node.path(field).asText();
        if (text.isEmpty()) return 0;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** 安全解析 boolean 字段，支持 "1" / "true"（不区分大小写） */
    static boolean parseBooleanSafe(JsonNode node, String field) {
        String text = node.path(field).asText();
        return "1".equals(text) || "true".equalsIgnoreCase(text);
    }

    /** 安全解析 double 字段，空值或解析失败返回 0.0 */
    static double parseDoubleSafe(JsonNode node, String field) {
        String text = node.path(field).asText();
        if (text.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /** 读取文本字段，空字符串返回 null */
    static String nullableText(JsonNode node, String field) {
        String text = node.path(field).asText();
        return (text == null || text.isEmpty()) ? null : text;
    }

    /** 将 JSON 节点序列化为字符串，缺失或 null 节点返回 null */
    static String toJsonOrNull(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) return null;
        try {
            return MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            return node.toString();
        }
    }

    /** null 或空字符串统一返回 null */
    static String nullToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }

    /** 将 Wiki 页面名的空格替换为下划线，用于拼接 wiki URL */
    static String escapeWikiPath(String pageName) {
        if (pageName == null || pageName.isEmpty()) return "";
        return pageName.replace(" ", "_");
    }
}
