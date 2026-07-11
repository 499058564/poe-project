package com.poe.cache.model;

public class SpawnWeight {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Ordinal position */
    private int ordinal;
    /** Spawn tag identifier */
    private String tag;
    /** Weight value */
    private int weight;

    public SpawnWeight() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public int getOrdinal() { return ordinal; }
    public void setOrdinal(int ordinal) { this.ordinal = ordinal; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
}
