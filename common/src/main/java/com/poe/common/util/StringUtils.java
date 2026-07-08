package com.poe.common.util;

/**
 * 全模块共享的字符串工具类。
 * 所有方法均已做空值安全处理。
 */
public final class StringUtils {

    private StringUtils() {}

    /**
     * 判断字符串是否为 {@code null} 或长度为 0。
     *
     * @param s 待检查的字符串
     * @return 为 null 或空串时返回 {@code true}
     */
    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * 判断字符串是否为 {@code null}、空串或仅包含空白字符。
     *
     * @param s 待检查的字符串
     * @return 为 null、空串或纯空白时返回 {@code true}
     */
    public static boolean isBlank(String s) {
        if (isEmpty(s)) {
            return true;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 标准化物品名称用于模糊匹配：去除首尾空白、压缩内部空白、转为小写。
     * <p>
     * 示例：{@code "Mage  Blood"} → {@code "mageblood"}。
     *
     * @param name 原始物品名称，可为 {@code null}
     * @return 标准化后的名称，{@code name} 为 null 时返回空串
     */
    public static String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().replaceAll("\\s+", "").toLowerCase();
    }
}
