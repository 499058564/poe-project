package com.poe.core.model;

import java.util.List;

/**
 * 通用分页搜索结果容器。
 *
 * @param <T> 结果项类型
 */
public class SearchResult<T> {

    /** 当前页结果列表 */
    private final List<T> items;

    /** 匹配总数 */
    private final int total;

    /** 当前页码（从 1 开始） */
    private final int page;

    /** 每页数量 */
    private final int pageSize;

    /** 是否还有更多页 */
    private final boolean hasMore;

    public SearchResult(List<T> items, int total, int page, int pageSize) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.hasMore = page * pageSize < total;
    }

    public List<T> getItems() { return items; }

    public int getTotal() { return total; }

    public int getPage() { return page; }

    public int getPageSize() { return pageSize; }

    public boolean isHasMore() { return hasMore; }

    /** 计算总页数 */
    public int getTotalPages() {
        return total == 0 ? 0 : (total + pageSize - 1) / pageSize;
    }
}
