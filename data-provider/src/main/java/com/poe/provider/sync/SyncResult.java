package com.poe.provider.sync;

import java.time.Instant;

/**
 * 数据同步结果，记录单次同步操作的统计信息。
 * <p>
 * 包含同步开始/结束时间、记录数、表名、错误信息，以及是否跳过或成功。
 */
public class SyncResult {

    /** 本次同步的表名（Cargo 表名） */
    private final String tableName;
    /** 本次同步的总记录数 */
    private final int syncedRecords;
    /** 同步持续时间（毫秒） */
    private final long durationMs;
    /** 同步开始时间 */
    private final Instant startedAt;
    /** 同步结束时间 */
    private final Instant finishedAt;
    /** 是否因为远程数据无变化而跳过 */
    private final boolean skipped;
    /** 同步是否成功（false 表示异常中断） */
    private final boolean success;
    /** 错误信息（成功时为 null） */
    private final String errorMessage;

    private SyncResult(String tableName, int syncedRecords, long durationMs,
                       Instant startedAt, Instant finishedAt,
                       boolean skipped, boolean success, String errorMessage) {
        this.tableName = tableName;
        this.syncedRecords = syncedRecords;
        this.durationMs = durationMs;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.skipped = skipped;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    // ---- factory methods ----

    /** 创建"已跳过"结果（远程无变化）。 */
    public static SyncResult skipped() {
        Instant now = Instant.now();
        return new SyncResult(null, 0, 0, now, now, true, true, null);
    }

    /** 创建正常同步结果。 */
    public static SyncResult of(int syncedRecords, Instant startedAt, Instant finishedAt) {
        long durationMs = startedAt != null && finishedAt != null
            ? java.time.Duration.between(startedAt, finishedAt).toMillis()
            : 0;
        return new SyncResult(null, syncedRecords, durationMs, startedAt, finishedAt,
            false, true, null);
    }

    /** 创建指定表的成功同步结果。 */
    public static SyncResult success(String tableName, int syncedRecords,
                                     Instant startedAt, Instant finishedAt) {
        long durationMs = startedAt != null && finishedAt != null
            ? java.time.Duration.between(startedAt, finishedAt).toMillis()
            : 0;
        return new SyncResult(tableName, syncedRecords, durationMs, startedAt, finishedAt,
            false, true, null);
    }

    /** 创建失败结果。 */
    public static SyncResult failed(String tableName, String errorMessage) {
        Instant now = Instant.now();
        return new SyncResult(tableName, 0, 0, now, now, false, false, errorMessage);
    }

    // ---- accessors ----

    /** 表名（Cargo 表名，可能为 null）。 */
    public String getTableName() { return tableName; }
    /** 同步的记录数。 */
    public int getSyncedRecords() { return syncedRecords; }
    /** 同步耗时（毫秒）。 */
    public long getDurationMs() { return durationMs; }
    /** 同步开始时间。 */
    public Instant getStartedAt() { return startedAt; }
    /** 同步结束时间。 */
    public Instant getFinishedAt() { return finishedAt; }
    /** 是否跳过（无变化）。 */
    public boolean isSkipped() { return skipped; }
    /** 是否成功（false 表示异常中断或部分失败）。 */
    public boolean isSuccess() { return success; }
    /** 错误信息（成功时为 null）。 */
    public String getErrorMessage() { return errorMessage; }

    @Override
    public String toString() {
        if (skipped) return "SyncResult{table='" + tableName + "', skipped}";
        if (!success) return "SyncResult{table='" + tableName + "', failed: " + errorMessage + "}";
        return "SyncResult{table='" + tableName + "', records=" + syncedRecords
            + ", duration=" + durationMs + "ms}";
    }
}
