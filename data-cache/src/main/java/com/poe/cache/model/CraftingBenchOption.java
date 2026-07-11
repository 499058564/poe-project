package com.poe.cache.model;

/**
 * 工艺台选项，映射 crafting_bench_options 表。
 * <p>
 * 记录藏身处工艺台可制作的词缀选项，包含等级要求、解锁条件、应用物品类型等信息。
 * 主键使用 Cargo 自带的 id 字段。
 */
public class CraftingBenchOption {
    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 工艺选项的唯一 ID */
    private Integer optionId;
    /** 词缀类型：prefix / suffix */
    private String affixType;
    /** 工艺解锁类别 */
    private String unlockCategory;
    /** 工艺解锁类别描述 */
    private String unlockCategoryDescription;
    /** 工艺效果描述 */
    private String description;
    /** 适用的物品主类别列表（逗号分隔） */
    private String itemClassCategories;
    /** 适用的具体物品类别列表（逗号分隔） */
    private String itemClasses;
    /** 适用的物品类别 ID 列表（逗号分隔） */
    private String itemClassesIds;
    /** 物品链接数要求 */
    private int links;
    /** 词缀组名 */
    private String modGroup;
    /** 关联的词缀 ID */
    private String modId;
    /** 工艺名称 */
    private String name;
    /** 提供该工艺的 NPC */
    private String npc;
    /** 排序序号 */
    private int ordinal;
    /** 工艺等级/阶数 */
    private int rank;
    /** 解锁配方所在位置 */
    private String recipeUnlockLocation;
    /** 所需角色等级 */
    private int requiredLevel;
    /** 要求的插槽颜色 */
    private String socketColours;
    /** 要求的插槽数量 */
    private int sockets;
    /** 需要解锁的隐匿词缀数量 */
    private int unveilsRequired;

    public CraftingBenchOption() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public Integer getOptionId() { return optionId; }
    public void setOptionId(Integer optionId) { this.optionId = optionId; }

    public String getAffixType() { return affixType; }
    public void setAffixType(String affixType) { this.affixType = affixType; }

    public String getUnlockCategory() { return unlockCategory; }
    public void setUnlockCategory(String unlockCategory) { this.unlockCategory = unlockCategory; }

    public String getUnlockCategoryDescription() { return unlockCategoryDescription; }
    public void setUnlockCategoryDescription(String unlockCategoryDescription) { this.unlockCategoryDescription = unlockCategoryDescription; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getItemClassCategories() { return itemClassCategories; }
    public void setItemClassCategories(String itemClassCategories) { this.itemClassCategories = itemClassCategories; }

    public String getItemClasses() { return itemClasses; }
    public void setItemClasses(String itemClasses) { this.itemClasses = itemClasses; }

    public String getItemClassesIds() { return itemClassesIds; }
    public void setItemClassesIds(String itemClassesIds) { this.itemClassesIds = itemClassesIds; }

    public int getLinks() { return links; }
    public void setLinks(int links) { this.links = links; }

    public String getModGroup() { return modGroup; }
    public void setModGroup(String modGroup) { this.modGroup = modGroup; }

    public String getModId() { return modId; }
    public void setModId(String modId) { this.modId = modId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNpc() { return npc; }
    public void setNpc(String npc) { this.npc = npc; }

    public int getOrdinal() { return ordinal; }
    public void setOrdinal(int ordinal) { this.ordinal = ordinal; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public String getRecipeUnlockLocation() { return recipeUnlockLocation; }
    public void setRecipeUnlockLocation(String recipeUnlockLocation) { this.recipeUnlockLocation = recipeUnlockLocation; }

    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }

    public String getSocketColours() { return socketColours; }
    public void setSocketColours(String socketColours) { this.socketColours = socketColours; }

    public int getSockets() { return sockets; }
    public void setSockets(int sockets) { this.sockets = sockets; }

    public int getUnveilsRequired() { return unveilsRequired; }
    public void setUnveilsRequired(int unveilsRequired) { this.unveilsRequired = unveilsRequired; }
}
