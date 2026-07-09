package com.poe.ui.theme;

/**
 * 主题资源路径常量，集中管理所有样式表引用。
 */
public final class ThemeConstants {

    private ThemeConstants() {
        // 工具类不允许实例化
    }

    /** 自定义 PoE 暗黑主题样式表（主入口，内含 @import 子文件） */
    public static final String DARK_THEME_CSS = "/theme/dark/theme.css";
}
