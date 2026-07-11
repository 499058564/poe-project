package com.poe.cache.model;

/**
 * 词缀生成权重，映射 mod_spawn_weights 表。
 * <p>
 * 记录每个词缀在各类物品标签上的出现权重。按 ordinal 排序。
 * 通过 _pageID 与 mods 表关联。
 */
public class ModSpawnWeight {
    /** Wiki 页面 ID，关联 mods.id */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 排序序号 */
    private int ordinal;
    /** 物品标签，如 "ring", "amulet" */
    private String tag;
    /** 该标签下的生成权重值 */
    private int value;

    public ModSpawnWeight() {}

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
