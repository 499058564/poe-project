package com.poe.cache.model;

/**
 * 药剂特有属性，映射 flasks 表。
 * <p>
 * 通过 _pageName 与 base_items 表关联。
 */
public class Flask {
    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName），关联 base_items */
    private String pageName;
    /** 最大充能数 */
    private int chargesMax;
    /** 每次使用消耗充能数 */
    private int chargesPerUse;
    /** 药剂持续时间（秒） */
    private double duration;
    /** 生命恢复量（生命药剂） */
    private int life;
    /** 魔力恢复量（魔力药剂） */
    private int mana;

    public Flask() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public int getChargesMax() { return chargesMax; }
    public void setChargesMax(int chargesMax) { this.chargesMax = chargesMax; }

    public int getChargesPerUse() { return chargesPerUse; }
    public void setChargesPerUse(int chargesPerUse) { this.chargesPerUse = chargesPerUse; }

    public double getDuration() { return duration; }
    public void setDuration(double duration) { this.duration = duration; }

    public int getLife() { return life; }
    public void setLife(int life) { this.life = life; }

    public int getMana() { return mana; }
    public void setMana(int mana) { this.mana = mana; }
}
