package com.poe.cache.model;

public class BlightItems {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Item tier */
    private String tier;

    public BlightItems() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }
}
