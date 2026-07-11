package com.poe.cache.model;

/**
 * 异界地图属性，映射 maps 表。
 * <p>
 * 通过 _pageName 与 base_items 表关联。
 */
public class GameMap {
    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName），关联 base_items */
    private String pageName;
    /** 区域内部 ID，对应游戏数据中的区域标识 */
    private String areaId;
    /** 地图怪物等级 */
    private int areaLevel;
    /** 地图 BOSS 角色名称 */
    private String guildCharacter;
    /** 所属地图系列名称 */
    private String series;
    /** 地图位阶（1-16，按 Atlas 排序） */
    private int tier;
    /** 传奇版本区域 ID（如有） */
    private String uniqueAreaId;
    /** 传奇版本怪物等级 */
    private int uniqueAreaLevel;
    /** 传奇版本 BOSS 角色名称 */
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
