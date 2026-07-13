package com.poe.cache.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 轻量 JSON 工具，用于 List&lt;String&gt; ↔ TEXT 列互转。
 * 不依赖第三方 JSON 库 — 使用简单的字符串拼接与解析。
 */
public final class JsonUtil {

    private JsonUtil() {}

    /**
     * 将 List&lt;String&gt; 序列化为 JSON 数组字符串。
     * @param list 字符串列表，可为 null 或空
     * @return JSON 数组字符串，如 {@code "[\"a\",\"b\"]"}；null/empty 返回 {@code "[]"}
     */
    public static String toJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append('"').append(escape(list.get(i))).append('"');
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 将 JSON 数组字符串反序列化为 List&lt;String&gt;。
     * @param json JSON 数组字符串，可为 null 或空串
     * @return 字符串列表（永不为 null）
     */
    public static List<String> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        String trimmed = json.trim();
        if (trimmed.equals("[]") || trimmed.length() < 3) {
            return result;
        }
        // strip leading "[" and trailing "]"
        String inner = trimmed.substring(1, trimmed.length() - 1);
        int i = 0;
        while (i < inner.length()) {
            // skip commas/spaces
            while (i < inner.length() && (inner.charAt(i) == ',' || inner.charAt(i) == ' ')) {
                i++;
            }
            if (i >= inner.length()) break;
            // expect opening quote
            if (inner.charAt(i) == '"') {
                int start = i + 1;
                int end = start;
                while (end < inner.length()) {
                    if (inner.charAt(end) == '"' && (end == start || inner.charAt(end - 1) != '\\')) {
                        break;
                    }
                    end++;
                }
                String value = unescape(inner.substring(start, end));
                result.add(value);
                i = end + 1; // skip closing quote
            } else {
                int comma = inner.indexOf(',', i);
                if (comma < 0) comma = inner.length();
                result.add(inner.substring(i, comma).trim());
                i = comma;
            }
        }
        return result;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String unescape(String s) {
        return s.replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\\", "\\");
    }
}
