package com.poe.core.event;

/**
 * 配置变更事件，当用户修改配置项时发布。
 *
 * <p>由 {@code AppConfig} 中配置 setter 触发，
 * 携带变更的 key、旧值和新值，供 UI 等模块响应。
 */
public class ConfigChangedEvent {

    /** 变更的配置键（如 "language"、"theme"） */
    private final String key;
    /** 变更前的值 */
    private final String oldValue;
    /** 变更后的值 */
    private final String newValue;

    /**
     * @param key 变更的配置键
     * @param oldValue 变更前的值
     * @param newValue 变更后的值
     */
    public ConfigChangedEvent(String key, String oldValue, String newValue) {
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getKey() { return key; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }

    @Override
    public String toString() {
        return "ConfigChangedEvent{key='" + key + "', old='" + oldValue + "', new='" + newValue + "'}";
    }
}
