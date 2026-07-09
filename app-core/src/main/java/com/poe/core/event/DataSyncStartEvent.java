package com.poe.core.event;

/**
 * 数据同步开始事件。
 *
 * <p>由 data-provider 在开始同步某个数据表时发布，
 * 携带表名和预期总记录数，供 UI 初始化进度条。
 */
public class DataSyncStartEvent {

    /** 同步的表名（如 "base_items"） */
    private final String tableName;
    /** 预期同步的总记录数 */
    private final int totalRecords;

    /**
     * @param tableName 同步的表名
     * @param totalRecords 预期同步的记录总数
     */
    public DataSyncStartEvent(String tableName, int totalRecords) {
        this.tableName = tableName;
        this.totalRecords = totalRecords;
    }

    public String getTableName() { return tableName; }
    public int getTotalRecords() { return totalRecords; }

    @Override
    public String toString() {
        return "DataSyncStartEvent{table='" + tableName + "', total=" + totalRecords + "}";
    }
}
