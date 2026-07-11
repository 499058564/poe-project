package com.poe.cache.model;

public class HeistEquipment {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Required job identifier */
    private String requiredJobId;
    /** Required job level */
    private int requiredJobLevel;

    public HeistEquipment() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getRequiredJobId() { return requiredJobId; }
    public void setRequiredJobId(String requiredJobId) { this.requiredJobId = requiredJobId; }
    public int getRequiredJobLevel() { return requiredJobLevel; }
    public void setRequiredJobLevel(int requiredJobLevel) { this.requiredJobLevel = requiredJobLevel; }
}
