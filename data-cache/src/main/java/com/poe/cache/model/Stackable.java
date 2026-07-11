package com.poe.cache.model;

/**
 * 通货/可堆叠物品属性，映射 stackables 表。
 * <p>
 * 通过 _pageName 与 base_items 表关联。
 */
public class Stackable {
    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName），关联 base_items */
    private String pageName;
    /** 默认最大堆叠数量 */
    private int stackSize;
    /** 通货仓库页内的最大堆叠数量 */
    private int stackSizeCurrencyTab;

    public Stackable() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public int getStackSize() { return stackSize; }
    public void setStackSize(int stackSize) { this.stackSize = stackSize; }

    public int getStackSizeCurrencyTab() { return stackSizeCurrencyTab; }
    public void setStackSizeCurrencyTab(int stackSizeCurrencyTab) { this.stackSizeCurrencyTab = stackSizeCurrencyTab; }
}
