package com.poe.provider;

import com.poe.provider.tool.ModelDaoGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Model + DAO 代码生成器入口。
 *
 * <p>修改下方常量后直接运行 main 即可，无需从命令行传参。
 *
 * @author yuziyang
 * @since 2026/7/15
 */
public class ModelDaoGeneratorMain {

    private static final Logger log = LoggerFactory.getLogger(ModelDaoGeneratorMain.class);

    // ======================== 配置区域 ========================

    /**
     * SQLite 数据库文件路径。
     * 支持相对于项目根目录的路径，运行时自动适配子模块工作目录。
     */
    private static final String DB_PATH = resolvePath("data-provider/src/main/resources/seed.db");

    /** 输出根目录 */
    private static final Path OUTPUT_DIR = Paths.get(resolvePath("data-cache/src/main/java"));

    /**
     * 要生成的表名集合。
     * <ul>
     *   <li>{@code null} — 全量生成所有表</li>
     *   <li>{@code Set.of("wiki_table_info", "poe_project_sys_config")} — 只生成指定表</li>
     * </ul>
     */
    private static final Set<String> TABLE_FILTER = null;

    // ======================== 路径解析 ========================

    /**
     * 将相对于项目根目录的路径解析为实际路径。
     * 自动处理 Gradle 子模块工作目录与根目录的差异。
     */
    private static String resolvePath(String relativePath) {
        // 优先从项目根目录解析（向上找到包含 settings.gradle 的目录）
        Path cwd = Paths.get("").toAbsolutePath();
        Path root = cwd;
        while (root != null && !Files.exists(root.resolve("settings.gradle.kts"))
                && !Files.exists(root.resolve("settings.gradle"))) {
            root = root.getParent();
        }
        if (root != null) {
            Path resolved = root.resolve(relativePath);
            if (Files.exists(resolved)) {
                return resolved.toString();
            }
        }
        // 回退：直接检测当前目录
        Path path = Paths.get(relativePath);
        if (Files.exists(path)) {
            return path.toAbsolutePath().normalize().toString();
        }
        // 最后尝试上级目录
        Path fromParent = Paths.get("..").resolve(relativePath);
        if (Files.exists(fromParent)) {
            return fromParent.toAbsolutePath().normalize().toString();
        }
        return path.toString();
    }

    // ======================== main ========================

    public static void main(String[] args) {
        try {
            new ModelDaoGenerator(DB_PATH, OUTPUT_DIR, TABLE_FILTER).generate();
            log.info("Model + DAO generation completed.");
        } catch (Exception e) {
            log.error("Generation failed: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}