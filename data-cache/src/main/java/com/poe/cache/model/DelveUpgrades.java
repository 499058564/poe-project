package com.poe.cache.model;

public class DelveUpgrades {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Upgrade cost */
    private int cost;
    /** Upgrade level */
    private int level;
    /** Upgrade type */
    private String type;

    public DelveUpgrades() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
