package com.poe.cache.model;

/**
 * 专精效果实体，映射 mastery_effects 表。
 * <p>
 * 记录天赋专精的各项可选效果，包括属性 ID、数值和文本描述。
 */
public class MasteryEffect {

    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 专精效果 ID（Cargo 字段 id） */
    private String effectId;
    /** 属性 ID 列表（逗号分隔） */
    private String statIds;
    /** 属性说明文本 */
    private String statText;
    /** 属性原始文本 */
    private String statTextRaw;
    /** 属性数值列表（逗号分隔） */
    private String statValues;

    public MasteryEffect() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getEffectId() { return effectId; }
    public void setEffectId(String effectId) { this.effectId = effectId; }

    public String getStatIds() { return statIds; }
    public void setStatIds(String statIds) { this.statIds = statIds; }

    public String getStatText() { return statText; }
    public void setStatText(String statText) { this.statText = statText; }

    public String getStatTextRaw() { return statTextRaw; }
    public void setStatTextRaw(String statTextRaw) { this.statTextRaw = statTextRaw; }

    public String getStatValues() { return statValues; }
    public void setStatValues(String statValues) { this.statValues = statValues; }
}
