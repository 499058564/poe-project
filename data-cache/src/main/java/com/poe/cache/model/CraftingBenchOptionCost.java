package com.poe.cache.model;

/**
 * 工艺台选项消耗，映射 crafting_bench_options_costs 表。
 * <p>
 * 记录工艺台词缀制作所需的通货消耗明细。
 * 通过 option_id 与 crafting_bench_options.id 关联。
 */
public class CraftingBenchOptionCost {
    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 关联的工艺选项 ID */
    private int optionId;
    /** 通货数量 */
    private int amount;
    /** 通货类型名称，如 "Orb of Alteration" */
    private String currencyName;

    public CraftingBenchOptionCost() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public int getOptionId() { return optionId; }
    public void setOptionId(int optionId) { this.optionId = optionId; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getCurrencyName() { return currencyName; }
    public void setCurrencyName(String currencyName) { this.currencyName = currencyName; }
}
