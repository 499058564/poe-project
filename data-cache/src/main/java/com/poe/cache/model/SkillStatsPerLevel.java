package com.poe.cache.model;

/**
 * 技能每级属性数值实体，映射 skill_stats_per_level 表。
 * <p>
 * 记录每个技能在每个等级下各属性 ID 对应的数值。
 */
public class SkillStatsPerLevel {

    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 属性 ID（Cargo 字段 id） */
    private String statId;
    /** 技能等级 */
    private int level;
    /** 属性数值 */
    private int value;

    public SkillStatsPerLevel() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getStatId() { return statId; }
    public void setStatId(String statId) { this.statId = statId; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
}
