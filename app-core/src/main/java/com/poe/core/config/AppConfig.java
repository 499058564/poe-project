package com.poe.core.config;

import com.poe.common.exception.ConfigException;
import com.poe.common.util.JsonUtils;
import com.poe.core.constant.ConfigConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 应用全局配置。
 * 负责 config.json 的加载、保存以及首次运行的目录初始化。
 *
 * <p>配置路径：{@code ~/.poe-tool/config.json}</p>
 */
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    /** 配置根目录 */
    private static final Path CONFIG_DIR = resolveBaseDir();

    /** 配置文件路径 */
    private static Path resolveBaseDir() {
        return Paths.get(System.getProperty("user.home"), ConfigConstants.CONFIG_DIR_NAME);
    }

    /** 配置文件路径（延迟计算，跟随 baseDir） */
    private static Path configFile() {
        return baseDir().resolve(ConfigConstants.CONFIG_FILE_NAME);
    }

    /** 用于测试的配置根目录覆盖 */
    private static Path testConfigDir = null;

    /** 设置测试用配置目录（测试前调用）。 */
    static void setTestConfigDir(Path dir) {
        testConfigDir = dir;
    }

    private static Path baseDir() {
        return testConfigDir != null ? testConfigDir : CONFIG_DIR;
    }

    // ── 配置项 ──

    /** 界面语言（"zh" / "en"），默认中文 */
    private String language = ConfigConstants.DEFAULT_LANGUAGE;
    /** 数据存储目录路径，默认为 {@code ~/.poe-tool/data/} */
    private String dataDirectory;
    /** Path of Building 安装路径或运行目录 */
    private String pobPath;
    /** 是否在启动时自动同步数据 */
    private boolean autoSync = ConfigConstants.DEFAULT_AUTO_SYNC;
    /** UI 主题名称（"dark" / "light"），默认暗色 */
    private String theme = ConfigConstants.DEFAULT_THEME;
    /** 主窗口宽度（像素） */
    private int windowWidth = ConfigConstants.DEFAULT_WINDOW_WIDTH;
    /** 主窗口高度（像素） */
    private int windowHeight = ConfigConstants.DEFAULT_WINDOW_HEIGHT;

    /** 单例（延迟初始化） */
    private static volatile AppConfig instance;

    private AppConfig() {
        this.dataDirectory = baseDir().resolve(ConfigConstants.DATA_DIR_NAME).toString();
    }

    // ── 公共访问 ──

    /** 获取配置单例（首次调用触发 load）。 */
    public static AppConfig getInstance() {
        if (instance == null) {
            synchronized (AppConfig.class) {
                if (instance == null) {
                    instance = load();
                }
            }
        }
        return instance;
    }

    /** 重新从磁盘加载配置（丢弃当前实例）。 */
    public static AppConfig reload() {
        synchronized (AppConfig.class) {
            instance = load();
            return instance;
        }
    }

    // ── 加载 / 保存 ──

    /**
     * 从磁盘加载配置。
     * <ul>
     *   <li>目录不存在 → 自动创建并写入默认配置</li>
     *   <li>文件存在且有效 → 反序列化</li>
     *   <li>文件损坏 → 备份损坏文件，写入默认配置</li>
     * </ul>
     */
    static AppConfig load() {
        Path dir = baseDir();
        try {
            Files.createDirectories(dir);
            Files.createDirectories(dir.resolve(ConfigConstants.DATA_DIR_NAME));
            Files.createDirectories(dir.resolve(ConfigConstants.LOGS_DIR_NAME));
        } catch (IOException e) {
            log.error("Failed to create config directories under {}", dir, e);
            return new AppConfig(); // 降级：返回内存默认配置
        }

        Path file = configFile();
        if (Files.exists(file)) {
            try {
                String json = Files.readString(file);
                AppConfig config = JsonUtils.fromJson(json, AppConfig.class);
                log.info("Config loaded from {}", file);
                return config;
            } catch (Exception e) {
                log.warn("Config file is corrupt, backing up and resetting to defaults", e);
                backupCorruptFile();
            }
        }

        // 首次运行或恢复：写入默认配置
        AppConfig defaults = new AppConfig();
        defaults.save();
        return defaults;
    }

    /** 保存当前配置到磁盘。 */
    public void save() {
        try {
            Files.createDirectories(baseDir());
            String json = JsonUtils.toJson(this);
            Files.writeString(configFile(), json);
            log.info("Config saved to {}", configFile());
        } catch (IOException e) {
            throw new ConfigException("Failed to save config to " + configFile(), e);
        }
    }

    // ── 内部 ──

    private static void backupCorruptFile() {
        Path file = configFile();
        try {
            Path backup = Paths.get(file.toString() + ConfigConstants.CORRUPT_SUFFIX
                + System.currentTimeMillis());
            Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
            log.info("Corrupt config backed up to {}", backup);
        } catch (IOException ex) {
            log.warn("Failed to backup corrupt config, overwriting it", ex);
        }
    }

    // ── Getters / Setters ──

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getDataDirectory() { return dataDirectory; }
    public void setDataDirectory(String dataDirectory) { this.dataDirectory = dataDirectory; }

    public String getPobPath() { return pobPath; }
    public void setPobPath(String pobPath) { this.pobPath = pobPath; }

    public boolean isAutoSync() { return autoSync; }
    public void setAutoSync(boolean autoSync) { this.autoSync = autoSync; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public int getWindowWidth() { return windowWidth; }
    public void setWindowWidth(int windowWidth) { this.windowWidth = windowWidth; }

    public int getWindowHeight() { return windowHeight; }
    public void setWindowHeight(int windowHeight) { this.windowHeight = windowHeight; }

    /** 配置根目录 {@code ~/.poe-tool/} 的绝对路径。 */
    public static Path getConfigDir() { return baseDir(); }

    /** 配置文件 {@code ~/.poe-tool/config.json} 的绝对路径。 */
    public static Path getConfigFile() { return configFile(); }

    /** 重置单例（仅供测试）。 */
    static void resetForTest() {
        instance = null;
    }
}
