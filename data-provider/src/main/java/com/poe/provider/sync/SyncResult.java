package com.poe.provider.sync;

import java.time.Instant;

/**
 * 数据同步结果，记录单次同步操作的统计信息。
 * <p>
 * 包含同步开始/结束时间、记录数以及是否跳过（如远程数据无变化）。
 */
public class SyncResult {

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

    private SyncResult(int syncedRecords, long durationMs,
                       Instant startedAt, Instant finishedAt, boolean skipped) {
        this.syncedRecords = syncedRecords;
        this.durationMs = durationMs;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.skipped = skipped;
    }

    // ---- factory methods ----

    /** 创建"已跳过"结果（远程无变化）。 */
    public static SyncResult skipped() {
        Instant now = Instant.now();
        return new SyncResult(0, 0, now, now, true);
    }

    /** 创建正常同步结果。 */
    public static SyncResult of(int syncedRecords, Instant startedAt, Instant finishedAt) {
        long durationMs = startedAt != null && finishedAt != null
            ? java.time.Duration.between(startedAt, finishedAt).toMillis()
            : 0;
        return new SyncResult(syncedRecords, durationMs, startedAt, finishedAt, false);
    }

    // ---- accessors ----

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

    @Override
    public String toString() {
        if (skipped) return "SyncResult{skipped}";
        return "SyncResult{records=" + syncedRecords + ", duration=" + durationMs + "ms}";
    }
}
