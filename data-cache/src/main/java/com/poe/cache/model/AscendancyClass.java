package com.poe.cache.model;

/**
 * 升华职业实体，映射 ascendancy_classes 表。
 * <p>
 * 记录七大基础职业各自的升华进阶分支，包括关联的基础职业和风味文本。
 */
public class AscendancyClass {

    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 所属基础职业名称 */
    private String characterClass;
    /** 所属基础职业 ID */
    private int characterId;
    /** 升华职业风味文本 */
    private String flavourText;
    /** 升华职业内部 ID（Cargo 字段 id） */
    private int ascendancyId;
    /** 升华职业名称 */
    private String name;

    public AscendancyClass() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getCharacterClass() { return characterClass; }
    public void setCharacterClass(String characterClass) { this.characterClass = characterClass; }

    public int getCharacterId() { return characterId; }
    public void setCharacterId(int characterId) { this.characterId = characterId; }

    public String getFlavourText() { return flavourText; }
    public void setFlavourText(String flavourText) { this.flavourText = flavourText; }

    public int getAscendancyId() { return ascendancyId; }
    public void setAscendancyId(int ascendancyId) { this.ascendancyId = ascendancyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
