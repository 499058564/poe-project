package com.poe.common.exception;

/**
 * 与上游数据源（PoE Wiki、poedb、poe.ninja）同步失败时抛出。
 */
public class DataSyncException extends PoeException {

    /**
     * @param message 同步失败描述
     */
    public DataSyncException(String message) {
        super(message);
    }

    /**
     * @param message 同步失败描述
     * @param cause   底层异常
     */
    public DataSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
