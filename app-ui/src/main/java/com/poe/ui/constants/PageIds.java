package com.poe.ui.constants;

/**
 * 功能页面标识符常量，集中管理避免字符串散落。
 */
public final class PageIds {

    private PageIds() {
        // 工具类不允许实例化
    }

    /** 物品搜索 */
    public static final String ITEM_SEARCH = "item-search";
    /** 技能宝石 */
    public static final String SKILL_GEMS = "skill-gems";
    /** 天赋树 */
    public static final String PASSIVE_TREE = "passive-tree";
    /** 装备模拟 */
    public static final String GEAR_SIM = "gear-sim";
    /** 设置 */
    public static final String SETTINGS = "settings";

    /** 应用启动时默认打开的页面 */
    public static final String DEFAULT_PAGE = ITEM_SEARCH;
}
