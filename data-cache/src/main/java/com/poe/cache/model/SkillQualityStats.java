package com.poe.cache.model;

/**
 * 技能品质属性数值实体，映射 skill_quality_stats 表。
 * <p>
 * 记录技能品质方案中每个属性 ID 对应的数值。
 */
public class SkillQualityStats {

    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 属性 ID（Cargo 字段 id） */
    private String statId;
    /** 品质方案 ID */
    private int setId;
    /** 属性数值 */
    private int value;

    public SkillQualityStats() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getStatId() { return statId; }
    public void setStatId(String statId) { this.statId = statId; }

    public int getSetId() { return setId; }
    public void setSetId(int setId) { this.setId = setId; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
}
