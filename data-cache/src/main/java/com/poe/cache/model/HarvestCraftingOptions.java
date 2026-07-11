package com.poe.cache.model;

public class HarvestCraftingOptions {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Crafting option identifier */
    private String optionId;
    /** Primal lifeforce cost */
    private int costPrimal;
    /** Rancour lifeforce cost */
    private int costRancour;
    /** Sacred lifeforce cost */
    private int costSacred;
    /** Vivid lifeforce cost */
    private int costVivid;
    /** Wild lifeforce cost */
    private int costWild;
    /** Crafting effect text */
    private String effect;
    /** Crafting effect HTML */
    private String effectHtml;
    /** Ordinal position */
    private int ordinal;

    public HarvestCraftingOptions() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getOptionId() { return optionId; }
    public void setOptionId(String optionId) { this.optionId = optionId; }
    public int getCostPrimal() { return costPrimal; }
    public void setCostPrimal(int costPrimal) { this.costPrimal = costPrimal; }
    public int getCostRancour() { return costRancour; }
    public void setCostRancour(int costRancour) { this.costRancour = costRancour; }
    public int getCostSacred() { return costSacred; }
    public void setCostSacred(int costSacred) { this.costSacred = costSacred; }
    public int getCostVivid() { return costVivid; }
    public void setCostVivid(int costVivid) { this.costVivid = costVivid; }
    public int getCostWild() { return costWild; }
    public void setCostWild(int costWild) { this.costWild = costWild; }
    public String getEffect() { return effect; }
    public void setEffect(String effect) { this.effect = effect; }
    public String getEffectHtml() { return effectHtml; }
    public void setEffectHtml(String effectHtml) { this.effectHtml = effectHtml; }
    public int getOrdinal() { return ordinal; }
    public void setOrdinal(int ordinal) { this.ordinal = ordinal; }
}
