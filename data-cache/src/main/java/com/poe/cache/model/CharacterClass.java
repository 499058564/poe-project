package com.poe.cache.model;

/**
 * 角色职业实体，映射 character_classes 表。
 * <p>
 * 记录 PoE 七大基础职业的属性倾向和风味文本。
 */
public class CharacterClass {

    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 敏捷属性描述 */
    private String dexterity;
    /** 职业风味文本 */
    private String flavourText;
    /** 职业内部 ID（Cargo 字段 id） */
    private int classId;
    /** 智力属性描述 */
    private String intelligence;
    /** 职业名称 */
    private String name;
    /** 职业字符串 ID */
    private String strId;
    /** 力量属性描述 */
    private String strength;

    public CharacterClass() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getDexterity() { return dexterity; }
    public void setDexterity(String dexterity) { this.dexterity = dexterity; }

    public String getFlavourText() { return flavourText; }
    public void setFlavourText(String flavourText) { this.flavourText = flavourText; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public String getIntelligence() { return intelligence; }
    public void setIntelligence(String intelligence) { this.intelligence = intelligence; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStrId() { return strId; }
    public void setStrId(String strId) { this.strId = strId; }

    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }
}
