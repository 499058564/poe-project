package com.poe.cache.model;

public class SynthesisGlobalMods {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Maximum area level */
    private int maxLevel;
    /** Minimum area level */
    private int minLevel;
    /** Modifier identifier */
    private String modId;
    /** Spawn weight */
    private int weight;

    public SynthesisGlobalMods() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public int getMaxLevel() { return maxLevel; }
    public void setMaxLevel(int maxLevel) { this.maxLevel = maxLevel; }
    public int getMinLevel() { return minLevel; }
    public void setMinLevel(int minLevel) { this.minLevel = minLevel; }
    public String getModId() { return modId; }
    public void setModId(String modId) { this.modId = modId; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
}
