package com.poe.ui.constants;

/**
 * 布局/尺寸/视觉常量，消除布局代码中的魔法数字。
 */
public final class LayoutConstants {

    private LayoutConstants() {
        // 工具类不允许实例化
    }

    // ---- Window 尺寸 ----
    /** 主窗口默认宽度 */
    public static final double WINDOW_DEFAULT_WIDTH = 1280;
    /** 主窗口默认高度 */
    public static final double WINDOW_DEFAULT_HEIGHT = 800;
    /** 主窗口最小宽度 */
    public static final double WINDOW_MIN_WIDTH = 1024;
    /** 主窗口最小高度 */
    public static final double WINDOW_MIN_HEIGHT = 600;

    // ---- Sidebar 尺寸 ----
    /** 侧边栏展开宽度 */
    public static final double SIDEBAR_EXPANDED_WIDTH = 200;
    /** 侧边栏折叠宽度（仅显示图标） */
    public static final double SIDEBAR_COLLAPSED_WIDTH = 48;
    /** 折叠态按钮可用宽度 = 折叠宽度 - 两侧内边距 */
    public static final double SIDEBAR_ICON_PADDING = 16;
    /** 折叠按钮图标（三横线汉堡菜单） */
    public static final String COLLAPSE_ICON = "\u2630";

    // ---- NavButton 样式 ----
    /** 禁用按钮透明度 */
    public static final double NAV_BUTTON_DISABLED_OPACITY = 0.4;

    // ---- StatusBar 尺寸 ----
    /** 状态栏各元素水平间距 */
    public static final double STATUS_BAR_SPACING = 16;
    /** 同步进度条首选宽度 */
    public static final double STATUS_BAR_PROGRESS_PREF_WIDTH = 120;
    /** 同步进度条最大高度 */
    public static final double STATUS_BAR_PROGRESS_MAX_HEIGHT = 12;
    /** 同步进度条初始进度 */
    public static final double STATUS_BAR_PROGRESS_INITIAL = 0;
}
