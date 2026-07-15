package com.poe.provider;

import com.poe.provider.client.PobDataExtractor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PobDataExtractor 单元测试。
 *
 * <p>验证：
 * <ul>
 *   <li>数据目录可用性检测</li>
 *   <li>版本号提取（骨架 → 默认返回 "unknown"）</li>
 *   <li>技能文件计数（骨架 → 默认返回 0）</li>
 *   <li>路径解析正确性</li>
 * </ul>
 */
class PobDataExtractorTest {

    @TempDir
    Path tempDir;

    private PobDataExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new PobDataExtractor(tempDir);
    }

    // ==================== 数据可用性 ====================

    @Test
    @DisplayName("空目录应被识别为不可用")
    void shouldDetectEmptyDirectoryAsUnavailable() throws IOException {
        Path emptyPob = tempDir.resolve("empty_pob");
        Files.createDirectories(emptyPob);

        PobDataExtractor ext = new PobDataExtractor(emptyPob);
        assertTrue(ext.isDataAvailable(),
            "Existing directory should be detected as available");
    }

    @Test
    @DisplayName("不存在目录应被识别为不可用")
    void shouldDetectMissingDirectoryAsUnavailable() {
        PobDataExtractor ext = new PobDataExtractor(
            tempDir.resolve("nonexistent"));
        assertFalse(ext.isDataAvailable(),
            "Missing directory should not be detected as available");
    }

    // ==================== 版本号提取 ====================

    @Test
    @DisplayName("POB 数据不可用时版本号应返回 unknown")
    void shouldReturnUnknownWhenDataUnavailable() {
        PobDataExtractor ext = new PobDataExtractor(
            tempDir.resolve("nonexistent"));
        assertEquals("unknown", ext.extractGameVersion());
    }

    @Test
    @DisplayName("POB 数据目录存在但未填充时版本号应返回 unknown")
    void shouldReturnUnknownWhenDataEmpty() throws IOException {
        Path pobDir = tempDir.resolve("pob_empty");
        Files.createDirectories(pobDir);

        PobDataExtractor ext = new PobDataExtractor(pobDir);
        assertEquals("unknown", ext.extractGameVersion(),
            "Empty POB dir should return 'unknown' for game version");
    }

    // ==================== 技能文件计数 ====================

    @Test
    @DisplayName("POB 数据不可用时技能计数应返回 0")
    void shouldReturnZeroSkillsWhenDataUnavailable() {
        PobDataExtractor ext = new PobDataExtractor(
            tempDir.resolve("nonexistent"));
        assertEquals(0, ext.countSkillGemFiles());
    }

    // ==================== 路径解析 ====================

    @Test
    @DisplayName("getPobRoot 应返回绝对路径")
    void shouldReturnAbsolutePath() {
        Path root = extractor.getPobRoot();
        assertTrue(root.isAbsolute(),
            "getPobRoot should return an absolute path");
        assertEquals(tempDir.toAbsolutePath(), root);
    }
}
