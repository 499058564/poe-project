package com.poe.cache.model;

/**
 * 怪物实体，映射 monsters 表。
 * <p>
 * 存储怪物基本属性、伤害倍率、标签与技能等信息。
 */
public class Monster {

    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 攻击速度 */
    private int attackSpeed;
    /** 暴击率 */
    private int criticalStrikeChance;
    /** 伤害倍率 */
    private double damageMultiplier;
    /** 终局模式词缀 ID 列表 */
    private String endgameModIds;
    /** 经验倍率 */
    private double experienceMultiplier;
    /** 生命倍率 */
    private double healthMultiplier;
    /** 是否为 Boss */
    private boolean isBoss;
    /** 最大攻击距离 */
    private int maximumAttackDistance;
    /** 元数据 ID */
    private String metadataId;
    /** 最小攻击距离 */
    private int minimumAttackDistance;
    /** 词缀 ID 列表 */
    private String modIds;
    /** 模型尺寸倍率 */
    private double modelSizeMultiplier;
    /** 怪物类型 ID */
    private String monsterTypeId;
    /** 怪物名称 */
    private String name;
    /** 第一部词缀 ID 列表 */
    private String part1ModIds;
    /** 第二部词缀 ID 列表 */
    private String part2ModIds;
    /** 稀有度 */
    private String rarity;
    /** 稀有度 ID */
    private String rarityId;
    /** 体型 */
    private int size;
    /** 技能 ID 列表 */
    private String skillIds;
    /** 标签列表 */
    private String tags;

    public Monster() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public int getAttackSpeed() { return attackSpeed; }
    public void setAttackSpeed(int attackSpeed) { this.attackSpeed = attackSpeed; }

    public int getCriticalStrikeChance() { return criticalStrikeChance; }
    public void setCriticalStrikeChance(int criticalStrikeChance) { this.criticalStrikeChance = criticalStrikeChance; }

    public double getDamageMultiplier() { return damageMultiplier; }
    public void setDamageMultiplier(double damageMultiplier) { this.damageMultiplier = damageMultiplier; }

    public String getEndgameModIds() { return endgameModIds; }
    public void setEndgameModIds(String endgameModIds) { this.endgameModIds = endgameModIds; }

    public double getExperienceMultiplier() { return experienceMultiplier; }
    public void setExperienceMultiplier(double experienceMultiplier) { this.experienceMultiplier = experienceMultiplier; }

    public double getHealthMultiplier() { return healthMultiplier; }
    public void setHealthMultiplier(double healthMultiplier) { this.healthMultiplier = healthMultiplier; }

    public boolean isBoss() { return isBoss; }
    public void setBoss(boolean boss) { isBoss = boss; }

    public int getMaximumAttackDistance() { return maximumAttackDistance; }
    public void setMaximumAttackDistance(int maximumAttackDistance) { this.maximumAttackDistance = maximumAttackDistance; }

    public String getMetadataId() { return metadataId; }
    public void setMetadataId(String metadataId) { this.metadataId = metadataId; }

    public int getMinimumAttackDistance() { return minimumAttackDistance; }
    public void setMinimumAttackDistance(int minimumAttackDistance) { this.minimumAttackDistance = minimumAttackDistance; }

    public String getModIds() { return modIds; }
    public void setModIds(String modIds) { this.modIds = modIds; }

    public double getModelSizeMultiplier() { return modelSizeMultiplier; }
    public void setModelSizeMultiplier(double modelSizeMultiplier) { this.modelSizeMultiplier = modelSizeMultiplier; }

    public String getMonsterTypeId() { return monsterTypeId; }
    public void setMonsterTypeId(String monsterTypeId) { this.monsterTypeId = monsterTypeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPart1ModIds() { return part1ModIds; }
    public void setPart1ModIds(String part1ModIds) { this.part1ModIds = part1ModIds; }

    public String getPart2ModIds() { return part2ModIds; }
    public void setPart2ModIds(String part2ModIds) { this.part2ModIds = part2ModIds; }

    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }

    public String getRarityId() { return rarityId; }
    public void setRarityId(String rarityId) { this.rarityId = rarityId; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public String getSkillIds() { return skillIds; }
    public void setSkillIds(String skillIds) { this.skillIds = skillIds; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
}
