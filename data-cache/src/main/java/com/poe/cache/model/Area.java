package com.poe.cache.model;

/**
 * 区域实体，映射 areas 表。
 * <p>
 * 存储 PoE 中的区域信息（城镇/地图/试炼/瓦尔区域等），
 * 包含等级、标签、怪物 ID、连接关系等。
 */
public class Area {

    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 所属章节 */
    private int act;
    /** 区域等级 */
    private int areaLevel;
    /** 区域类型标签 */
    private String areaTypeTags;
    /** Boss 怪物 ID 列表 */
    private String bossMonsterIds;
    /** 连接区域 ID 列表 */
    private String connectionIds;
    /** 入口 NPC */
    private String entryNpc;
    /** 入口文字 */
    private String entryText;
    /** 风味文字 */
    private String flavourText;
    /** 是否有传送点 */
    private int hasWaypoint;
    /** 区域 ID */
    private String id;
    /** 信息框 HTML */
    private String infoboxHtml;
    /** 是否为藏身处区域 */
    private int isHideoutArea;
    /** 是否为迷宫气锁区域 */
    private int isLabyrinthAirlockArea;
    /** 是否为迷宫区域 */
    private int isLabyrinthArea;
    /** 是否为迷宫 Boss 区域 */
    private int isLabyrinthBossArea;
    /** 是否为传承地图区域 */
    private int isLegacyMapArea;
    /** 是否为地图区域 */
    private int isMapArea;
    /** 是否为城镇区域 */
    private int isTownArea;
    /** 是否为传奇地图区域 */
    private int isUniqueMapArea;
    /** 是否为瓦尔区域 */
    private int isVaalArea;
    /** 最高等级限制 */
    private int levelRestrictionMax;
    /** 加载画面 */
    private String loadingScreen;
    /** 主页面 */
    private String mainPage;
    /** 主页面分类 */
    private String mainpageCategories;
    /** 词缀 ID 列表 */
    private String modifierIds;
    /** 怪物 ID 列表 */
    private String monsterIds;
    /** 区域名称 */
    private String name;
    /** 父区域 ID */
    private String parentAreaId;
    /** 引入版本 */
    private String releaseVersion;
    /** 移除版本 */
    private String removalVersion;
    /** 截图 */
    private String screenshot;
    /** 属性文字 */
    private String statText;
    /** 保险箱最大数量 */
    private int strongboxMaxCount;
    /** 保险箱生成几率 */
    private int strongboxSpawnChance;
    /** 保险箱魔法权重 */
    private int strongboxWeightMagic;
    /** 保险箱普通权重 */
    private int strongboxWeightNormal;
    /** 保险箱稀有权重 */
    private int strongboxWeightRare;
    /** 保险箱传奇权重 */
    private int strongboxWeightUnique;
    /** 标签列表 */
    private String tags;
    /** 瓦尔区域 ID 列表 */
    private String vaalAreaIds;
    /** 瓦尔区域生成几率 */
    private int vaalAreaSpawnChance;

    public Area() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public int getAct() { return act; }
    public void setAct(int act) { this.act = act; }

    public int getAreaLevel() { return areaLevel; }
    public void setAreaLevel(int areaLevel) { this.areaLevel = areaLevel; }

    public String getAreaTypeTags() { return areaTypeTags; }
    public void setAreaTypeTags(String areaTypeTags) { this.areaTypeTags = areaTypeTags; }

    public String getBossMonsterIds() { return bossMonsterIds; }
    public void setBossMonsterIds(String bossMonsterIds) { this.bossMonsterIds = bossMonsterIds; }

    public String getConnectionIds() { return connectionIds; }
    public void setConnectionIds(String connectionIds) { this.connectionIds = connectionIds; }

    public String getEntryNpc() { return entryNpc; }
    public void setEntryNpc(String entryNpc) { this.entryNpc = entryNpc; }

    public String getEntryText() { return entryText; }
    public void setEntryText(String entryText) { this.entryText = entryText; }

    public String getFlavourText() { return flavourText; }
    public void setFlavourText(String flavourText) { this.flavourText = flavourText; }

    public int getHasWaypoint() { return hasWaypoint; }
    public void setHasWaypoint(int hasWaypoint) { this.hasWaypoint = hasWaypoint; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getInfoboxHtml() { return infoboxHtml; }
    public void setInfoboxHtml(String infoboxHtml) { this.infoboxHtml = infoboxHtml; }

    public int getIsHideoutArea() { return isHideoutArea; }
    public void setIsHideoutArea(int isHideoutArea) { this.isHideoutArea = isHideoutArea; }

    public int getIsLabyrinthAirlockArea() { return isLabyrinthAirlockArea; }
    public void setIsLabyrinthAirlockArea(int isLabyrinthAirlockArea) { this.isLabyrinthAirlockArea = isLabyrinthAirlockArea; }

    public int getIsLabyrinthArea() { return isLabyrinthArea; }
    public void setIsLabyrinthArea(int isLabyrinthArea) { this.isLabyrinthArea = isLabyrinthArea; }

    public int getIsLabyrinthBossArea() { return isLabyrinthBossArea; }
    public void setIsLabyrinthBossArea(int isLabyrinthBossArea) { this.isLabyrinthBossArea = isLabyrinthBossArea; }

    public int getIsLegacyMapArea() { return isLegacyMapArea; }
    public void setIsLegacyMapArea(int isLegacyMapArea) { this.isLegacyMapArea = isLegacyMapArea; }

    public int getIsMapArea() { return isMapArea; }
    public void setIsMapArea(int isMapArea) { this.isMapArea = isMapArea; }

    public int getIsTownArea() { return isTownArea; }
    public void setIsTownArea(int isTownArea) { this.isTownArea = isTownArea; }

    public int getIsUniqueMapArea() { return isUniqueMapArea; }
    public void setIsUniqueMapArea(int isUniqueMapArea) { this.isUniqueMapArea = isUniqueMapArea; }

    public int getIsVaalArea() { return isVaalArea; }
    public void setIsVaalArea(int isVaalArea) { this.isVaalArea = isVaalArea; }

    public int getLevelRestrictionMax() { return levelRestrictionMax; }
    public void setLevelRestrictionMax(int levelRestrictionMax) { this.levelRestrictionMax = levelRestrictionMax; }

    public String getLoadingScreen() { return loadingScreen; }
    public void setLoadingScreen(String loadingScreen) { this.loadingScreen = loadingScreen; }

    public String getMainPage() { return mainPage; }
    public void setMainPage(String mainPage) { this.mainPage = mainPage; }

    public String getMainpageCategories() { return mainpageCategories; }
    public void setMainpageCategories(String mainpageCategories) { this.mainpageCategories = mainpageCategories; }

    public String getModifierIds() { return modifierIds; }
    public void setModifierIds(String modifierIds) { this.modifierIds = modifierIds; }

    public String getMonsterIds() { return monsterIds; }
    public void setMonsterIds(String monsterIds) { this.monsterIds = monsterIds; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getParentAreaId() { return parentAreaId; }
    public void setParentAreaId(String parentAreaId) { this.parentAreaId = parentAreaId; }

    public String getReleaseVersion() { return releaseVersion; }
    public void setReleaseVersion(String releaseVersion) { this.releaseVersion = releaseVersion; }

    public String getRemovalVersion() { return removalVersion; }
    public void setRemovalVersion(String removalVersion) { this.removalVersion = removalVersion; }

    public String getScreenshot() { return screenshot; }
    public void setScreenshot(String screenshot) { this.screenshot = screenshot; }

    public String getStatText() { return statText; }
    public void setStatText(String statText) { this.statText = statText; }

    public int getStrongboxMaxCount() { return strongboxMaxCount; }
    public void setStrongboxMaxCount(int strongboxMaxCount) { this.strongboxMaxCount = strongboxMaxCount; }

    public int getStrongboxSpawnChance() { return strongboxSpawnChance; }
    public void setStrongboxSpawnChance(int strongboxSpawnChance) { this.strongboxSpawnChance = strongboxSpawnChance; }

    public int getStrongboxWeightMagic() { return strongboxWeightMagic; }
    public void setStrongboxWeightMagic(int strongboxWeightMagic) { this.strongboxWeightMagic = strongboxWeightMagic; }

    public int getStrongboxWeightNormal() { return strongboxWeightNormal; }
    public void setStrongboxWeightNormal(int strongboxWeightNormal) { this.strongboxWeightNormal = strongboxWeightNormal; }

    public int getStrongboxWeightRare() { return strongboxWeightRare; }
    public void setStrongboxWeightRare(int strongboxWeightRare) { this.strongboxWeightRare = strongboxWeightRare; }

    public int getStrongboxWeightUnique() { return strongboxWeightUnique; }
    public void setStrongboxWeightUnique(int strongboxWeightUnique) { this.strongboxWeightUnique = strongboxWeightUnique; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getVaalAreaIds() { return vaalAreaIds; }
    public void setVaalAreaIds(String vaalAreaIds) { this.vaalAreaIds = vaalAreaIds; }

    public int getVaalAreaSpawnChance() { return vaalAreaSpawnChance; }
    public void setVaalAreaSpawnChance(int vaalAreaSpawnChance) { this.vaalAreaSpawnChance = vaalAreaSpawnChance; }
}
