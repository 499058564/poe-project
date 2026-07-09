package com.poe.core.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AppConfig 单元测试。
 * 通过覆盖系统属性隔离测试环境，避免污染真实 ~/.poe-tool/。
 */
class AppConfigTest {

    private Path tempHome;
    private Path poeToolDir;

    @BeforeEach
    void setUp() throws IOException {
        // 隔离：用临时目录作为 ~/.poe-tool 根
        tempHome = Files.createTempDirectory("poe-config-test-");
        poeToolDir = tempHome.resolve(".poe-tool");
        AppConfig.setTestConfigDir(poeToolDir);
        AppConfig.resetForTest();
    }

    @AfterEach
    void tearDown() throws IOException {
        AppConfig.setTestConfigDir(null);
        AppConfig.resetForTest();
        // 清理临时目录
        try (var stream = Files.walk(tempHome)) {
            stream.sorted(java.util.Comparator.reverseOrder())
                .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
        }
    }

    // ── 首次运行 ──

    @Test
    void firstRun_shouldCreateDirectoriesAndDefaultConfig() {
        AppConfig config = AppConfig.getInstance();

        Path configDir = poeToolDir;
        assertTrue(Files.exists(configDir), "配置根目录应自动创建");
        assertTrue(Files.exists(configDir.resolve("data")), "data/ 子目录应自动创建");
        assertTrue(Files.exists(configDir.resolve("logs")), "logs/ 子目录应自动创建");
        assertTrue(Files.exists(configDir.resolve("config.json")), "config.json 应自动创建");

        // 默认值校验
        assertEquals("zh", config.getLanguage());
        assertTrue(config.getDataDirectory().endsWith("data"), "默认 dataDirectory 应指向 data/");
        assertNull(config.getPobPath());
        assertTrue(config.isAutoSync());
        assertEquals("dark", config.getTheme());
        assertEquals(1280, config.getWindowWidth());
        assertEquals(800, config.getWindowHeight());
    }

    // ── 保存 / 加载 ──

    @Test
    void saveAndReload_shouldPersistChanges() {
        AppConfig config = AppConfig.getInstance();
        config.setLanguage("en");
        config.setPobPath("/custom/pob");
        config.setAutoSync(false);
        config.setWindowWidth(1920);
        config.setWindowHeight(1080);
        config.save();

        // 重新加载
        AppConfig reloaded = AppConfig.reload();

        assertEquals("en", reloaded.getLanguage());
        assertEquals("/custom/pob", reloaded.getPobPath());
        assertFalse(reloaded.isAutoSync());
        assertEquals(1920, reloaded.getWindowWidth());
        assertEquals(1080, reloaded.getWindowHeight());
    }

    // ── 文件损坏恢复 ──

    @Test
    void corruptConfig_shouldResetToDefaults() throws IOException {
        // 先创建有效配置
        AppConfig config = AppConfig.getInstance();
        config.setLanguage("en");
        config.save();

        // 写入损坏的 JSON
        Path configFile = poeToolDir.resolve("config.json");
        Files.writeString(configFile, "{ this is not valid json ---");

        // 重新加载
        AppConfig recovered = AppConfig.reload();

        assertEquals("zh", recovered.getLanguage(),
            "损坏后应返回默认 language");
        assertTrue(Files.exists(configFile),
            "config.json 应被重新生成");
    }

    // ── 静态路径 ──

    @Test
    void getConfigDir_shouldPointToPoeToolInUserHome() {
        assertEquals(poeToolDir, AppConfig.getConfigDir());
    }

    @Test
    void getConfigFile_shouldPointToConfigJson() {
        Path expected = poeToolDir.resolve("config.json");
        assertEquals(expected, AppConfig.getConfigFile());
    }
}
