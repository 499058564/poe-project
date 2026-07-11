package com.poe.cache.model;

public class Pantheon {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** God name */
    private String godName;
    /** Whether this is a major god */
    private boolean isMajorGod;

    public Pantheon() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getGodName() { return godName; }
    public void setGodName(String godName) { this.godName = godName; }
    public boolean isMajorGod() { return isMajorGod; }
    public void setMajorGod(boolean majorGod) { this.isMajorGod = majorGod; }
}
