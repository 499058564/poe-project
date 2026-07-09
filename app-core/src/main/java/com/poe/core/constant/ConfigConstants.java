package com.poe.core.constant;

/**
 * 配置相关常量，集中管理路径名和默认值。
 */
public final class ConfigConstants {

    private ConfigConstants() {
        // 工具类不允许实例化
    }

    // ── 路径常量 ──

    /** 配置根目录名 */
    public static final String CONFIG_DIR_NAME = ".poe-tool";
    /** 配置文件名 */
    public static final String CONFIG_FILE_NAME = "config.json";
    /** 数据子目录名 */
    public static final String DATA_DIR_NAME = "data";
    /** 日志子目录名 */
    public static final String LOGS_DIR_NAME = "logs";
    /** 损坏配置文件备份后缀 */
    public static final String CORRUPT_SUFFIX = ".corrupt.";

    // ── 默认值常量 ──

    /** 默认语言 */
    public static final String DEFAULT_LANGUAGE = "zh";
    /** 默认主题 */
    public static final String DEFAULT_THEME = "dark";
    /** 默认窗口宽度 */
    public static final int DEFAULT_WINDOW_WIDTH = 1280;
    /** 默认窗口高度 */
    public static final int DEFAULT_WINDOW_HEIGHT = 800;
    /** 默认是否自动同步 */
    public static final boolean DEFAULT_AUTO_SYNC = true;
}
