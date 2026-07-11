package com.poe.cache.model;

/**
 * 命运卡属性，映射 divination_cards 表。
 * <p>
 * 通过 _pageName 与 base_items 表关联。
 */
public class DivinationCard {
    private Integer pageId;
    private String pageName;
    private String cardArt;
    private String cardBackground;

    public DivinationCard() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getCardArt() { return cardArt; }
    public void setCardArt(String cardArt) { this.cardArt = cardArt; }

    public String getCardBackground() { return cardBackground; }
    public void setCardBackground(String cardBackground) { this.cardBackground = cardBackground; }
}
