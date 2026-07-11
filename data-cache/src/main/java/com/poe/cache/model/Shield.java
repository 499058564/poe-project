package com.poe.cache.model;

/**
 * 盾牌特有属性，映射 shields 表。
 * <p>
 * 通过 _pageName 与 base_items 表关联。
 */
public class Shield {
    private Integer pageId;
    private String pageName;
    private int block;

    public Shield() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public int getBlock() { return block; }
    public void setBlock(int block) { this.block = block; }
}
