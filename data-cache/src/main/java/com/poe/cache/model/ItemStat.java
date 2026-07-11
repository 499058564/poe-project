package com.poe.cache.model;

/**
 * 物品固定属性值，映射 item_stats 表。
 * <p>
 * 记录暗金等物品的固定词缀属性值，包含 min/max/avg 范围。
 * 通过 _pageID 与 base_items 表关联。
 */
public class ItemStat {
    /** Wiki 页面 ID，关联 base_items.id */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 属性平均值 */
    private int avg;
    /** 属性标识符，如 "base_maximum_life" */
    private String statId;
    /** 属性最大值 */
    private int maxValue;
    /** 属性最小值 */
    private int minValue;
    /** 关联的词缀 ID */
    private String modId;

    public ItemStat() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public int getAvg() { return avg; }
    public void setAvg(int avg) { this.avg = avg; }

    public String getStatId() { return statId; }
    public void setStatId(String statId) { this.statId = statId; }

    public int getMaxValue() { return maxValue; }
    public void setMaxValue(int maxValue) { this.maxValue = maxValue; }

    public int getMinValue() { return minValue; }
    public void setMinValue(int minValue) { this.minValue = minValue; }

    public String getModId() { return modId; }
    public void setModId(String modId) { this.modId = modId; }
}
