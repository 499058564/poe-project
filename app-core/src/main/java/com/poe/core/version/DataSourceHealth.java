package com.poe.core.version;

/**
 * 数据源健康检查结果。
 */
public class DataSourceHealth {

    public enum Status {
        HEALTHY, DEGRADED, UNAVAILABLE, UNKNOWN
    }

    private final String sourceName;
    private final Status status;
    private final long responseTimeMs;
    private final String message;

    public DataSourceHealth(String sourceName, Status status, long responseTimeMs, String message) {
        this.sourceName = sourceName;
        this.status = status;
        this.responseTimeMs = responseTimeMs;
        this.message = message;
    }

    public String getSourceName() { return sourceName; }
    public Status getStatus() { return status; }
    public long getResponseTimeMs() { return responseTimeMs; }
    public String getMessage() { return message; }

    public boolean isAvailable() {
        return status == Status.HEALTHY || status == Status.DEGRADED;
    }

    @Override
    public String toString() {
        return "DataSourceHealth{" + sourceName + ": " + status + " (" + responseTimeMs + "ms)}";
    }
}
