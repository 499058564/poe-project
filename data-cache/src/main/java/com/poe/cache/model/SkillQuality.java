package com.poe.cache.model;

/**
 * 技能品质效果实体，映射 skill_quality 表。
 * <p>
 * 记录技能不同品质方案的属性文本和权重。
 */
public class SkillQuality {

    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 品质方案 ID */
    private int setId;
    /** 品质效果属性文本 */
    private String statText;
    /** 品质方案权重 */
    private int weight;

    public SkillQuality() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public int getSetId() { return setId; }
    public void setSetId(int setId) { this.setId = setId; }

    public String getStatText() { return statText; }
    public void setStatText(String statText) { this.statText = statText; }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
}
