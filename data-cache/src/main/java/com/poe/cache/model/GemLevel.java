package com.poe.cache.model;

/**
 * 宝石等级需求实体，映射 gem_levels 表。
 * <p>
 * 记录宝石各等级的经验需求和属性要求（力量/敏捷/智力/角色等级）。
 */
public class GemLevel {

    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 升级所需经验 */
    private int experience;
    /** 宝石等级 */
    private int level;
    /** 需求敏捷 */
    private int requiredDexterity;
    /** 需求智力 */
    private int requiredIntelligence;
    /** 需求角色等级 */
    private int requiredLevel;
    /** 需求力量 */
    private int requiredStrength;

    public GemLevel() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getRequiredDexterity() { return requiredDexterity; }
    public void setRequiredDexterity(int requiredDexterity) { this.requiredDexterity = requiredDexterity; }

    public int getRequiredIntelligence() { return requiredIntelligence; }
    public void setRequiredIntelligence(int requiredIntelligence) { this.requiredIntelligence = requiredIntelligence; }

    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }

    public int getRequiredStrength() { return requiredStrength; }
    public void setRequiredStrength(int requiredStrength) { this.requiredStrength = requiredStrength; }
}
