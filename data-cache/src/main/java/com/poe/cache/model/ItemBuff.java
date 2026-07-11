package com.poe.cache.model;

/**
 * 物品 Buff 效果，映射 item_buffs 表。
 * <p>
 * 记录药剂等物品使用后提供的临时 Buff 效果，如抗性增加、伤害提升等。
 * 通过 _pageID 与 base_items 表关联。
 */
public class ItemBuff {
    /** Wiki 页面 ID，关联 base_items.id */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** Buff 数值列表（逗号分隔），如 "40,5" */
    private String buffValues;
    /** Buff 图标文件名 */
    private String icon;
    /** Buff 内部标识符，如 "flask_utility_resist_chaos" */
    private String buffId;
    /** Buff 效果文本描述 */
    private String statText;

    public ItemBuff() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getBuffValues() { return buffValues; }
    public void setBuffValues(String buffValues) { this.buffValues = buffValues; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getBuffId() { return buffId; }
    public void setBuffId(String buffId) { this.buffId = buffId; }

    public String getStatText() { return statText; }
    public void setStatText(String statText) { this.statText = statText; }
}
