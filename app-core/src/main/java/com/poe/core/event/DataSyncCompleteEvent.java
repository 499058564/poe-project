package com.poe.core.event;

/**
 * 数据同步完成事件。
 *
 * <p>在某个表同步全部完成后发布，携带实际同步记录数和耗时。
 */
public class DataSyncCompleteEvent {

    /** 同步的表名 */
    private final String tableName;
    /** 实际同步记录数 */
    private final int syncedRecords;
    /** 同步耗时（毫秒） */
    private final long durationMs;

    /**
     * @param tableName 同步的表名
     * @param syncedRecords 实际同步记录数
     * @param durationMs 耗时（毫秒）
     */
    public DataSyncCompleteEvent(String tableName, int syncedRecords, long durationMs) {
        this.tableName = tableName;
        this.syncedRecords = syncedRecords;
        this.durationMs = durationMs;
    }

    public String getTableName() { return tableName; }
    public int getSyncedRecords() { return syncedRecords; }
    public long getDurationMs() { return durationMs; }

    @Override
    public String toString() {
        return "DataSyncCompleteEvent{table='" + tableName + "', synced=" + syncedRecords
            + ", duration=" + durationMs + "ms}";
    }
}
