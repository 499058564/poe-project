package com.poe.cache.model;

/**
 * 珠宝特有属性，映射 jewels 表。
 * <p>
 * 通过 _pageName 与 base_items 表关联。
 */
public class Jewel {
    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName），关联 base_items */
    private String pageName;
    /** 珠宝数量限制描述文本 */
    private String jewelLimit;
    /** 珠宝作用范围（HTML 形式，如 "Large"） */
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
