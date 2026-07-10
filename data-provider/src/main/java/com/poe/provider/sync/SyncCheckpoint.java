package com.poe.provider.sync;

import java.time.Instant;

/**
 * 同步断点，用于支持断点续传。
 * <p>
 * 在长时间同步被中断时，保存当前进度，下次启动从断点偏移量继续。
 * 断点持久化到 data_version 表或外部文件。
 */
public class SyncCheckpoint {

    /** 正在同步的表名 */
    private final String tableName;
    /** 已完成写入的最后偏移量 */
    private final int lastOffset;
    /** 记录断点时的时间 */
    private final Instant timestamp;

    /**
     * @param tableName  表名
     * @param lastOffset 已完成写入的最后偏移量（下次从此处继续）
     * @param timestamp  记录时间
     */
    public SyncCheckpoint(String tableName, int lastOffset, Instant timestamp) {
        this.tableName = tableName;
        this.lastOffset = lastOffset;
        this.timestamp = timestamp;
    }

    public String getTableName() { return tableName; }
    public int getLastOffset() { return lastOffset; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "SyncCheckpoint{table='" + tableName + "', offset=" + lastOffset + "}";
    }
}
