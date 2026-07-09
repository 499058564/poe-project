package com.poe.ui.constants;

/**
 * CSS 样式类名常量，确保 Java 代码与 CSS 选择器保持一致。
 */
public final class StyleClasses {

    private StyleClasses() {
        // 工具类不允许实例化
    }

    // ---- Sidebar 组件 ----
    /** 侧边栏容器 */
    public static final String SIDEBAR = "sidebar";
    /** 折叠/展开切换按钮 */
    public static final String COLLAPSE_BTN = "collapse-btn";
    /** 导航按钮（ToggleButton 子类） */
    public static final String NAV_BUTTON = "nav-button";
    /** 二级菜单子项按钮 */
    public static final String NAV_SUB_ITEM = "nav-sub-item";

    // ---- ContentArea 组件 ----
    /** 多标签内容区容器 */
    public static final String CONTENT_AREA = "content-area";
    /** 功能页占位面板 */
    public static final String PAGE_PLACEHOLDER = "page-placeholder";
    /** 占位提示标签 */
    public static final String PLACEHOLDER_LABEL = "placeholder-label";

    // ---- StatusBar 组件 ----
    /** 底部状态栏容器 */
    public static final String STATUS_BAR = "status-bar";
    /** 状态栏文本标签（版本/时间/状态） */
    public static final String STATUS_LABEL = "status-label";
}
