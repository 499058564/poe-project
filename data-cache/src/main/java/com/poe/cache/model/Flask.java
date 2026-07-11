package com.poe.cache.model;

/**
 * 药剂特有属性，映射 flasks 表。
 * <p>
 * 通过 _pageName 与 base_items 表关联。
 */
public class Flask {
    private Integer pageId;
    private String pageName;
    private int chargesMax;
    private int chargesPerUse;
    private double duration;
    private int life;
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
