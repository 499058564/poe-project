package com.poe.cache.model;

/**
 * 物品关联词缀，映射 item_mods 表。
 * <p>
 * 记录物品与词缀的关联关系，区分显式/隐式/地图碎片奖励等类型。
 * 通过 _pageID 与 base_items 表关联。
 */
public class ItemMod {
    /** Wiki 页面 ID，关联 base_items.id */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 词缀 ID（Cargo 字段 id） */
    private String modId;
    /** 是否为显式词缀 */
    private boolean explicit;
    /** 是否为隐式词缀 */
    private boolean implicit;
    /** 是否为地图碎片奖励词缀 */
    private boolean mapFragmentBonus;
    /** 是否为随机词缀 */
    private boolean random;
    /** 词缀文本描述 */
    private String text;

    public ItemMod() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getModId() { return modId; }
    public void setModId(String modId) { this.modId = modId; }

    public boolean isExplicit() { return explicit; }
    public void setExplicit(boolean explicit) { this.explicit = explicit; }

    public boolean isImplicit() { return implicit; }
    public void setImplicit(boolean implicit) { this.implicit = implicit; }

    public boolean isMapFragmentBonus() { return mapFragmentBonus; }
    public void setMapFragmentBonus(boolean mapFragmentBonus) { this.mapFragmentBonus = mapFragmentBonus; }

    public boolean isRandom() { return random; }
    public void setRandom(boolean random) { this.random = random; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
