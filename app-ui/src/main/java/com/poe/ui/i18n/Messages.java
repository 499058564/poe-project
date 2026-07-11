package com.poe.ui.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 国际化消息访问器，当前固定为简体中文。
 * <p>
 * 按领域拆分为多个子文件（app / nav / status / placeholder），
 * 避免单文件过大。扩展新语言时只需添加对应的 {@code _en.properties} 文件。
 */
public final class Messages {

    /** 当前语言环境（后续可改为运行时切换） */
    private static final Locale LOCALE = Locale.SIMPLIFIED_CHINESE;

    /** 按领域拆分的 ResourceBundle 文件名列表 */
    private static final String[] BUNDLE_NAMES = {
        "i18n/app",
        "i18n/nav",
        "i18n/status",
        "i18n/placeholder",
        "i18n/search",
    };

    /** 对应 BUNDLE_NAMES 加载的 ResourceBundle 实例 */
    private static final ResourceBundle[] BUNDLES;

    static {
        BUNDLES = new ResourceBundle[BUNDLE_NAMES.length];
        for (int i = 0; i < BUNDLE_NAMES.length; i++) {
            BUNDLES[i] = ResourceBundle.getBundle(BUNDLE_NAMES[i], LOCALE);
        }
    }

    private Messages() {
        // 工具类不允许实例化
    }

    /**
     * 在所有子文件中查找并返回原始字符串。
     *
     * @param key i18n 资源键
     * @return 对应语言的翻译文本
     * @throws MissingResourceException 如果 key 在所有子文件中都不存在
     */
    public static String get(String key) {
        for (ResourceBundle bundle : BUNDLES) {
            if (bundle.containsKey(key)) {
                return bundle.getString(key);
            }
        }
        throw new MissingResourceException(
            "Can't find resource for key: " + key,
            Messages.class.getName(), key);
    }

    /**
     * 获取格式化字符串，使用 {@link MessageFormat} 替换占位符。
     *
     * @param key  i18n 资源键
     * @param args 格式化参数，替换模式中的 {0}, {1} 等占位符
     * @return 格式化后的翻译文本
     */
    public static String fmt(String key, Object... args) {
        return MessageFormat.format(get(key), args);
    }
}
