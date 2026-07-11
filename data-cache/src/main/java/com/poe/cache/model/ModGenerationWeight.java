package com.poe.cache.model;

/**
 * 词缀生成类型权重，映射 mod_generation_weights 表。
 * <p>
 * 记录词缀在不同生成类型（前缀/后缀/基底等）上的权重分布。
 * 通过 _pageID 与 mods 表关联。
 */
public class ModGenerationWeight {
    /** Wiki 页面 ID，关联 mods.id */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 排序序号 */
    private int ordinal;
    /** 生成类型标签，如 "prefix", "suffix" */
    private String tag;
    /** 该生成类型的权重值 */
    private int value;

    public ModGenerationWeight() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public int getOrdinal() { return ordinal; }
    public void setOrdinal(int ordinal) { this.ordinal = ordinal; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
}
