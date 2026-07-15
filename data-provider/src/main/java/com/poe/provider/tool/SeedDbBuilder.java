package com.poe.provider.tool;

import com.poe.cache.manager.DatabaseManager;
import com.poe.provider.client.WikiApiClient;
import com.poe.provider.sync.DataSyncService;
import com.poe.provider.sync.SyncResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 种子数据库构建工具（离线/本地构建流程）。
 *
 * <p>由 Gradle {@code buildSeedDb} 任务调用，构建预置的 seed.db：
 * <ol>
 *   <li>直接在 outputPath 上建库（不存在则创建，存在则执行 DDL）</li>
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

    private static final String DEFAULT_OUTPUT_PATH = "data-provider/src/main/resources/seed.db";

    private static final String INIT_DB_SQL_PATH = "data-provider/src/main/resources/db/init_ddl.sql";

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

    /**
     * 构建种子数据库。
     * @throws Exception
     */
    public void build() throws Exception {
        log.info("=== SeedDbBuilder started ===");
        log.info("Output: {}", outputPath.toAbsolutePath());
        log.info("PoeCharm2: {}", poeCharm2Path.toAbsolutePath());
        log.info("Skip Wiki: {}", skipWiki);

        // 确保输出目录存在
        Files.createDirectories(outputPath.getParent());

        boolean dbExists = Files.exists(outputPath);
        log.info("Database {} {}", outputPath, dbExists ? "exists, will execute DDL" : "not found, will create");

        buildInternal();

        log.info("Seed database ready: {} ({} bytes)",
                outputPath, Files.exists(outputPath) ? Files.size(outputPath) : 0);
    }

    /**
     * 直接在 outputPath 上执行建库和初始化。
     * @throws Exception
     */
    private void buildInternal() throws Exception {
        // 直接在 outputPath 上操作：不存在自动创建，存在则执行 DDL（IF NOT EXISTS 确保幂等）
        DatabaseManager dbm = new DatabaseManager(outputPath.toString());
        dbm.forceInit();
        //初始化数据库
        initDb(dbm);

        // 2. 导入 PoeCharm2 翻译
        /*TranslationService ts = new TranslationService(dbm.getDataSource());
        Path translateDir = poeCharm2Path.resolve("Data/Translate/zh-rCN");
        int count = ts.importFromPoeCharm2(translateDir);
        log.info("PoeCharm2 translations imported: {} entries", count);*/

        // 3. 从 Wiki 同步数据（可选）
        /*if (!skipWiki) {
            syncWikiData(dbm);
        }*/

        // 4. VACUUM
        try (Connection conn = dbm.getConnection()) {
            conn.createStatement().execute("VACUUM");
            log.info("VACUUM completed");
        }

        dbm.shutdown();
    }

    private void initDb(DatabaseManager dbm) throws Exception {
        log.info("SeedDbBuilder|buildInternal|开始执行初始化表");
        String ddl = Files.readString(Path.of(INIT_DB_SQL_PATH));
        try (Statement stmt = dbm.getConnection().createStatement()) {
            for (String sql : ddl.split(";")) {
                // 只剔除独立的注释行，保留行内注释（SQLite 能正确解析）
                String cleaned = Arrays.stream(sql.split("\n"))
                        .map(String::strip)
                        .filter(line -> !line.isEmpty() && !line.startsWith("--"))
                        .collect(Collectors.joining("\n"));
                if (!cleaned.isBlank()) {
                    stmt.execute(cleaned);
                }
            }
        }
        log.info("SeedDbBuilder|buildInternal|执行初始化表结束");
    }

    /**
     * 同步wiki数据
     * @param dbm 数据库管理器
     * @throws Exception
     */
    private void syncWikiData(DatabaseManager dbm) throws Exception {
        //TODO yzy 数据同步
        // 1. 先拉取表名入库
        // 2. 再拉取表字段入库
        // 3. 根据表名和字段,生成建表语句
        // 4. 执行建表语句
        // 5. 拉取数据


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

    /**
     * 命令行参数解析
     * @param args 命令行参数
     * @return SeedDbBuilder 实例
     */
    public static SeedDbBuilder parse(String[] args) {
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

    /**
     * 自动检测 poecharm2 路径
     * @return poecharm2 路径
     */
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
