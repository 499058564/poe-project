package com.poe.cache.model;

/**
 * 词缀出售价格，映射 mod_sell_prices 表。
 * <p>
 * 记录词缀在商人处的出售价格（获得该词缀的物品可卖多少通货）。
 * 通过 _pageID 与 mods 表关联。
 */
public class ModSellPrice {
    /** Wiki 页面 ID，关联 mods.id */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 通货数量 */
    private int amount;
    /** 通货类型名称，如 "Orb of Alteration" */
    private String currencyName;

    public ModSellPrice() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getCurrencyName() { return currencyName; }
    public void setCurrencyName(String currencyName) { this.currencyName = currencyName; }
}
