package com.poe.cache.model;

public class SynthesisAreas {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Synthesis area identifier */
    private String areaId;
    /** Maximum area level */
    private int maxLevel;
    /** Minimum area level */
    private int minLevel;
    /** Area display name */
    private String name;
    /** Area size */
    private int size;
    /** Area weight for spawning */
    private int weight;

    public SynthesisAreas() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getAreaId() { return areaId; }
    public void setAreaId(String areaId) { this.areaId = areaId; }
    public int getMaxLevel() { return maxLevel; }
    public void setMaxLevel(int maxLevel) { this.maxLevel = maxLevel; }
    public int getMinLevel() { return minLevel; }
    public void setMinLevel(int minLevel) { this.minLevel = minLevel; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
}
