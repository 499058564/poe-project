package com.poe.cache.model;

/**
 * 异界地图属性，映射 maps 表。
 * <p>
 * 通过 _pageName 与 base_items 表关联。
 */
public class GameMap {
    private Integer pageId;
    private String pageName;
    private String areaId;
    private int areaLevel;
    private String guildCharacter;
    private String series;
    private int tier;
    private String uniqueAreaId;
    private int uniqueAreaLevel;
    private String uniqueGuildCharacter;

    public GameMap() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getAreaId() { return areaId; }
    public void setAreaId(String areaId) { this.areaId = areaId; }

    public int getAreaLevel() { return areaLevel; }
    public void setAreaLevel(int areaLevel) { this.areaLevel = areaLevel; }

    public String getGuildCharacter() { return guildCharacter; }
    public void setGuildCharacter(String guildCharacter) { this.guildCharacter = guildCharacter; }

    public String getSeries() { return series; }
    public void setSeries(String series) { this.series = series; }

    public int getTier() { return tier; }
    public void setTier(int tier) { this.tier = tier; }

    public String getUniqueAreaId() { return uniqueAreaId; }
    public void setUniqueAreaId(String uniqueAreaId) { this.uniqueAreaId = uniqueAreaId; }

    public int getUniqueAreaLevel() { return uniqueAreaLevel; }
    public void setUniqueAreaLevel(int uniqueAreaLevel) { this.uniqueAreaLevel = uniqueAreaLevel; }

    public String getUniqueGuildCharacter() { return uniqueGuildCharacter; }
    public void setUniqueGuildCharacter(String uniqueGuildCharacter) { this.uniqueGuildCharacter = uniqueGuildCharacter; }
}
