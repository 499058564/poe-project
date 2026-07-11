package com.poe.cache.model;

/**
 * 精华，映射 essences 表。
 * <p>
 * 记录精华物品的分类、等级和类型信息。名称来自 _pageName。
 * 通过 _pageID 与 base_items 表关联。
 */
public class Essence {
    /** Wiki 页面 ID，关联 base_items.id */
    private Integer pageId;
    /** Wiki 页面名称（_pageName），即精华名称 */
    private String pageName;
    /** 精华分类 */
    private String category;
    /** 精华等级 */
    private int level;
    /** 使用所需的最低等级 */
    private int levelRestriction;
    /** 精华类型编号 */
    private int type;

    public Essence() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getLevelRestriction() { return levelRestriction; }
    public void setLevelRestriction(int levelRestriction) { this.levelRestriction = levelRestriction; }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
}
