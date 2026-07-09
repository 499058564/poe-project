package com.poe.common.exception;

/**
 * 所有 PoE 应用异常的基类。
 * 领域异常均应继承此类。
 */
public class PoeException extends RuntimeException {

    /**
     * 使用描述信息构造异常。
     *
     * @param message 错误描述
     */
    public PoeException(String message) {
        super(message);
    }

    /**
     * 使用描述信息和根因构造异常。
     *
     * @param message 错误描述
     * @param cause   底层异常
     */
    public PoeException(String message, Throwable cause) {
        super(message, cause);
    }
}
