package com.poe.cache.model;

/**
 * 异界图鉴节点实体，映射 atlas_nodes 表。
 * <p>
 * 存储异界图鉴中每个地图节点的区域、层级、连接与命运卡信息。
 */
public class AtlasNode {

    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 关联区域 ID */
    private String areaId;
    /** 连接节点 ID 列表 */
    private String connections;
    /** 可掉落命运卡 ID 列表 */
    private String divCards;
    /** 图鉴节点 ID */
    private String id;
    /** 是否不在异界图鉴上 */
    private boolean isOffAtlas;
    /** 区域连接 0 */
    private String regionConnections0;
    /** 区域连接 1 */
    private String regionConnections1;
    /** 区域连接 2 */
    private String regionConnections2;
    /** 区域连接 3 */
    private String regionConnections3;
    /** 区域连接 4 */
    private String regionConnections4;
    /** 所属区域 ID */
    private String regionId;
    /** 区域最低要求 */
    private int regionMinimum;
    /** 系列 ID */
    private int seriesId;
    /** 层级 0 */
    private int tier0;
    /** 层级 1 */
    private int tier1;
    /** 层级 2 */
    private int tier2;
    /** 层级 3 */
    private int tier3;
    /** 层级 4 */
    private int tier4;

    public AtlasNode() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getAreaId() { return areaId; }
    public void setAreaId(String areaId) { this.areaId = areaId; }

    public String getConnections() { return connections; }
    public void setConnections(String connections) { this.connections = connections; }

    public String getDivCards() { return divCards; }
    public void setDivCards(String divCards) { this.divCards = divCards; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public boolean isOffAtlas() { return isOffAtlas; }
    public void setOffAtlas(boolean offAtlas) { isOffAtlas = offAtlas; }

    public String getRegionConnections0() { return regionConnections0; }
    public void setRegionConnections0(String regionConnections0) { this.regionConnections0 = regionConnections0; }

    public String getRegionConnections1() { return regionConnections1; }
    public void setRegionConnections1(String regionConnections1) { this.regionConnections1 = regionConnections1; }

    public String getRegionConnections2() { return regionConnections2; }
    public void setRegionConnections2(String regionConnections2) { this.regionConnections2 = regionConnections2; }

    public String getRegionConnections3() { return regionConnections3; }
    public void setRegionConnections3(String regionConnections3) { this.regionConnections3 = regionConnections3; }

    public String getRegionConnections4() { return regionConnections4; }
    public void setRegionConnections4(String regionConnections4) { this.regionConnections4 = regionConnections4; }

    public String getRegionId() { return regionId; }
    public void setRegionId(String regionId) { this.regionId = regionId; }

    public int getRegionMinimum() { return regionMinimum; }
    public void setRegionMinimum(int regionMinimum) { this.regionMinimum = regionMinimum; }

    public int getSeriesId() { return seriesId; }
    public void setSeriesId(int seriesId) { this.seriesId = seriesId; }

    public int getTier0() { return tier0; }
    public void setTier0(int tier0) { this.tier0 = tier0; }

    public int getTier1() { return tier1; }
    public void setTier1(int tier1) { this.tier1 = tier1; }

    public int getTier2() { return tier2; }
    public void setTier2(int tier2) { this.tier2 = tier2; }

    public int getTier3() { return tier3; }
    public void setTier3(int tier3) { this.tier3 = tier3; }

    public int getTier4() { return tier4; }
    public void setTier4(int tier4) { this.tier4 = tier4; }
}
