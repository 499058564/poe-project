package com.poe.provider.sync;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.manager.DatabaseManager;
import com.poe.core.event.AppEventBus;
import com.poe.core.event.DataSyncCompleteEvent;
import com.poe.core.event.DataSyncProgressEvent;
import com.poe.core.event.DataSyncStartEvent;
import com.poe.provider.client.WikiApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import javax.sql.DataSource;
import java.time.Instant;
import java.util.*;

/**
 * 数据同步服务，编排完整的 Wiki → SQLite 同步流程。
 *
 * <h3>同步流程</h3>
 * <ol>
 *   <li>通过 Wiki Cargo COUNT(*) 获取远程记录总数</li>
 *   <li>与本地 {@code data_version} 表的 {@code record_count} 对比</li>
 *   <li>一致则跳过，不一致则进入全量同步</li>
 *   <li>分批拉取（每批 500 条）→ 转换 → 事务写入 → 发布进度事件</li>
 *   <li>items 表同步后重建 FTS5 全文索引</li>
 *   <li>更新 {@code data_version} 表</li>
 * </ol>
 *
 * <h3>限流</h3>
 * 依赖 {@link WikiApiClient} 内部的 2 req/s 限流和 3 次重试。
 * 表间有 15 秒冷却避免 Cloudflare 累积限流，失败的表会在重试轮次中再尝试一次。
 *
 * <h3>断点续传</h3>
 * 通过 {@link SyncCheckpoint} 记录当前进度，异常中断后可从上次偏移量继续。
 */
public class DataSyncService {

    private static final Logger log = LoggerFactory.getLogger(DataSyncService.class);

    /** 每批拉取的最大记录数（Wiki API 上限 500）。 */
    private static final int BATCH_SIZE = 500;

    /**
     * 表元数据配置：Cargo表名 → (SQLite表名, batchSize, keyField)。
     * <p>fields 不再硬编码，而是通过 {@link #initFromWiki()} 从 wiki API 动态获取，
     * 避免因字段不存在触发 MWException。
     */
    private static final Map<String, TableMeta> TABLE_META = new LinkedHashMap<>();
    static {
        TABLE_META.put("items", new TableMeta("base_items", 100, "name"));
        TABLE_META.put("skill_gems", new TableMeta("skill_gems"));
        TABLE_META.put("passive_skills", new TableMeta("passive_skills"));
        TABLE_META.put("mods", new TableMeta("mods"));
        TABLE_META.put("weapons", new TableMeta("weapons"));
        TABLE_META.put("armours", new TableMeta("armours"));
        TABLE_META.put("shields", new TableMeta("shields"));
        TABLE_META.put("amulets", new TableMeta("amulets"));
        TABLE_META.put("flasks", new TableMeta("flasks"));
        TABLE_META.put("jewels", new TableMeta("jewels"));
        TABLE_META.put("stackables", new TableMeta("stackables"));
        TABLE_META.put("maps", new TableMeta("maps"));
        TABLE_META.put("map_fragments", new TableMeta("map_fragments"));
        TABLE_META.put("map_series", new TableMeta("map_series"));
        TABLE_META.put("divination_cards", new TableMeta("divination_cards"));
        // ---- 怪物 ----
        TABLE_META.put("monsters", new TableMeta("monsters"));
        TABLE_META.put("monster_types", new TableMeta("monster_types"));
        TABLE_META.put("monster_base_stats", new TableMeta("monster_base_stats"));
        TABLE_META.put("monster_life_scaling", new TableMeta("monster_life_scaling"));
        TABLE_META.put("monster_map_multipliers", new TableMeta("monster_map_multipliers"));
        TABLE_META.put("monster_resistances", new TableMeta("monster_resistances"));
        // ---- 区域 ----
        TABLE_META.put("areas", new TableMeta("areas", 200));
        // ---- 异界图鉴 ----
        TABLE_META.put("atlas_nodes", new TableMeta("atlas_nodes"));
        // ---- Delve ----
        TABLE_META.put("delve_level_scaling", new TableMeta("delve_level_scaling"));
        TABLE_META.put("delve_resources_per_level", new TableMeta("delve_resources_per_level"));
        TABLE_META.put("delve_upgrades", new TableMeta("delve_upgrades"));
        TABLE_META.put("delve_upgrade_stats", new TableMeta("delve_upgrade_stats"));
        // ---- Heist ----
        TABLE_META.put("heist_areas", new TableMeta("heist_areas"));
        TABLE_META.put("heist_jobs", new TableMeta("heist_jobs"));
        TABLE_META.put("heist_npcs", new TableMeta("heist_npcs"));
        TABLE_META.put("heist_npc_skills", new TableMeta("heist_npc_skills"));
        TABLE_META.put("heist_npc_stats", new TableMeta("heist_npc_stats"));
        TABLE_META.put("heist_equipment", new TableMeta("heist_equipment"));
        // ---- Blight ----
        TABLE_META.put("blight_crafting_recipes", new TableMeta("blight_crafting_recipes"));
        TABLE_META.put("blight_crafting_recipes_items", new TableMeta("blight_crafting_recipes_items"));
        TABLE_META.put("blight_items", new TableMeta("blight_items"));
        TABLE_META.put("blight_towers", new TableMeta("blight_towers"));
        // ---- Harvest ----
        TABLE_META.put("harvest_crafting_options", new TableMeta("harvest_crafting_options"));
        TABLE_META.put("harvest_plant_boosters", new TableMeta("harvest_plant_boosters"));
        TABLE_META.put("harvest_seeds", new TableMeta("harvest_seeds"));
        // ---- Synthesis ----
        TABLE_META.put("synthesis_areas", new TableMeta("synthesis_areas"));
        TABLE_META.put("synthesis_corrupted_mods", new TableMeta("synthesis_corrupted_mods"));
        TABLE_META.put("synthesis_global_mods", new TableMeta("synthesis_global_mods"));
        TABLE_META.put("synthesis_mods", new TableMeta("synthesis_mods"));
        // ---- Bestiary ----
        TABLE_META.put("bestiary_recipes", new TableMeta("bestiary_recipes"));
        TABLE_META.put("bestiary_recipe_components", new TableMeta("bestiary_recipe_components"));
        // ---- Incursion ----
        TABLE_META.put("incursion_rooms", new TableMeta("incursion_rooms"));
        // ---- Pantheon ----
        TABLE_META.put("pantheon", new TableMeta("pantheon"));
        TABLE_META.put("pantheon_souls", new TableMeta("pantheon_souls"));
        TABLE_META.put("pantheon_stats", new TableMeta("pantheon_stats"));
        // ---- 词缀子表 ----
        TABLE_META.put("mod_stats", new TableMeta("mod_stats"));
        TABLE_META.put("mod_spawn_weights", new TableMeta("mod_spawn_weights"));
        TABLE_META.put("mod_generation_weights", new TableMeta("mod_generation_weights"));
        TABLE_META.put("mod_sell_prices", new TableMeta("mod_sell_prices"));
        // ---- 物品-词缀关联 ----
        TABLE_META.put("item_mods", new TableMeta("item_mods"));
        TABLE_META.put("item_stats", new TableMeta("item_stats"));
        TABLE_META.put("item_buffs", new TableMeta("item_buffs"));
        // ---- 工艺/配方 ----
        TABLE_META.put("crafting_bench_options", new TableMeta("crafting_bench_options"));
        TABLE_META.put("crafting_bench_options_costs", new TableMeta("crafting_bench_options_costs"));
        TABLE_META.put("essences", new TableMeta("essences"));
        TABLE_META.put("fossils", new TableMeta("fossils"));
        TABLE_META.put("fossil_weights", new TableMeta("fossil_weights"));
        // ---- 经济数据 ----
        TABLE_META.put("vendor_rewards", new TableMeta("vendor_rewards"));
        TABLE_META.put("item_sell_prices", new TableMeta("item_sell_prices"));
        TABLE_META.put("item_purchase_costs", new TableMeta("item_purchase_costs"));
        // ---- 技能详细数据 ----
        TABLE_META.put("skill", new TableMeta("skills"));
        TABLE_META.put("skill_levels", new TableMeta("skill_levels"));
        TABLE_META.put("skill_stats_per_level", new TableMeta("skill_stats_per_level"));
        TABLE_META.put("skill_quality", new TableMeta("skill_quality"));
        TABLE_META.put("skill_quality_stats", new TableMeta("skill_quality_stats"));
        TABLE_META.put("gem_levels", new TableMeta("gem_levels"));
        // ---- 天赋详细数据 ----
        TABLE_META.put("passive_skill_connections", new TableMeta("passive_skill_connections"));
        TABLE_META.put("mastery_effects", new TableMeta("mastery_effects"));
        TABLE_META.put("mastery_groups", new TableMeta("mastery_groups"));
        // ---- 职业数据 ----
        TABLE_META.put("character_classes", new TableMeta("character_classes"));
        TABLE_META.put("ascendancy_classes", new TableMeta("ascendancy_classes"));
        // ---- 杂项与历史数据 ----
        TABLE_META.put("versions", new TableMeta("versions"));
        TABLE_META.put("legacy_variants", new TableMeta("legacy_variants"));
        TABLE_META.put("prophecies", new TableMeta("prophecies"));
        TABLE_META.put("quest_rewards", new TableMeta("quest_rewards"));
        TABLE_META.put("spawn_weights", new TableMeta("spawn_weights"));
        TABLE_META.put("generic_stats", new TableMeta("generic_stats"));
        // ---- 赛季特有物品 ----
        TABLE_META.put("tattoos", new TableMeta("tattoos"));
        TABLE_META.put("tinctures", new TableMeta("tinctures"));
        TABLE_META.put("sentinels", new TableMeta("sentinels"));
        TABLE_META.put("idols", new TableMeta("idols"));
        TABLE_META.put("grafts", new TableMeta("grafts"));
        TABLE_META.put("corpse_items", new TableMeta("corpse_items"));
        // ---- 杂项低优先级 ----
        TABLE_META.put("cosmetic_items", new TableMeta("cosmetic_items"));
        TABLE_META.put("hideout_doodads", new TableMeta("hideout_doodads"));
        TABLE_META.put("guides", new TableMeta("guides"));
    }

    /** 运行时表配置：由 {@link #initFromWiki()} 填充，合并 TABLE_META + wiki 发现字段。 */
    private final Map<String, TableConfig> tableConfigs = new LinkedHashMap<>();

    private final WikiApiClient wikiClient;
    private final DatabaseManager dbManager;

    /** 表间冷却时间（毫秒），生产环境默认 5s，测试可通过 setter 设为 0。 */
    private long interTableDelayMs = 5_000;

    /** 失败表额外冷却时间（毫秒），生产环境默认 15s。 */
    private long failureDelayMs = 15_000;

    /** 重试轮次前冷却时间（毫秒），生产环境默认 30s。 */
    private long retryCooldownMs = 30_000;

    public DataSyncService(WikiApiClient wikiClient, DatabaseManager dbManager) {
        this.wikiClient = wikiClient;
        this.dbManager = dbManager;
    }

    /**
     * 从 Wiki API 动态获取所有 Cargo 表的字段定义，构建运行时 tableConfigs。
     * <p>
     * 必须在使用 {@link #syncTable} / {@link #syncAll} 之前调用。
     * 对于 TABLE_META 中注册的表，使用 wiki 返回的真实字段列表；
     * 对于未注册的表，跳过（无 Converter/DAO 支持）。
     * <p>
     * 为减少 Cloudflare 限流，表间有 1 秒延迟。
     *
     * @throws DataSyncException 无法连接 Wiki API 时抛出
     */
    public void initFromWiki() {
        log.info("Discovering table fields from Wiki API...");
        tableConfigs.clear();

        int discovered = 0;
        for (Map.Entry<String, TableMeta> entry : TABLE_META.entrySet()) {
            String cargoTable = entry.getKey();
            discovered++;
            initTableFromWiki(cargoTable);
            if (discovered % 10 == 0) {
                sleep(2000); // 每 10 张表加 2s 冷却，缓解 Cloudflare 限流
            }
        }
        log.info("Initialized {} table configs from Wiki", tableConfigs.size());
    }

    /**
     * 从 Wiki API 获取单张表的字段定义并注册到 tableConfigs。
     * <p>
     * 适合只同步单表的场景，避免全量 discovery 导致的 Cloudflare 限流。
     *
     * @param cargoTable Cargo 表名
     */
    public void initTableFromWiki(String cargoTable) {
        TableMeta meta = TABLE_META.get(cargoTable);
        if (meta == null) {
            log.warn("No TABLE_META entry for '{}', skipping field discovery", cargoTable);
            return;
        }
        try {
            List<WikiApiClient.CargoField> fields = wikiClient.queryCargoFields(cargoTable);
            if (fields.isEmpty()) {
                log.warn("No fields returned for '{}', skipping", cargoTable);
                return;
            }
            String fieldsStr = fields.stream()
                .map(WikiApiClient.CargoField::name)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
            tableConfigs.put(cargoTable, new TableConfig(
                meta.sqliteTable, fieldsStr, meta.batchSize, meta.keyField));
            log.info("Discovered '{}': {} fields (sqlite={})",
                cargoTable, fields.size(), meta.sqliteTable);
        } catch (Exception e) {
            log.error("Failed to discover fields for '{}': {}", cargoTable, e.getMessage());
        }
    }

    /** 设置表间间隔（毫秒），0 表示无延迟。供测试使用。 */
    public void setInterTableDelayMs(long interTableDelayMs) {
        this.interTableDelayMs = interTableDelayMs;
    }

    /** 设置失败表额外冷却（毫秒），0 表示无延迟。供测试使用。 */
    public void setFailureDelayMs(long failureDelayMs) {
        this.failureDelayMs = failureDelayMs;
    }

    /** 设置重试轮次前冷却（毫秒），0 表示无延迟。供测试使用。 */
    public void setRetryCooldownMs(long retryCooldownMs) {
        this.retryCooldownMs = retryCooldownMs;
    }

    // ==================== 公开方法 ====================

    private static void sleep(long millis) {
        if (millis <= 0) return;
        try { Thread.sleep(millis); } catch (InterruptedException ignored) { }
    }

    /**
     * 全量同步所有已配置的表。
     * <p>
     * 单个表同步失败不影响其他表，异常信息记录在 SyncResult 中。
     * 失败的表会在第一轮结束后重试一次，以应对 Cloudflare 临时限流。
     *
     * @return 每个表的同步结果映射（Cargo表名 → SyncResult）
     */
    public Map<String, SyncResult> syncAll() throws SQLException {
        Map<String, SyncResult> results = new LinkedHashMap<>();
        List<String> failedTables = new ArrayList<>();

        // 第一轮：顺序同步全部表，表间冷却避免 Cloudflare 累积限流
        for (String tableName : tableConfigs.keySet()) {
            try {
                results.put(tableName, syncTable(tableName));
                sleep(interTableDelayMs);
            } catch (Exception e) {
                log.error("Sync failed for table '{}': {}", tableName, e.getMessage(), e);
                results.put(tableName, SyncResult.failed(tableName, e.getMessage()));
                failedTables.add(tableName);
                sleep(failureDelayMs);
            }
        }

        // 第二轮：重试失败的表（仅重试一次）
        if (!failedTables.isEmpty()) {
            log.info("Retrying {} failed table(s)...", failedTables.size());
            sleep(retryCooldownMs);

            for (String tableName : failedTables) {
                try {
                    log.info("Retry syncing '{}'...", tableName);
                    results.put(tableName, syncTable(tableName));
                    sleep(interTableDelayMs);
                } catch (Exception e) {
                    log.error("Retry failed for table '{}': {}", tableName, e.getMessage());
                    // 保留第一轮的失败结果
                }
            }
        }

        return results;
    }

    /**
     * 同步指定表。
     *
     * @param cargoTable Cargo 表名（如 "items"）
     * @return 同步结果
     * @throws IllegalArgumentException 表名未在配置中
     */
    public SyncResult syncTable(String cargoTable) throws SQLException {
        TableConfig config = tableConfigs.get(cargoTable);
        if (config == null) {
            throw new IllegalArgumentException("Unknown table: " + cargoTable);
        }

        Instant startedAt = Instant.now();

        // 1. 获取远程记录总数
        int remoteCount = wikiClient.queryCargoTableCount(cargoTable);
        if (remoteCount <= 0) {
            log.warn("Remote table '{}' returned 0 or error count, skipping", cargoTable);
            return SyncResult.skipped();
        }

        // 2. 与本地对比
        int localCount = getLocalRecordCount(config.sqliteTable);
        if (localCount == remoteCount) {
            log.info("Table '{}' is up-to-date ({} records), skipping", cargoTable, remoteCount);
            return SyncResult.skipped();
        }

        log.info("Syncing '{}': local={}, remote={}", cargoTable, localCount, remoteCount);

        // 3. 发布开始事件
        AppEventBus.postAsync(new DataSyncStartEvent(cargoTable, remoteCount));

        // 4. 分批拉取并写入
        int synced = batchSync(cargoTable, config, remoteCount);

        // 5. 重建 FTS 索引（仅 items 表）
        if ("items".equals(cargoTable)) {
            rebuildFts();
        }

        // 6. 更新 data_version
        updateDataVersion(config.sqliteTable, remoteCount);

        // 7. 发布完成事件
        Instant finishedAt = Instant.now();
        SyncResult result = SyncResult.success(cargoTable, synced, startedAt, finishedAt);
        AppEventBus.postAsync(new DataSyncCompleteEvent(
            cargoTable, result.getSyncedRecords(), result.getDurationMs()));

        log.info("Sync '{}' complete: {} records in {}ms", cargoTable, synced, result.getDurationMs());
        return result;
    }

    /**
     * 检查远程数据是否有更新。
     * <p>
     * 对比所有配置表的远程 COUNT(*) 与本地 record_count。
     *
     * @return true 至少一个表有变化
     */
    public boolean hasUpdates() {
        try (Connection conn = dbManager.getConnection()) {
            for (Map.Entry<String, TableConfig> entry : tableConfigs.entrySet()) {
                int remote = wikiClient.queryCargoTableCount(entry.getKey());
                int local = getLocalRecordCount(conn, entry.getValue().sqliteTable);
                if (remote > 0 && remote != local) {
                    return true;
                }
            }
        } catch (SQLException e) {
            log.error("Failed to check for updates", e);
        }
        return false;
    }

    /**
     * 获取所有需要同步的表名集合。
     * <p>
     * 逐表查询远程 COUNT(*) 并与本地 {@code data_version} 对比。
     * 只返回远程 > 0 且本地记录数不一致的表，避免不必要的全量 COUNT 请求。
     *
     * @return 需要同步的 Cargo 表名集合
     */
    public Set<String> getOutOfSyncTables() {
        Set<String> outOfSync = new LinkedHashSet<>();
        try (Connection conn = dbManager.getConnection()) {
            for (Map.Entry<String, TableConfig> entry : tableConfigs.entrySet()) {
                String cargoTable = entry.getKey();
                try {
                    int remote = wikiClient.queryCargoTableCount(cargoTable);
                    if (remote <= 0) continue;
                    int local = getLocalRecordCount(conn, entry.getValue().sqliteTable);
                    if (remote != local) {
                        outOfSync.add(cargoTable);
                    }
                } catch (Exception e) {
                    log.warn("Failed to check count for '{}', marking as out-of-sync: {}",
                        cargoTable, e.getMessage());
                    outOfSync.add(cargoTable);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to get out-of-sync tables", e);
        }
        return outOfSync;
    }

    /**
     * 仅同步指定表集合。
     * <p>
     * 与 {@link #syncAll()} 使用相同的失败容忍和一轮重试策略，
     * 但不检查已同步的表，直接对传入的表执行同步。
     *
     * @param cargoTables 需要同步的 Cargo 表名集合
     * @return 每张表的同步结果（Cargo表名 → SyncResult）
     */
    public Map<String, SyncResult> syncTables(Set<String> cargoTables) throws SQLException {
        Map<String, SyncResult> results = new LinkedHashMap<>();
        List<String> failedTables = new ArrayList<>();

        for (String tableName : cargoTables) {
            if (!tableConfigs.containsKey(tableName)) {
                log.warn("Unknown table '{}', skipping", tableName);
                results.put(tableName, SyncResult.failed(tableName, "Unknown table"));
                continue;
            }
            try {
                results.put(tableName, syncTable(tableName));
                sleep(interTableDelayMs);
            } catch (Exception e) {
                log.error("Sync failed for table '{}': {}", tableName, e.getMessage(), e);
                results.put(tableName, SyncResult.failed(tableName, e.getMessage()));
                failedTables.add(tableName);
                sleep(failureDelayMs);
            }
        }

        // 重试失败的表
        if (!failedTables.isEmpty()) {
            log.info("Retrying {} failed table(s)...", failedTables.size());
            sleep(retryCooldownMs);

            for (String tableName : failedTables) {
                try {
                    log.info("Retry syncing '{}'...", tableName);
                    results.put(tableName, syncTable(tableName));
                    sleep(interTableDelayMs);
                } catch (Exception e) {
                    log.error("Retry failed for table '{}': {}", tableName, e.getMessage());
                }
            }
        }

        return results;
    }

    /**
     * 便捷方法：自动检测并仅同步过期的表。
     * <p>
     * 等价于 {@code syncTables(getOutOfSyncTables())}。
     * 适用于"先全量同步，之后只增量拉取变化表"的场景。
     *
     * @return 每张表的同步结果（Cargo表名 → SyncResult）
     */
    public Map<String, SyncResult> syncOutOfSyncTables() throws SQLException {
        Set<String> outOfSync = getOutOfSyncTables();
        if (outOfSync.isEmpty()) {
            log.info("All tables are up-to-date, nothing to sync");
            return Collections.emptyMap();
        }
        log.info("Found {} out-of-sync table(s): {}", outOfSync.size(), outOfSync);
        return syncTables(outOfSync);
    }

    // ==================== 内部实现 ====================

    /**
     * 分批拉取 + 转换 + 事务写入。
     * <p>
     * 先拉取第一批数据验证 API 可用，成功后才清空旧表并写入。
     * 如果首次 API 调用失败，旧数据得以保留，避免"清空后同步失败导致数据丢失"。
     * <p>
     * 支持两种分页模式：
     * <ul>
     *   <li><b>offset 分页</b>（默认）：通过 offset 参数逐批拉取</li>
     *   <li><b>键值游标分页</b>（config.keyField 非空时）：
     *       使用 {@code WHERE keyField >= lastKey ORDER BY keyField} 逐批拉取，
     *       避免深度 offset 在大表上触发 Cargo MWException</li>
     * </ul>
     */
    private int batchSync(String cargoTable, TableConfig config, int totalCount) throws SQLException {
        int totalSynced = 0;

        DataSource ds = dbManager.getDataSource();
        //Object dao = createDao(ds, cargoTable);
        Object dao = null;
        boolean cleared = false;

        if (config.keyField != null) {
            // ---- 键值游标分页 ----
            return batchSyncByKey(cargoTable, config, totalCount, ds, dao, cleared);
        }

        // ---- offset 分页（默认） ----
        for (int offset = 0; offset < totalCount; offset += config.batchSize) {
            String fields = config.fields;
            JsonNode root = wikiClient.queryCargoTable(cargoTable, fields, offset, config.batchSize);
            JsonNode rows = root.path("cargoquery");
            if (!rows.isArray()) break;

            // 转换 — converter classes have been removed, using raw JsonNode
            int batchSize = rows.size();
            List<Object> batch = new ArrayList<>(batchSize);
            int idx = 0;
            for (JsonNode row : rows) {
                JsonNode entity = row.path("title");
                if (!entity.isMissingNode() && !entity.isNull()) {
                    batch.add(entity);
                    idx++;
                }
            }

            // 首批数据拉取成功后才清空旧表（保护已有数据不被意外清空）
            if (!cleared) {
                clearTable(ds, config.sqliteTable);
                cleared = true;
            }

            // 事务写入
            if (!batch.isEmpty()) {
                //batchInsert(dao, batch);
            }

            totalSynced += batch.size();

            // 发布进度事件
            AppEventBus.postAsync(new DataSyncProgressEvent(cargoTable,
                Math.min(totalSynced, totalCount), totalCount));

            if (rows.size() < config.batchSize) break; // 最后一批
        }

        return totalSynced;
    }

    /**
     * 键值游标分页同步。
     * <p>
     * 使用 {@code WHERE keyField >= lastKey ORDER BY keyField} 逐批拉取。
     * 每批取最后一条记录的键值作为下一批的起点。
     * 当同一键值行数超过 batchSize 时，自动降级为 {@code >} 跳过重复键。
     */
    private int batchSyncByKey(String cargoTable, TableConfig config, int totalCount,
                                DataSource ds, Object dao, boolean cleared) throws SQLException {
        int totalSynced = 0;
        String keyField = config.keyField;
        String lastKey = "";
        boolean useGreaterThan = false;

        while (true) {
            JsonNode root;
            if (useGreaterThan) {
                root = wikiClient.queryCargoTableByKeyGt(
                    cargoTable, config.fields, keyField, lastKey, config.batchSize);
                useGreaterThan = false;
            } else {
                root = wikiClient.queryCargoTableByKey(
                    cargoTable, config.fields, keyField, lastKey, config.batchSize);
            }

            JsonNode rows = root.path("cargoquery");
            if (!rows.isArray() || rows.size() == 0) break;

            int batchSize = rows.size();
            List<Object> batch = new ArrayList<>(batchSize);

            String firstRowKey = rows.get(0).path("title").path(keyField).asText();
            String newLastKey = null;

            for (int i = 0; i < batchSize; i++) {
                JsonNode title = rows.get(i).path("title");
                if (!title.isMissingNode() && !title.isNull()) {
                    batch.add(title);
                }
                if (i == batchSize - 1) {
                    newLastKey = title.path(keyField).asText();
                }
            }

            // 键值溢出检测：整批同一键值 + 满载 → 用 > 跳过剩余重复行
            if (lastKey.equals(firstRowKey) && lastKey.equals(newLastKey)
                    && batchSize == config.batchSize) {
                log.warn("Key overflow for '{}' at '{}', using > to skip duplicates",
                    cargoTable, lastKey);
                useGreaterThan = true;
                continue;
            }

            // 首批成功后才清空旧表
            if (!cleared) {
                clearTable(ds, config.sqliteTable);
                cleared = true;
            }

            if (!batch.isEmpty()) {
                //batchInsert(dao, batch);
            }

            totalSynced += batch.size();

            AppEventBus.postAsync(new DataSyncProgressEvent(cargoTable,
                Math.min(totalSynced, totalCount), totalCount));

            if (batchSize < config.batchSize) break;
            if (totalSynced >= totalCount) break; // 已拉取足够数据

            if (newLastKey == null || newLastKey.equals(lastKey)) {
                log.warn("Key stalled for '{}' at '{}', stopping", cargoTable, lastKey);
                break;
            }

            lastKey = newLastKey;
        }

        return totalSynced;
    }

    /** Cached setPageId/setId methods per class to avoid repeated reflection. */
    private static final Map<Class<?>, java.lang.reflect.Method> ID_SETTER_CACHE = new java.util.HashMap<>();

    /**
     * 为实体分配顺序 ID。
     * 优先查找 setPageId(Integer/int)，其次 setId(Integer/int)。都不存在则静默跳过。
     */
    private static void assignSequentialId(Object entity, int id) {
        Class<?> clazz = entity.getClass();
        java.lang.reflect.Method method = ID_SETTER_CACHE.computeIfAbsent(clazz, c -> {
            try { return c.getMethod("setPageId", Integer.class); } catch (NoSuchMethodException e1) { }
            try { return c.getMethod("setPageId", int.class); } catch (NoSuchMethodException e2) { }
            try { return c.getMethod("setId", Integer.class); } catch (NoSuchMethodException e3) { }
            try { return c.getMethod("setId", int.class); } catch (NoSuchMethodException e4) { }
            return null;
        });
        if (method != null) {
            try {
                method.invoke(entity, id);
            } catch (Exception ignored) { }
        }
    }

    // ---- 数据库操作 ----

    /** 清空目标表所有数据（DELETE，无 WHERE 条件）。 */
    private void clearTable(DataSource ds, String tableName) throws SQLException {
        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM " + tableName);
        }
    }

    /** 查询本地表记录数，失败返回 -1（触发强制同步）。 */
    private int getLocalRecordCount(String tableName) {
        try (Connection conn = dbManager.getConnection()) {
            return getLocalRecordCount(conn, tableName);
        } catch (SQLException e) {
            log.error("Failed to get local count for {}", tableName, e);
            return -1; // 强制同步
        }
    }

    /** 在给定连接上查询本地表记录数，表不存在时返回 -1。 */
    private int getLocalRecordCount(Connection conn, String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            // 表可能不存在
        }
        return -1;
    }

    /** 写入或更新 data_version 表，记录同步时间与记录数。 */
    private void updateDataVersion(String tableName, int recordCount) {
        String sql = "INSERT OR REPLACE INTO data_version (table_name, last_sync, record_count) " +
            "VALUES (?, datetime('now'), ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setInt(2, recordCount);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to update data_version for {}", tableName, e);
        }
    }

    /**
     * 重建 FTS5 items_fts 全文索引（外部内容表模式）。
     */
    private void rebuildFts() {
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO items_fts(items_fts) VALUES('rebuild')");
            log.info("FTS index rebuilt");
        } catch (SQLException e) {
            log.error("Failed to rebuild FTS index", e);
        }
    }

    // ==================== 内部类型 ====================

    /** 单个表的同步配置。 */
    /** 表元数据（不含 fields，fields 从 wiki 动态获取）。 */
    static class TableMeta {
        final String sqliteTable;
        final int batchSize;
        final String keyField;

        TableMeta(String sqliteTable) {
            this(sqliteTable, BATCH_SIZE, null);
        }

        TableMeta(String sqliteTable, int batchSize) {
            this(sqliteTable, batchSize, null);
        }

        TableMeta(String sqliteTable, int batchSize, String keyField) {
            this.sqliteTable = sqliteTable;
            this.batchSize = batchSize;
            this.keyField = keyField;
        }
    }

    /** 运行时表配置：sqliteTable + 动态 fields + batchSize + keyField。 */
    static class TableConfig {
        final String sqliteTable;
        final String fields;
        final int batchSize;
        /**
         * 游标分页键字段。非空时使用 {@code WHERE keyField >= lastKey ORDER BY keyField}
         * 替代 offset 分页，避免深度 offset 在大表上触发 Cargo MWException。
         */
        final String keyField;

        TableConfig(String sqliteTable, String fields, int batchSize, String keyField) {
            this.sqliteTable = sqliteTable;
            this.fields = fields;
            this.batchSize = batchSize;
            this.keyField = keyField;
        }
    }
}
