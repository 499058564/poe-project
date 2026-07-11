package com.poe.cache.model;

public class BlightTowers {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Tower cost */
    private int cost;
    /** Tower description */
    private String description;
    /** Tower icon */
    private String icon;
    /** Tower identifier */
    private String towerId;
    /** Tower display name */
    private String name;
    /** Tower radius */
    private int radius;
    /** Tower tier */
    private String tier;

    public BlightTowers() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getTowerId() { return towerId; }
    public void setTowerId(String towerId) { this.towerId = towerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getRadius() { return radius; }
    public void setRadius(int radius) { this.radius = radius; }
    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }
}
