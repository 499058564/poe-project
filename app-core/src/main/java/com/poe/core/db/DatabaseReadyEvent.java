package com.poe.core.db;

/**
 * 数据库就绪事件。当 DatabaseInitializer 完成初始化（无论成功/失败）后发布。
 */
public class DatabaseReadyEvent {

    private final boolean success;
    private final String message;

    public DatabaseReadyEvent(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
