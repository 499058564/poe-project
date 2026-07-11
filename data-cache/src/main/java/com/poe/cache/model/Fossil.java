package com.poe.cache.model;

/**
 * 化石，映射 fossils 表。
 * <p>
 * 记录挖矿化石的属性，包括可添加/禁止的标签、强制词缀、品质/附魔/复制能力等。
 * 名称来自 _pageName。通过 _pageID 与 base_items 表关联。
 */
public class Fossil {
    /** Wiki 页面 ID，关联 base_items.id */
    private Integer pageId;
    /** Wiki 页面名称（_pageName），即化石名称 */
    private String pageName;
    /** 添加的词缀 ID 列表（逗号分隔） */
    private String addedModifierIds;
    /** 允许的标签列表（逗号分隔） */
    private String allowedTags;
    /** 基础物品 ID */
    private String baseItemId;
    /** 是否可附魔 */
    private boolean canEnchant;
    /** 是否可复制 */
    private boolean canMirror;
    /** 是否可加品质 */
    private boolean canQuality;
    /** 是否可将插槽洗白 */
    private boolean canRollWhiteSockets;
    /** 腐化精华几率 */
    private int corruptedEssenceChance;
    /** 禁止的标签列表（逗号分隔） */
    private String forbiddenTags;
    /** 强制添加的词缀 ID 列表（逗号分隔） */
    private String forcedModifierIds;
    /** 是否为幸运（重骰两次取更好结果） */
    private boolean lucky;
    /** 售价词缀 ID 列表（逗号分隔） */
    private String sellPriceModifierIds;

    public Fossil() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getAddedModifierIds() { return addedModifierIds; }
    public void setAddedModifierIds(String addedModifierIds) { this.addedModifierIds = addedModifierIds; }

    public String getAllowedTags() { return allowedTags; }
    public void setAllowedTags(String allowedTags) { this.allowedTags = allowedTags; }

    public String getBaseItemId() { return baseItemId; }
    public void setBaseItemId(String baseItemId) { this.baseItemId = baseItemId; }

    public boolean isCanEnchant() { return canEnchant; }
    public void setCanEnchant(boolean canEnchant) { this.canEnchant = canEnchant; }

    public boolean isCanMirror() { return canMirror; }
    public void setCanMirror(boolean canMirror) { this.canMirror = canMirror; }

    public boolean isCanQuality() { return canQuality; }
    public void setCanQuality(boolean canQuality) { this.canQuality = canQuality; }

    public boolean isCanRollWhiteSockets() { return canRollWhiteSockets; }
    public void setCanRollWhiteSockets(boolean canRollWhiteSockets) { this.canRollWhiteSockets = canRollWhiteSockets; }

    public int getCorruptedEssenceChance() { return corruptedEssenceChance; }
    public void setCorruptedEssenceChance(int corruptedEssenceChance) { this.corruptedEssenceChance = corruptedEssenceChance; }

    public String getForbiddenTags() { return forbiddenTags; }
    public void setForbiddenTags(String forbiddenTags) { this.forbiddenTags = forbiddenTags; }

    public String getForcedModifierIds() { return forcedModifierIds; }
    public void setForcedModifierIds(String forcedModifierIds) { this.forcedModifierIds = forcedModifierIds; }

    public boolean isLucky() { return lucky; }
    public void setLucky(boolean lucky) { this.lucky = lucky; }

    public String getSellPriceModifierIds() { return sellPriceModifierIds; }
    public void setSellPriceModifierIds(String sellPriceModifierIds) { this.sellPriceModifierIds = sellPriceModifierIds; }
}
