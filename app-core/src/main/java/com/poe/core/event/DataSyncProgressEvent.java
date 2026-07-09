package com.poe.core.event;

/**
 * 数据同步进度事件。
 *
 * <p>在同步过程中周期性发布，携带当前进度。
 * 推荐在批量写入（如每 500 条）时发布一次。
 */
public class DataSyncProgressEvent {

    /** 同步的表名 */
    private final String tableName;
    /** 已完成记录数 */
    private final int current;
    /** 总记录数 */
    private final int total;

    /**
     * @param tableName 同步的表名
     * @param current 当前已处理的记录数
     * @param total 总记录数
     */
    public DataSyncProgressEvent(String tableName, int current, int total) {
        this.tableName = tableName;
        this.current = current;
        this.total = total;
    }

    public String getTableName() { return tableName; }
    public int getCurrent() { return current; }
    public int getTotal() { return total; }

    /** 进度百分比（0.0 ~ 1.0） */
    public double getProgress() {
        return total > 0 ? (double) current / total : 0.0;
    }

    @Override
    public String toString() {
        return "DataSyncProgressEvent{table='" + tableName + "', " + current + "/" + total + "}";
    }
}
