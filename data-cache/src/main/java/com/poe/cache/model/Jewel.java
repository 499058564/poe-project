package com.poe.cache.model;

/**
 * 珠宝特有属性，映射 jewels 表。
 * <p>
 * 通过 _pageName 与 base_items 表关联。
 */
public class Jewel {
    private Integer pageId;
    private String pageName;
    private String jewelLimit;
    private String radiusHtml;

    public Jewel() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getJewelLimit() { return jewelLimit; }
    public void setJewelLimit(String jewelLimit) { this.jewelLimit = jewelLimit; }

    public String getRadiusHtml() { return radiusHtml; }
    public void setRadiusHtml(String radiusHtml) { this.radiusHtml = radiusHtml; }
}
