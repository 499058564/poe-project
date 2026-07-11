package com.poe.cache.model;

public class HeistNpcStats {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** NPC identifier */
    private String npcId;
    /** Stat identifier */
    private String statId;
    /** Stat value */
    private double value;

    public HeistNpcStats() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getNpcId() { return npcId; }
    public void setNpcId(String npcId) { this.npcId = npcId; }
    public String getStatId() { return statId; }
    public void setStatId(String statId) { this.statId = statId; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
