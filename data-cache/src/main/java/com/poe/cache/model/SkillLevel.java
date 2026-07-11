package com.poe.cache.model;

/**
 * 技能等级数据实体，映射 skill_levels 表。
 * <p>
 * 记录每个技能等级的具体数值，包括消耗、冷却、伤害效能、属性需求等。
 */
public class SkillLevel {

    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 攻击速度倍率 */
    private int attackSpeedMultiplier;
    /** 攻击时间（秒） */
    private double attackTime;
    /** 冷却时间（秒） */
    private double cooldown;
    /** 消耗数值列表 */
    private String costAmounts;
    /** 消耗倍率 */
    private double costMultiplier;
    /** 消耗类型列表 */
    private String costTypes;
    /** 暴击率 */
    private double criticalStrikeChance;
    /** 伤害效能 */
    private double damageEffectiveness;
    /** 伤害倍率 */
    private double damageMultiplier;
    /** 敏捷需求 */
    private int dexterityRequirement;
    /** 持续时间（秒） */
    private double duration;
    /** 升级所需经验 */
    private int experience;
    /** 智力需求 */
    private int intelligenceRequirement;
    /** 技能等级 */
    private int level;
    /** 角色等级需求 */
    private int levelRequirement;
    /** 生命保留（固定值） */
    private int lifeReservationFlat;
    /** 生命保留（百分比） */
    private int lifeReservationPercent;
    /** 魔力保留（固定值） */
    private int manaReservationFlat;
    /** 魔力保留（百分比） */
    private int manaReservationPercent;
    /** 技能品质等级 */
    private int skillLevel;
    /** 属性文本 */
    private String statText;
    /** 最大储存次数 */
    private int storedUses;
    /** 力量需求 */
    private int strengthRequirement;
    /** 瓦尔灵魂获取阻止时间（秒） */
    private double vaalSoulGainPreventionTime;
    /** 瓦尔技能灵魂需求 */
    private int vaalSoulsRequirement;
    /** 瓦尔技能储存次数 */
    private int vaalStoredUses;

    public SkillLevel() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public int getAttackSpeedMultiplier() { return attackSpeedMultiplier; }
    public void setAttackSpeedMultiplier(int attackSpeedMultiplier) { this.attackSpeedMultiplier = attackSpeedMultiplier; }

    public double getAttackTime() { return attackTime; }
    public void setAttackTime(double attackTime) { this.attackTime = attackTime; }

    public double getCooldown() { return cooldown; }
    public void setCooldown(double cooldown) { this.cooldown = cooldown; }

    public String getCostAmounts() { return costAmounts; }
    public void setCostAmounts(String costAmounts) { this.costAmounts = costAmounts; }

    public double getCostMultiplier() { return costMultiplier; }
    public void setCostMultiplier(double costMultiplier) { this.costMultiplier = costMultiplier; }

    public String getCostTypes() { return costTypes; }
    public void setCostTypes(String costTypes) { this.costTypes = costTypes; }

    public double getCriticalStrikeChance() { return criticalStrikeChance; }
    public void setCriticalStrikeChance(double criticalStrikeChance) { this.criticalStrikeChance = criticalStrikeChance; }

    public double getDamageEffectiveness() { return damageEffectiveness; }
    public void setDamageEffectiveness(double damageEffectiveness) { this.damageEffectiveness = damageEffectiveness; }

    public double getDamageMultiplier() { return damageMultiplier; }
    public void setDamageMultiplier(double damageMultiplier) { this.damageMultiplier = damageMultiplier; }

    public int getDexterityRequirement() { return dexterityRequirement; }
    public void setDexterityRequirement(int dexterityRequirement) { this.dexterityRequirement = dexterityRequirement; }

    public double getDuration() { return duration; }
    public void setDuration(double duration) { this.duration = duration; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public int getIntelligenceRequirement() { return intelligenceRequirement; }
    public void setIntelligenceRequirement(int intelligenceRequirement) { this.intelligenceRequirement = intelligenceRequirement; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getLevelRequirement() { return levelRequirement; }
    public void setLevelRequirement(int levelRequirement) { this.levelRequirement = levelRequirement; }

    public int getLifeReservationFlat() { return lifeReservationFlat; }
    public void setLifeReservationFlat(int lifeReservationFlat) { this.lifeReservationFlat = lifeReservationFlat; }

    public int getLifeReservationPercent() { return lifeReservationPercent; }
    public void setLifeReservationPercent(int lifeReservationPercent) { this.lifeReservationPercent = lifeReservationPercent; }

    public int getManaReservationFlat() { return manaReservationFlat; }
    public void setManaReservationFlat(int manaReservationFlat) { this.manaReservationFlat = manaReservationFlat; }

    public int getManaReservationPercent() { return manaReservationPercent; }
    public void setManaReservationPercent(int manaReservationPercent) { this.manaReservationPercent = manaReservationPercent; }

    public int getSkillLevel() { return skillLevel; }
    public void setSkillLevel(int skillLevel) { this.skillLevel = skillLevel; }

    public String getStatText() { return statText; }
    public void setStatText(String statText) { this.statText = statText; }

    public int getStoredUses() { return storedUses; }
    public void setStoredUses(int storedUses) { this.storedUses = storedUses; }

    public int getStrengthRequirement() { return strengthRequirement; }
    public void setStrengthRequirement(int strengthRequirement) { this.strengthRequirement = strengthRequirement; }

    public double getVaalSoulGainPreventionTime() { return vaalSoulGainPreventionTime; }
    public void setVaalSoulGainPreventionTime(double vaalSoulGainPreventionTime) { this.vaalSoulGainPreventionTime = vaalSoulGainPreventionTime; }

    public int getVaalSoulsRequirement() { return vaalSoulsRequirement; }
    public void setVaalSoulsRequirement(int vaalSoulsRequirement) { this.vaalSoulsRequirement = vaalSoulsRequirement; }

    public int getVaalStoredUses() { return vaalStoredUses; }
    public void setVaalStoredUses(int vaalStoredUses) { this.vaalStoredUses = vaalStoredUses; }
}
