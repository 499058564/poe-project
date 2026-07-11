package com.poe.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Path of Building 社区数据提取器。
 *
 * <h3>定位</h3>
 * POB 数据用于<b>验证</b> Wiki 同步结果，不作为主数据源。
 * Wiki 数据优先，POB 作为完整性校验的补充。
 *
 * <h3>数据来源</h3>
 * 从 {@code pob-runtime/src/main/pob/} 子模块读取数据文件：
 * <ul>
 *   <li>{@code Data/3_0/Skills/} — 技能宝石 Lua 定义</li>
 *   <li>{@code Data/3_0/PassiveTree/} — 天赋树 JSON/XML</li>
 *   <li>{@code Data/3_0/Mods/} — 词缀定义</li>
 *   <li>{@code Data/3_0/BaseItems/} — 基础物品</li>
 *   <li>{@code tree.lua / tree_3_*.lua} — 版本号与天赋树版本</li>
 * </ul>
 *
 * <h3>使用方式</h3>
 * <pre>{@code
 * PobDataExtractor extractor = new PobDataExtractor(
 *     Paths.get("pob-runtime/src/main/pob")
 * );
 * // 获取 POB 中的技能宝石数量，与 Wiki 同步结果比对
 * int pobSkillCount = extractor.countSkillGemFiles();
 * // 获取 POB 游戏版本号
 * String gameVersion = extractor.extractGameVersion();
 * }</pre>
 *
 * <h3>当前状态</h3>
 * {@code pob-runtime} 子模块尚未初始化 POB 仓库代码。
 * 当前实现提供路径解析骨架，POB 子模块就绪后可直接填充数据读取逻辑。
 */
public class PobDataExtractor {

    private static final Logger log = LoggerFactory.getLogger(PobDataExtractor.class);

    /** POB 数据根目录。 */
    private final Path pobRoot;

    private final ObjectMapper objectMapper;

    /**
     * 创建 POB 数据提取器。
     *
     * @param pobRoot POB 仓库根目录，如 {@code pob-runtime/src/main/pob}
     */
    public PobDataExtractor(Path pobRoot) {
        this.pobRoot = pobRoot;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 使用默认路径创建提取器。
     * 默认路径：{@code pob-runtime/src/main/pob/}
     */
    public PobDataExtractor() {
        this(Paths.get("pob-runtime", "src", "main", "pob"));
    }

    // ==================== 公开方法 ====================

    /**
     * 检查 POB 数据目录是否存在。
     *
     * @return true 如果 POB 数据根目录存在且可读
     */
    public boolean isDataAvailable() {
        return Files.isDirectory(pobRoot) && Files.isReadable(pobRoot);
    }

    /**
     * 从 POB 数据文件提取游戏版本号。
     *
     * <p>版本号存储在 {@code tree.lua} 或 {@code tree_3_*.lua} 中，
     * 格式为 {@code treeVersion = "3.25"} 或 {@code gameVersion = "3.25.0"}。
     *
     * @return 游戏版本号字符串，数据不可用时返回 {@code "unknown"}
     */
    public String extractGameVersion() {
        if (!isDataAvailable()) {
            log.warn("POB data directory not available: {}", pobRoot);
            return "unknown";
        }

        // TODO: 解析 tree.lua 或 tree_3_*.lua 中的版本号
        // 当前为骨架实现，待 POB 子模块就绪后填充
        log.info("POB game version extraction not yet implemented — POB data not synced");
        return "unknown";
    }

    /**
     * 统计技能宝石定义文件数量（用于验证 Wiki 同步完整性）。
     *
     * @return 技能文件数，数据不可用时返回 0
     */
    public int countSkillGemFiles() {
        Path skillsDir = pobRoot.resolve("Data").resolve("3_0").resolve("Skills");
        if (!Files.isDirectory(skillsDir)) {
            log.warn("POB Skills directory not found: {}", skillsDir);
            return 0;
        }

        // TODO: 统计 skillsDir 下的 .lua 文件数量
        log.info("POB skill gem counting not yet implemented — POB data not synced");
        return 0;
    }

    /**
     * 获取 POB 数据根目录路径。
     *
     * @return POB 根目录的绝对路径
     */
    public Path getPobRoot() {
        return pobRoot.toAbsolutePath();
    }
}
