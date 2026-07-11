package com.poe.cache.model;

/**
 * 技能宝石实体，映射 skills 表。
 * <p>
 * 存储主动技能和辅助技能的基本信息，包括施法时间、描述、属性文本等。
 */
public class Skill {

    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 技能名称 */
    private String activeSkillName;
    /** 施法时间（秒） */
    private double castTime;
    /** 技能描述文本 */
    private String description;
    /** 是否为辅助技能 */
    private boolean isSupport;
    /** 物品类别 ID 限制 */
    private String itemClassIdRestriction;
    /** 物品类别限制 */
    private String itemClassRestriction;
    /** 技能最大等级 */
    private int maxLevel;
    /** 技能内部 ID */
    private String skillId;
    /** 属性说明文本 */
    private String statText;

    public Skill() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getActiveSkillName() { return activeSkillName; }
    public void setActiveSkillName(String activeSkillName) { this.activeSkillName = activeSkillName; }

    public double getCastTime() { return castTime; }
    public void setCastTime(double castTime) { this.castTime = castTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isSupport() { return isSupport; }
    public void setSupport(boolean support) { isSupport = support; }

    public String getItemClassIdRestriction() { return itemClassIdRestriction; }
    public void setItemClassIdRestriction(String itemClassIdRestriction) { this.itemClassIdRestriction = itemClassIdRestriction; }

    public String getItemClassRestriction() { return itemClassRestriction; }
    public void setItemClassRestriction(String itemClassRestriction) { this.itemClassRestriction = itemClassRestriction; }

    public int getMaxLevel() { return maxLevel; }
    public void setMaxLevel(int maxLevel) { this.maxLevel = maxLevel; }

    public String getSkillId() { return skillId; }
    public void setSkillId(String skillId) { this.skillId = skillId; }

    public String getStatText() { return statText; }
    public void setStatText(String statText) { this.statText = statText; }
}
