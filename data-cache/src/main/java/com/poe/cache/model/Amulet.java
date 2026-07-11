package com.poe.cache.model;

/**
 * 项链特有属性，映射 amulets 表。
 * <p>
 * 通过 _pageName 与 base_items 表关联。
 */
public class Amulet {
    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName），关联 base_items */
    private String pageName;
    /** 是否为魔符（Talisman） */
    private boolean isTalisman;
    /** 魔符层级（1-4） */
    private int talismanTier;

    public Amulet() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public boolean isTalisman() { return isTalisman; }
    public void setTalisman(boolean talisman) { isTalisman = talisman; }

    public int getTalismanTier() { return talismanTier; }
    public void setTalismanTier(int talismanTier) { this.talismanTier = talismanTier; }
}
