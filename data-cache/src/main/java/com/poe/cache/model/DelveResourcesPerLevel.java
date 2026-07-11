package com.poe.cache.model;

public class DelveResourcesPerLevel {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Area level */
    private int areaLevel;
    /** Sulphite amount */
    private int sulphite;

    public DelveResourcesPerLevel() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public int getAreaLevel() { return areaLevel; }
    public void setAreaLevel(int areaLevel) { this.areaLevel = areaLevel; }
    public int getSulphite() { return sulphite; }
    public void setSulphite(int sulphite) { this.sulphite = sulphite; }
}
