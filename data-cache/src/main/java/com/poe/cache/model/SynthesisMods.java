package com.poe.cache.model;

public class SynthesisMods {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Item class IDs (comma-separated) */
    private String itemClassIds;
    /** Modifier IDs (comma-separated) */
    private String modIds;
    /** Stat identifier */
    private String statId;
    /** Stat display text */
    private String statText;
    /** Stat value */
    private double statValue;

    public SynthesisMods() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getItemClassIds() { return itemClassIds; }
    public void setItemClassIds(String itemClassIds) { this.itemClassIds = itemClassIds; }
    public String getModIds() { return modIds; }
    public void setModIds(String modIds) { this.modIds = modIds; }
    public String getStatId() { return statId; }
    public void setStatId(String statId) { this.statId = statId; }
    public String getStatText() { return statText; }
    public void setStatText(String statText) { this.statText = statText; }
    public double getStatValue() { return statValue; }
    public void setStatValue(double statValue) { this.statValue = statValue; }
}
