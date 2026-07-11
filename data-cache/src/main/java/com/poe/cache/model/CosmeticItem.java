package com.poe.cache.model;

public class CosmeticItem {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Cosmetic type category */
    private String cosmeticType;
    /** Target items/classes (comma-separated) */
    private String target;
    /** Cosmetic theme */
    private String theme;

    public CosmeticItem() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getCosmeticType() { return cosmeticType; }
    public void setCosmeticType(String cosmeticType) { this.cosmeticType = cosmeticType; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
}
