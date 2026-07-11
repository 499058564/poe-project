package com.poe.cache.model;

/**
 * 化石权重，映射 fossil_weights 表。
 * <p>
 * 记录化石对各类词缀标签的出现权重影响。通过 base_item_id 关联化石物品。
 */
public class FossilWeight {
    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 化石基础物品 ID，关联 fossils.base_item_id */
    private String baseItemId;
    /** 排序序号 */
    private int ordinal;
    /** 词缀标签 */
    private String tag;
    /** 权重类型：increased / decreased / more / less */
    private String weightType;
    /** 权重值 */
    private int weight;

    public FossilWeight() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getBaseItemId() { return baseItemId; }
    public void setBaseItemId(String baseItemId) { this.baseItemId = baseItemId; }

    public int getOrdinal() { return ordinal; }
    public void setOrdinal(int ordinal) { this.ordinal = ordinal; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getWeightType() { return weightType; }
    public void setWeightType(String weightType) { this.weightType = weightType; }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
}
