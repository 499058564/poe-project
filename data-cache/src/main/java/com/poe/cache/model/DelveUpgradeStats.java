package com.poe.cache.model;

public class DelveUpgradeStats {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Cargo record ID */
    private String cargoId;
    /** Stat level */
    private int level;
    /** Stat type */
    private String type;
    /** Stat value */
    private double value;

    public DelveUpgradeStats() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getCargoId() { return cargoId; }
    public void setCargoId(String cargoId) { this.cargoId = cargoId; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
