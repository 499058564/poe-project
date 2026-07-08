package com.poe.ui.enums;

import com.poe.ui.constants.PageIds;
import com.poe.ui.i18n.keys.NavKeys;

/**
 * 应用功能页面定义枚举。
 * 集中管理页面 ID、i18n key、初始禁用状态、父子层级，
 * 由 MainWindow 统一注入到 Sidebar 和 ContentArea。
 */
public enum PageDefEnum {

    // ---- 一级菜单 ----
    ITEM_SEARCH(PageIds.ITEM_SEARCH, NavKeys.ITEM_SEARCH, false, null),
    // ---- 物品搜索子页 ----
    ITEM_BASE(PageIds.ITEM_BASE, NavKeys.ITEM_BASE, false, PageIds.ITEM_SEARCH),
    ITEM_UNIQUE(PageIds.ITEM_UNIQUE, NavKeys.ITEM_UNIQUE, false, PageIds.ITEM_SEARCH),
    ITEM_CURRENCY(PageIds.ITEM_CURRENCY, NavKeys.ITEM_CURRENCY, false, PageIds.ITEM_SEARCH),
    // ---- 一级菜单 ----
    SKILL_GEMS(PageIds.SKILL_GEMS, NavKeys.SKILL_GEMS, false, null),
    PASSIVE_TREE(PageIds.PASSIVE_TREE, NavKeys.PASSIVE_TREE, false, null),
    // ---- 一级菜单（含子页） ----
    GEAR_SIM(PageIds.GEAR_SIM, NavKeys.GEAR_SIM, true, null),
    // ---- 装备模拟子页 ----
    GEAR_EQUIPMENT(PageIds.GEAR_EQUIPMENT, NavKeys.GEAR_EQUIPMENT, true, PageIds.GEAR_SIM),
    GEAR_JEWELS(PageIds.GEAR_JEWELS, NavKeys.GEAR_JEWELS, true, PageIds.GEAR_SIM),
    GEAR_FLASKS(PageIds.GEAR_FLASKS, NavKeys.GEAR_FLASKS, true, PageIds.GEAR_SIM),
    // ---- 一级菜单 ----
    SETTINGS(PageIds.SETTINGS, NavKeys.SETTINGS, false, null);

    private final String pageId;
    private final String i18nKey;
    private final boolean disabled;
    /** 父页面 ID，{@code null} 表示一级菜单 */
    private final String parentPageId;

    PageDefEnum(String pageId, String i18nKey, boolean disabled, String parentPageId) {
        this.pageId = pageId;
        this.i18nKey = i18nKey;
        this.disabled = disabled;
        this.parentPageId = parentPageId;
    }

    public String pageId() { return pageId; }

    public String i18nKey() { return i18nKey; }

    /** 该页面在导航中是否默认禁用 */
    public boolean disabled() { return disabled; }

    /** 父页面 ID，{@code null} 表示一级菜单 */
    public String parentPageId() { return parentPageId; }

    /** 是否为一级菜单（无父页面） */
    public boolean isTopLevel() { return parentPageId == null; }
}
