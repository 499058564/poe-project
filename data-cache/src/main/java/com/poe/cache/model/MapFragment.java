package com.poe.cache.model;

/**
 * 地图碎片属性，映射 map_fragments 表。
 * <p>
 * 通过 _pageName 与 base_items 表关联。
 */
public class MapFragment {
    private Integer pageId;
    private String pageName;
    private int mapFragmentLimit;

    public MapFragment() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public int getMapFragmentLimit() { return mapFragmentLimit; }
    public void setMapFragmentLimit(int mapFragmentLimit) { this.mapFragmentLimit = mapFragmentLimit; }
}
