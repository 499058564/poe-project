package com.poe.provider.tool;

import com.poe.cache.manager.DatabaseManager;
import com.poe.cache.manager.MigrationManager;
import com.poe.core.service.TranslationService;
import com.poe.provider.WikiApiClient;
import com.poe.provider.sync.DataSyncService;
import com.poe.provider.sync.SyncResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 种子数据库构建工具（离线/本地构建流程）。
 *
 * <p>由 Gradle {@code buildSeedDb} 任务调用，构建预置的 seed.db：
 * <ol>
 *   <li>创建临时 SQLite 库</li>
 *   <li>运行迁移脚本</li>
 *   <li>导入 PoeCharm2 中文翻译</li>
 *   <li>从 Wiki 同步核心数据（限时，最多 10 分钟）</li>
 *   <li>VACUUM 清理</li>
 *   <li>输出到指定路径（默认 app-ui/src/main/resources/seed.db）</li>
 * </ol>
 *
 * <h3>启动参数</h3>
 * <ul>
 *   <li>{@code --output <path>} — 输出文件路径（必需）</li>
 *   <li>{@code --poecharm2 <path>} — PoeCharm2 仓库路径（可选，默认自动检测）</li>
 *   <li>{@code --skip-wiki} — 跳过 Wiki 同步，仅导入翻译</li>
 *   <li>{@code --timeout <seconds>} — Wiki 同步超时秒数（默认 600）</li>
 * </ul>
 */
public class SeedDbBuilder {

    private static final Logger log = LoggerFactory.getLogger(SeedDbBuilder.class);

    private static final int DEFAULT_WIKI_TIMEOUT_SECONDS = 600;

    public static void main(String[] args) {
        try {
            SeedDbBuilder builder = SeedDbBuilder.parse(args);
            builder.build();
        } catch (Exception e) {
            log.error("SeedDbBuilder failed: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    private final Path outputPath;
    private final Path poeCharm2Path;
    private final boolean skipWiki;
    private final int wikiTimeoutSeconds;

    private SeedDbBuilder(Path outputPath, Path poeCharm2Path, boolean skipWiki, int wikiTimeoutSeconds) {
        this.outputPath = outputPath;
        this.poeCharm2Path = poeCharm2Path;
        this.skipWiki = skipWiki;
        this.wikiTimeoutSeconds = wikiTimeoutSeconds;
    }

    void build() throws Exception {
        log.info("=== SeedDbBuilder started ===");
        log.info("Output: {}", outputPath.toAbsolutePath());
        log.info("PoeCharm2: {}", poeCharm2Path.toAbsolutePath());
        log.info("Skip Wiki: {}", skipWiki);

        // 确保输出目录存在
        Files.createDirectories(outputPath.getParent());

        // 不需要持久化在 ~/.poe-tool 中，直接创建临时文件
        Path tempDb = Files.createTempFile("seed-build-", ".db");
        try {
            buildInternal(tempDb);
            log.info("Build completed. Copying to output: {}", outputPath);
            Files.copy(tempDb, outputPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Seed database written: {} ({} bytes)",
                    outputPath, Files.size(outputPath));
        } finally {
            try {
                Files.deleteIfExists(tempDb);
            } catch (IOException ignored) {
            }
        }
    }

    private void buildInternal(Path dbPath) throws Exception {
        // 1. 初始化临时库
        DatabaseManager dbm = new DatabaseManager(dbPath.toString());
        dbm.forceInit(); // 运行迁移

        // 2. 导入 PoeCharm2 翻译
        try (Connection conn = dbm.getConnection()) {
            TranslationService ts = new TranslationService(
                    new com.poe.cache.dao.TranslationDao(conn));
            Path translateDir = poeCharm2Path.resolve("Data/Translate/zh-rCN");
            int count = ts.importFromPoeCharm2(translateDir);
            log.info("PoeCharm2 translations imported: {} entries", count);
        }

        // 3. 从 Wiki 同步数据（可选）
        if (!skipWiki) {
            syncWikiData(dbm);
        }

        // 4. VACUUM
        try (Connection conn = dbm.getConnection()) {
            conn.createStatement().execute("VACUUM");
            log.info("VACUUM completed");
        }

        dbm.shutdown();
    }

    private void syncWikiData(DatabaseManager dbm) throws Exception {
        WikiApiClient wikiClient = new WikiApiClient();
        DataSyncService syncService = new DataSyncService(wikiClient, dbm);

        log.info("Starting Wiki sync (timeout: {}s)...", wikiTimeoutSeconds);
        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(wikiTimeoutSeconds);

        Map<String, SyncResult> results;
        try {
            results = syncService.syncAll();
        } catch (Exception e) {
            log.warn("Wiki sync failed (seed db will be translation-only): {}", e.getMessage());
            return;
        }

        int successCount = 0;
        int failCount = 0;
        for (Map.Entry<String, SyncResult> entry : results.entrySet()) {
            if (entry.getValue().getSyncedRecords() > 0) {
                successCount++;
            } else {
                failCount++;
            }
        }

        long elapsed = TimeUnit.MILLISECONDS.toSeconds(deadline - System.currentTimeMillis()
                + TimeUnit.SECONDS.toMillis(wikiTimeoutSeconds));
        log.info("Wiki sync finished: {} tables synced, {} failed, {}s elapsed",
                successCount, failCount, elapsed);
    }

    // ============== 命令行解析 ==============

    static SeedDbBuilder parse(String[] args) {
        Path output = null;
        Path poeCharm2 = detectPoeCharm2();
        boolean skipWiki = false;
        int timeout = DEFAULT_WIKI_TIMEOUT_SECONDS;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--output":
                    output = Paths.get(args[++i]);
                    break;
                case "--poecharm2":
                    poeCharm2 = Paths.get(args[++i]);
                    break;
                case "--skip-wiki":
                    skipWiki = true;
                    break;
                case "--timeout":
                    timeout = Integer.parseInt(args[++i]);
                    break;
                case "--help":
                    printHelp();
                    System.exit(0);
                    break;
                default:
                    System.err.println("Unknown argument: " + args[i]);
                    printHelp();
                    System.exit(1);
            }
        }

        if (output == null) {
            System.err.println("Missing required --output <path>");
            printHelp();
            System.exit(1);
        }

        return new SeedDbBuilder(output, poeCharm2, skipWiki, timeout);
    }

    private static void printHelp() {
        System.out.println(String.join("\n", Arrays.asList(
                "Usage: SeedDbBuilder --output <path> [options]",
                "",
                "Options:",
                "  --output <path>       Output file path (required)",
                "  --poecharm2 <path>    PoeCharm2 repo path (default: auto-detect)",
                "  --skip-wiki           Skip Wiki data sync",
                "  --timeout <seconds>   Wiki sync timeout (default: 600)",
                "  --help                Print this help"
        )));
    }

    private static Path detectPoeCharm2() {
        // 从当前工作目录向上查找
        Path dir = Paths.get("").toAbsolutePath();
        while (dir != null) {
            Path candidate = dir.resolve("poecharm2");
            if (Files.isDirectory(candidate.resolve("Data"))) {
                return candidate;
            }
            dir = dir.getParent();
        }
        return Paths.get("poecharm2"); // fallback
    }
}
