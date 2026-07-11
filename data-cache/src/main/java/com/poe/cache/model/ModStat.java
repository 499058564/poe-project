package com.poe.cache.model;

/**
 * 词缀属性值，映射 mod_stats 表。
 * <p>
 * 一条词缀可包含多条属性（stat），每条属性有 min/max 范围值和唯一标识 id。
 * 通过 _pageID 与 mods 表（id）关联。
 */
public class ModStat {
    /** Wiki 页面 ID，关联 mods.id */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 属性标识符，如 "base_maximum_life" */
    private String statId;
    /** 属性最小值 */
    private int minValue;
    /** 属性最大值 */
    private int maxValue;

    public ModStat() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getStatId() { return statId; }
    public void setStatId(String statId) { this.statId = statId; }

    public int getMinValue() { return minValue; }
    public void setMinValue(int minValue) { this.minValue = minValue; }

    public int getMaxValue() { return maxValue; }
    public void setMaxValue(int maxValue) { this.maxValue = maxValue; }
}
