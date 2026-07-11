package com.poe.cache.model;

/**
 * 物品出售价格，映射 item_sell_prices 表。
 * <p>
 * 记录物品卖给商人可获得的通货数量。
 * 通过 _pageID 与 base_items 表关联。
 */
public class ItemSellPrice {
    /** Wiki 页面 ID，关联 base_items.id */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 通货数量 */
    private int amount;
    /** 通货类型名称，如 "Scroll Fragment" */
    private String currencyName;

    public ItemSellPrice() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getCurrencyName() { return currencyName; }
    public void setCurrencyName(String currencyName) { this.currencyName = currencyName; }
}
