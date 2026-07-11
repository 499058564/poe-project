package com.poe.cache.model;

/**
 * 通货/可堆叠物品属性，映射 stackables 表。
 * <p>
 * 通过 _pageName 与 base_items 表关联。
 */
public class Stackable {
    private Integer pageId;
    private String pageName;
    private int stackSize;
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
