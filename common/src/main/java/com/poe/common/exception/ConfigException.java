package com.poe.common.exception;

/**
 * 应用配置无效或缺失时抛出，例如 {@code config.json} 中缺少必要的路径或设置。
 */
public class ConfigException extends PoeException {

    /**
     * @param message 配置问题描述
     */
    public ConfigException(String message) {
        super(message);
    }

    /**
     * @param message 配置问题描述
     * @param cause   底层异常
     */
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
