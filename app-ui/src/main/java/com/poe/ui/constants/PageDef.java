package com.poe.ui.constants;

import com.poe.ui.i18n.keys.NavKeys;

/**
 * 应用功能页面定义枚举。
 * 集中管理页面 ID、i18n key、初始禁用状态，
 * 由 MainWindow 统一注入到 Sidebar 和 ContentArea。
 */
public enum PageDef {

    ITEM_SEARCH(PageIds.ITEM_SEARCH, NavKeys.ITEM_SEARCH, false),
    SKILL_GEMS(PageIds.SKILL_GEMS, NavKeys.SKILL_GEMS, false),
    PASSIVE_TREE(PageIds.PASSIVE_TREE, NavKeys.PASSIVE_TREE, false),
    GEAR_SIM(PageIds.GEAR_SIM, NavKeys.GEAR_SIM, true),
    SETTINGS(PageIds.SETTINGS, NavKeys.SETTINGS, false);

    private final String pageId;
    private final String i18nKey;
    private final boolean disabled;

    PageDef(String pageId, String i18nKey, boolean disabled) {
        this.pageId = pageId;
        this.i18nKey = i18nKey;
        this.disabled = disabled;
    }

    public String pageId() { return pageId; }

    public String i18nKey() { return i18nKey; }

    /** 该页面在导航中是否默认禁用 */
    public boolean disabled() { return disabled; }
}
