package com.poe.cache.model;

/**
 * 地图系列属性，映射 map_series 表。
 * <p>
 * 通过 _pageName 与 base_items 表关联。
 */
public class MapSeries {
    private Integer pageId;
    private String pageName;
    private String seriesId;
    private String name;
    private int ordinal;

    public MapSeries() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getSeriesId() { return seriesId; }
    public void setSeriesId(String seriesId) { this.seriesId = seriesId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getOrdinal() { return ordinal; }
    public void setOrdinal(int ordinal) { this.ordinal = ordinal; }
}
