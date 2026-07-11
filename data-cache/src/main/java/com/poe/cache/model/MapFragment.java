package com.poe.cache.model;

/**
 * 地图碎片属性，映射 map_fragments 表。
 * <p>
 * 通过 _pageName 与 base_items 表关联。
 */
public class MapFragment {
    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName），关联 base_items */
    private String pageName;
    /** 同种碎片一叠最大数量限制 */
    private int mapFragmentLimit;

    public MapFragment() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public int getMapFragmentLimit() { return mapFragmentLimit; }
    public void setMapFragmentLimit(int mapFragmentLimit) { this.mapFragmentLimit = mapFragmentLimit; }
}
