package com.poe.provider.sync;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.dao.*;
import com.poe.cache.manager.DatabaseManager;
import com.poe.cache.model.*;
import com.poe.core.event.AppEventBus;
import com.poe.core.event.DataSyncCompleteEvent;
import com.poe.core.event.DataSyncProgressEvent;
import com.poe.core.event.DataSyncStartEvent;
import com.poe.provider.WikiApiClient;
import com.poe.provider.converter.*;
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
        Object dao = createDao(ds, cargoTable);
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

            // 转换
            int batchSize = rows.size();
            List<Object> batch = new ArrayList<>(batchSize);
            DataConverter<Object> converter = getConverter(cargoTable);
            int idx = 0;
            for (JsonNode row : rows) {
                Object entity = converter.convert(row.path("title"));
                if (entity != null) {
                    assignSequentialId(entity, offset + idx + 1);
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
                batchInsert(dao, batch);
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
            DataConverter<Object> converter = getConverter(cargoTable);

            String firstRowKey = rows.get(0).path("title").path(keyField).asText();
            String newLastKey = null;

            for (int i = 0; i < batchSize; i++) {
                JsonNode title = rows.get(i).path("title");
                Object entity = converter.convert(title);
                if (entity != null) {
                    assignSequentialId(entity, totalSynced + batch.size() + 1);
                    batch.add(entity);
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
                batchInsert(dao, batch);
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

    /** 根据 DAO 类型分发批量插入调用。 */
    @SuppressWarnings("unchecked")
    private void batchInsert(Object dao, List<Object> entities) throws SQLException {
        if (dao instanceof ItemDao) {
            ((ItemDao) dao).batchInsert((List<Item>) (List<?>) entities);
        } else if (dao instanceof SkillGemDao) {
            ((SkillGemDao) dao).batchInsert((List<SkillGem>) (List<?>) entities);
        } else if (dao instanceof PassiveSkillDao) {
            ((PassiveSkillDao) dao).batchInsert((List<PassiveSkill>) (List<?>) entities);
        } else if (dao instanceof ModDao) {
            ((ModDao) dao).batchInsert((List<Mod>) (List<?>) entities);
        } else if (dao instanceof WeaponDao) {
            ((WeaponDao) dao).batchInsert((List<Weapon>) (List<?>) entities);
        } else if (dao instanceof ArmourDao) {
            ((ArmourDao) dao).batchInsert((List<Armour>) (List<?>) entities);
        } else if (dao instanceof ShieldDao) {
            ((ShieldDao) dao).batchInsert((List<Shield>) (List<?>) entities);
        } else if (dao instanceof AmuletDao) {
            ((AmuletDao) dao).batchInsert((List<Amulet>) (List<?>) entities);
        } else if (dao instanceof FlaskDao) {
            ((FlaskDao) dao).batchInsert((List<Flask>) (List<?>) entities);
        } else if (dao instanceof JewelDao) {
            ((JewelDao) dao).batchInsert((List<Jewel>) (List<?>) entities);
        } else if (dao instanceof StackableDao) {
            ((StackableDao) dao).batchInsert((List<Stackable>) (List<?>) entities);
        } else if (dao instanceof MapDao) {
            ((MapDao) dao).batchInsert((List<GameMap>) (List<?>) entities);
        } else if (dao instanceof MapFragmentDao) {
            ((MapFragmentDao) dao).batchInsert((List<MapFragment>) (List<?>) entities);
        } else if (dao instanceof MapSeriesDao) {
            ((MapSeriesDao) dao).batchInsert((List<MapSeries>) (List<?>) entities);
        } else if (dao instanceof DivinationCardDao) {
            ((DivinationCardDao) dao).batchInsert((List<DivinationCard>) (List<?>) entities);
        } else if (dao instanceof ModStatDao) {
            ((ModStatDao) dao).batchInsert((List<ModStat>) (List<?>) entities);
        } else if (dao instanceof ModSpawnWeightDao) {
            ((ModSpawnWeightDao) dao).batchInsert((List<ModSpawnWeight>) (List<?>) entities);
        } else if (dao instanceof ModGenerationWeightDao) {
            ((ModGenerationWeightDao) dao).batchInsert((List<ModGenerationWeight>) (List<?>) entities);
        } else if (dao instanceof ModSellPriceDao) {
            ((ModSellPriceDao) dao).batchInsert((List<ModSellPrice>) (List<?>) entities);
        } else if (dao instanceof ItemModDao) {
            ((ItemModDao) dao).batchInsert((List<ItemMod>) (List<?>) entities);
        } else if (dao instanceof ItemStatDao) {
            ((ItemStatDao) dao).batchInsert((List<ItemStat>) (List<?>) entities);
        } else if (dao instanceof ItemBuffDao) {
            ((ItemBuffDao) dao).batchInsert((List<ItemBuff>) (List<?>) entities);
        } else if (dao instanceof CraftingBenchOptionDao) {
            ((CraftingBenchOptionDao) dao).batchInsert((List<CraftingBenchOption>) (List<?>) entities);
        } else if (dao instanceof CraftingBenchOptionCostDao) {
            ((CraftingBenchOptionCostDao) dao).batchInsert((List<CraftingBenchOptionCost>) (List<?>) entities);
        } else if (dao instanceof EssenceDao) {
            ((EssenceDao) dao).batchInsert((List<Essence>) (List<?>) entities);
        } else if (dao instanceof FossilDao) {
            ((FossilDao) dao).batchInsert((List<Fossil>) (List<?>) entities);
        } else if (dao instanceof FossilWeightDao) {
            ((FossilWeightDao) dao).batchInsert((List<FossilWeight>) (List<?>) entities);
        } else if (dao instanceof VendorRewardDao) {
            ((VendorRewardDao) dao).batchInsert((List<VendorReward>) (List<?>) entities);
        } else if (dao instanceof ItemSellPriceDao) {
            ((ItemSellPriceDao) dao).batchInsert((List<ItemSellPrice>) (List<?>) entities);
        } else if (dao instanceof ItemPurchaseCostDao) {
            ((ItemPurchaseCostDao) dao).batchInsert((List<ItemPurchaseCost>) (List<?>) entities);
        } else if (dao instanceof SkillDao) {
            ((SkillDao) dao).batchInsert((List<Skill>) (List<?>) entities);
        } else if (dao instanceof SkillLevelDao) {
            ((SkillLevelDao) dao).batchInsert((List<SkillLevel>) (List<?>) entities);
        } else if (dao instanceof SkillStatsPerLevelDao) {
            ((SkillStatsPerLevelDao) dao).batchInsert((List<SkillStatsPerLevel>) (List<?>) entities);
        } else if (dao instanceof SkillQualityDao) {
            ((SkillQualityDao) dao).batchInsert((List<SkillQuality>) (List<?>) entities);
        } else if (dao instanceof SkillQualityStatsDao) {
            ((SkillQualityStatsDao) dao).batchInsert((List<SkillQualityStats>) (List<?>) entities);
        } else if (dao instanceof GemLevelDao) {
            ((GemLevelDao) dao).batchInsert((List<GemLevel>) (List<?>) entities);
        } else if (dao instanceof PassiveSkillConnectionDao) {
            ((PassiveSkillConnectionDao) dao).batchInsert((List<PassiveSkillConnection>) (List<?>) entities);
        } else if (dao instanceof MasteryEffectDao) {
            ((MasteryEffectDao) dao).batchInsert((List<MasteryEffect>) (List<?>) entities);
        } else if (dao instanceof MasteryGroupDao) {
            ((MasteryGroupDao) dao).batchInsert((List<MasteryGroup>) (List<?>) entities);
        } else if (dao instanceof CharacterClassDao) {
            ((CharacterClassDao) dao).batchInsert((List<CharacterClass>) (List<?>) entities);
        } else if (dao instanceof AscendancyClassDao) {
            ((AscendancyClassDao) dao).batchInsert((List<AscendancyClass>) (List<?>) entities);
        } else if (dao instanceof MonsterDao) {
            ((MonsterDao) dao).batchInsert((List<Monster>) (List<?>) entities);
        } else if (dao instanceof MonsterTypeDao) {
            ((MonsterTypeDao) dao).batchInsert((List<MonsterType>) (List<?>) entities);
        } else if (dao instanceof MonsterBaseStatDao) {
            ((MonsterBaseStatDao) dao).batchInsert((List<MonsterBaseStat>) (List<?>) entities);
        } else if (dao instanceof MonsterLifeScalingDao) {
            ((MonsterLifeScalingDao) dao).batchInsert((List<MonsterLifeScaling>) (List<?>) entities);
        } else if (dao instanceof MonsterMapMultiplierDao) {
            ((MonsterMapMultiplierDao) dao).batchInsert((List<MonsterMapMultiplier>) (List<?>) entities);
        } else if (dao instanceof MonsterResistanceDao) {
            ((MonsterResistanceDao) dao).batchInsert((List<MonsterResistance>) (List<?>) entities);
        } else if (dao instanceof AreaDao) {
            ((AreaDao) dao).batchInsert((List<Area>) (List<?>) entities);
        } else if (dao instanceof AtlasNodeDao) {
            ((AtlasNodeDao) dao).batchInsert((List<AtlasNode>) (List<?>) entities);
        } else if (dao instanceof DelveLevelScalingDao) {
            ((DelveLevelScalingDao) dao).batchInsert((List<DelveLevelScaling>) (List<?>) entities);
        } else if (dao instanceof DelveResourcesPerLevelDao) {
            ((DelveResourcesPerLevelDao) dao).batchInsert((List<DelveResourcesPerLevel>) (List<?>) entities);
        } else if (dao instanceof DelveUpgradesDao) {
            ((DelveUpgradesDao) dao).batchInsert((List<DelveUpgrades>) (List<?>) entities);
        } else if (dao instanceof DelveUpgradeStatsDao) {
            ((DelveUpgradeStatsDao) dao).batchInsert((List<DelveUpgradeStats>) (List<?>) entities);
        } else if (dao instanceof HeistAreasDao) {
            ((HeistAreasDao) dao).batchInsert((List<HeistAreas>) (List<?>) entities);
        } else if (dao instanceof HeistJobsDao) {
            ((HeistJobsDao) dao).batchInsert((List<HeistJobs>) (List<?>) entities);
        } else if (dao instanceof HeistNpcsDao) {
            ((HeistNpcsDao) dao).batchInsert((List<HeistNpcs>) (List<?>) entities);
        } else if (dao instanceof HeistNpcSkillsDao) {
            ((HeistNpcSkillsDao) dao).batchInsert((List<HeistNpcSkills>) (List<?>) entities);
        } else if (dao instanceof HeistNpcStatsDao) {
            ((HeistNpcStatsDao) dao).batchInsert((List<HeistNpcStats>) (List<?>) entities);
        } else if (dao instanceof HeistEquipmentDao) {
            ((HeistEquipmentDao) dao).batchInsert((List<HeistEquipment>) (List<?>) entities);
        } else if (dao instanceof BlightCraftingRecipesDao) {
            ((BlightCraftingRecipesDao) dao).batchInsert((List<BlightCraftingRecipes>) (List<?>) entities);
        } else if (dao instanceof BlightCraftingRecipesItemsDao) {
            ((BlightCraftingRecipesItemsDao) dao).batchInsert((List<BlightCraftingRecipesItems>) (List<?>) entities);
        } else if (dao instanceof BlightItemsDao) {
            ((BlightItemsDao) dao).batchInsert((List<BlightItems>) (List<?>) entities);
        } else if (dao instanceof BlightTowersDao) {
            ((BlightTowersDao) dao).batchInsert((List<BlightTowers>) (List<?>) entities);
        } else if (dao instanceof HarvestCraftingOptionsDao) {
            ((HarvestCraftingOptionsDao) dao).batchInsert((List<HarvestCraftingOptions>) (List<?>) entities);
        } else if (dao instanceof HarvestPlantBoostersDao) {
            ((HarvestPlantBoostersDao) dao).batchInsert((List<HarvestPlantBoosters>) (List<?>) entities);
        } else if (dao instanceof HarvestSeedsDao) {
            ((HarvestSeedsDao) dao).batchInsert((List<HarvestSeeds>) (List<?>) entities);
        } else if (dao instanceof SynthesisAreasDao) {
            ((SynthesisAreasDao) dao).batchInsert((List<SynthesisAreas>) (List<?>) entities);
        } else if (dao instanceof SynthesisCorruptedModsDao) {
            ((SynthesisCorruptedModsDao) dao).batchInsert((List<SynthesisCorruptedMods>) (List<?>) entities);
        } else if (dao instanceof SynthesisGlobalModsDao) {
            ((SynthesisGlobalModsDao) dao).batchInsert((List<SynthesisGlobalMods>) (List<?>) entities);
        } else if (dao instanceof SynthesisModsDao) {
            ((SynthesisModsDao) dao).batchInsert((List<SynthesisMods>) (List<?>) entities);
        } else if (dao instanceof BestiaryRecipesDao) {
            ((BestiaryRecipesDao) dao).batchInsert((List<BestiaryRecipes>) (List<?>) entities);
        } else if (dao instanceof BestiaryRecipeComponentsDao) {
            ((BestiaryRecipeComponentsDao) dao).batchInsert((List<BestiaryRecipeComponents>) (List<?>) entities);
        } else if (dao instanceof IncursionRoomsDao) {
            ((IncursionRoomsDao) dao).batchInsert((List<IncursionRooms>) (List<?>) entities);
        } else if (dao instanceof PantheonDao) {
            ((PantheonDao) dao).batchInsert((List<Pantheon>) (List<?>) entities);
        } else if (dao instanceof PantheonSoulsDao) {
            ((PantheonSoulsDao) dao).batchInsert((List<PantheonSouls>) (List<?>) entities);
        } else if (dao instanceof PantheonStatsDao) {
            ((PantheonStatsDao) dao).batchInsert((List<PantheonStats>) (List<?>) entities);
        } else if (dao instanceof VersionDao) {
            ((VersionDao) dao).batchInsert((List<Version>) (List<?>) entities);
        } else if (dao instanceof LegacyVariantDao) {
            ((LegacyVariantDao) dao).batchInsert((List<LegacyVariant>) (List<?>) entities);
        } else if (dao instanceof ProphecyDao) {
            ((ProphecyDao) dao).batchInsert((List<Prophecy>) (List<?>) entities);
        } else if (dao instanceof QuestRewardDao) {
            ((QuestRewardDao) dao).batchInsert((List<QuestReward>) (List<?>) entities);
        } else if (dao instanceof SpawnWeightDao) {
            ((SpawnWeightDao) dao).batchInsert((List<SpawnWeight>) (List<?>) entities);
        } else if (dao instanceof GenericStatDao) {
            ((GenericStatDao) dao).batchInsert((List<GenericStat>) (List<?>) entities);
        } else if (dao instanceof TattooDao) {
            ((TattooDao) dao).batchInsert((List<Tattoo>) (List<?>) entities);
        } else if (dao instanceof TinctureDao) {
            ((TinctureDao) dao).batchInsert((List<Tincture>) (List<?>) entities);
        } else if (dao instanceof SentinelDao) {
            ((SentinelDao) dao).batchInsert((List<Sentinel>) (List<?>) entities);
        } else if (dao instanceof IdolDao) {
            ((IdolDao) dao).batchInsert((List<Idol>) (List<?>) entities);
        } else if (dao instanceof GraftDao) {
            ((GraftDao) dao).batchInsert((List<Graft>) (List<?>) entities);
        } else if (dao instanceof CorpseItemDao) {
            ((CorpseItemDao) dao).batchInsert((List<CorpseItem>) (List<?>) entities);
        } else if (dao instanceof CosmeticItemDao) {
            ((CosmeticItemDao) dao).batchInsert((List<CosmeticItem>) (List<?>) entities);
        } else if (dao instanceof HideoutDoodadDao) {
            ((HideoutDoodadDao) dao).batchInsert((List<HideoutDoodad>) (List<?>) entities);
        } else if (dao instanceof GuideDao) {
            ((GuideDao) dao).batchInsert((List<Guide>) (List<?>) entities);
        } else {
            throw new IllegalArgumentException("Unknown DAO: " + dao.getClass());
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

    // ---- DAO / Converter 工厂 ----

    /** 根据 Cargo 表名返回对应的 Converter 实例。 */
    @SuppressWarnings("unchecked")
    private static DataConverter<Object> getConverter(String cargoTable) {
        if ("items".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new ItemConverter();
        } else if ("skill_gems".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new SkillGemConverter();
        } else if ("passive_skills".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new PassiveSkillConverter();
        } else if ("mods".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new ModConverter();
        } else if ("weapons".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new WeaponConverter();
        } else if ("armours".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new ArmourConverter();
        } else if ("shields".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new ShieldConverter();
        } else if ("amulets".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new AmuletConverter();
        } else if ("flasks".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new FlaskConverter();
        } else if ("jewels".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new JewelConverter();
        } else if ("stackables".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new StackableConverter();
        } else if ("maps".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new MapConverter();
        } else if ("map_fragments".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new MapFragmentConverter();
        } else if ("map_series".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new MapSeriesConverter();
        } else if ("divination_cards".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new DivinationCardConverter();
        } else if ("mod_stats".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new ModStatConverter();
        } else if ("mod_spawn_weights".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new ModSpawnWeightConverter();
        } else if ("mod_generation_weights".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new ModGenerationWeightConverter();
        } else if ("mod_sell_prices".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new ModSellPriceConverter();
        } else if ("item_mods".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new ItemModConverter();
        } else if ("item_stats".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new ItemStatConverter();
        } else if ("item_buffs".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new ItemBuffConverter();
        } else if ("crafting_bench_options".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new CraftingBenchOptionConverter();
        } else if ("crafting_bench_options_costs".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new CraftingBenchOptionCostConverter();
        } else if ("essences".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new EssenceConverter();
        } else if ("fossils".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new FossilConverter();
        } else if ("fossil_weights".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new FossilWeightConverter();
        } else if ("vendor_rewards".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new VendorRewardConverter();
        } else if ("item_sell_prices".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new ItemSellPriceConverter();
        } else if ("item_purchase_costs".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new ItemPurchaseCostConverter();
        } else if ("skill".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new SkillConverter();
        } else if ("skill_levels".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new SkillLevelConverter();
        } else if ("skill_stats_per_level".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new SkillStatsPerLevelConverter();
        } else if ("skill_quality".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new SkillQualityConverter();
        } else if ("skill_quality_stats".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new SkillQualityStatsConverter();
        } else if ("gem_levels".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new GemLevelConverter();
        } else if ("passive_skill_connections".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new PassiveSkillConnectionConverter();
        } else if ("mastery_effects".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new MasteryEffectConverter();
        } else if ("mastery_groups".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new MasteryGroupConverter();
        } else if ("character_classes".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new CharacterClassConverter();
        } else if ("ascendancy_classes".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new AscendancyClassConverter();
        } else if ("monsters".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new MonsterConverter();
        } else if ("monster_types".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new MonsterTypeConverter();
        } else if ("monster_base_stats".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new MonsterBaseStatConverter();
        } else if ("monster_life_scaling".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new MonsterLifeScalingConverter();
        } else if ("monster_map_multipliers".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new MonsterMapMultiplierConverter();
        } else if ("monster_resistances".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new MonsterResistanceConverter();
        } else if ("areas".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new AreaConverter();
        } else if ("atlas_nodes".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new AtlasNodeConverter();
        } else if ("delve_level_scaling".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new DelveLevelScalingConverter();
        } else if ("delve_resources_per_level".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new DelveResourcesPerLevelConverter();
        } else if ("delve_upgrades".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new DelveUpgradesConverter();
        } else if ("delve_upgrade_stats".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new DelveUpgradeStatsConverter();
        } else if ("heist_areas".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new HeistAreasConverter();
        } else if ("heist_jobs".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new HeistJobsConverter();
        } else if ("heist_npcs".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new HeistNpcsConverter();
        } else if ("heist_npc_skills".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new HeistNpcSkillsConverter();
        } else if ("heist_npc_stats".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new HeistNpcStatsConverter();
        } else if ("heist_equipment".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new HeistEquipmentConverter();
        } else if ("blight_crafting_recipes".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new BlightCraftingRecipesConverter();
        } else if ("blight_crafting_recipes_items".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new BlightCraftingRecipesItemsConverter();
        } else if ("blight_items".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new BlightItemsConverter();
        } else if ("blight_towers".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new BlightTowersConverter();
        } else if ("harvest_crafting_options".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new HarvestCraftingOptionsConverter();
        } else if ("harvest_plant_boosters".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new HarvestPlantBoostersConverter();
        } else if ("harvest_seeds".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new HarvestSeedsConverter();
        } else if ("synthesis_areas".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new SynthesisAreasConverter();
        } else if ("synthesis_corrupted_mods".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new SynthesisCorruptedModsConverter();
        } else if ("synthesis_global_mods".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new SynthesisGlobalModsConverter();
        } else if ("synthesis_mods".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new SynthesisModsConverter();
        } else if ("bestiary_recipes".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new BestiaryRecipesConverter();
        } else if ("bestiary_recipe_components".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new BestiaryRecipeComponentsConverter();
        } else if ("incursion_rooms".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new IncursionRoomsConverter();
        } else if ("pantheon".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new PantheonConverter();
        } else if ("pantheon_souls".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new PantheonSoulsConverter();
        } else if ("pantheon_stats".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new PantheonStatsConverter();
        } else if ("versions".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new VersionConverter();
        } else if ("legacy_variants".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new LegacyVariantConverter();
        } else if ("prophecies".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new ProphecyConverter();
        } else if ("quest_rewards".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new QuestRewardConverter();
        } else if ("spawn_weights".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new SpawnWeightConverter();
        } else if ("generic_stats".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new GenericStatConverter();
        } else if ("tattoos".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new TattooConverter();
        } else if ("tinctures".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new TinctureConverter();
        } else if ("sentinels".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new SentinelConverter();
        } else if ("idols".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new IdolConverter();
        } else if ("grafts".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new GraftConverter();
        } else if ("corpse_items".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new CorpseItemConverter();
        } else if ("cosmetic_items".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new CosmeticItemConverter();
        } else if ("hideout_doodads".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new HideoutDoodadConverter();
        } else if ("guides".equals(cargoTable)) {
            return (DataConverter<Object>) (DataConverter<?>) new GuideConverter();
        }
        throw new IllegalArgumentException("No converter for: " + cargoTable);
    }

    /** 根据 Cargo 表名创建对应的 DAO 实例。 */
    private static Object createDao(DataSource ds, String cargoTable) {
        if ("items".equals(cargoTable)) {
            return new ItemDao(ds);
        } else if ("skill_gems".equals(cargoTable)) {
            return new SkillGemDao(ds);
        } else if ("passive_skills".equals(cargoTable)) {
            return new PassiveSkillDao(ds);
        } else if ("mods".equals(cargoTable)) {
            return new ModDao(ds);
        } else if ("weapons".equals(cargoTable)) {
            return new WeaponDao(ds);
        } else if ("armours".equals(cargoTable)) {
            return new ArmourDao(ds);
        } else if ("shields".equals(cargoTable)) {
            return new ShieldDao(ds);
        } else if ("amulets".equals(cargoTable)) {
            return new AmuletDao(ds);
        } else if ("flasks".equals(cargoTable)) {
            return new FlaskDao(ds);
        } else if ("jewels".equals(cargoTable)) {
            return new JewelDao(ds);
        } else if ("stackables".equals(cargoTable)) {
            return new StackableDao(ds);
        } else if ("maps".equals(cargoTable)) {
            return new MapDao(ds);
        } else if ("map_fragments".equals(cargoTable)) {
            return new MapFragmentDao(ds);
        } else if ("map_series".equals(cargoTable)) {
            return new MapSeriesDao(ds);
        } else if ("divination_cards".equals(cargoTable)) {
            return new DivinationCardDao(ds);
        } else if ("mod_stats".equals(cargoTable)) {
            return new ModStatDao(ds);
        } else if ("mod_spawn_weights".equals(cargoTable)) {
            return new ModSpawnWeightDao(ds);
        } else if ("mod_generation_weights".equals(cargoTable)) {
            return new ModGenerationWeightDao(ds);
        } else if ("mod_sell_prices".equals(cargoTable)) {
            return new ModSellPriceDao(ds);
        } else if ("item_mods".equals(cargoTable)) {
            return new ItemModDao(ds);
        } else if ("item_stats".equals(cargoTable)) {
            return new ItemStatDao(ds);
        } else if ("item_buffs".equals(cargoTable)) {
            return new ItemBuffDao(ds);
        } else if ("crafting_bench_options".equals(cargoTable)) {
            return new CraftingBenchOptionDao(ds);
        } else if ("crafting_bench_options_costs".equals(cargoTable)) {
            return new CraftingBenchOptionCostDao(ds);
        } else if ("essences".equals(cargoTable)) {
            return new EssenceDao(ds);
        } else if ("fossils".equals(cargoTable)) {
            return new FossilDao(ds);
        } else if ("fossil_weights".equals(cargoTable)) {
            return new FossilWeightDao(ds);
        } else if ("vendor_rewards".equals(cargoTable)) {
            return new VendorRewardDao(ds);
        } else if ("item_sell_prices".equals(cargoTable)) {
            return new ItemSellPriceDao(ds);
        } else if ("item_purchase_costs".equals(cargoTable)) {
            return new ItemPurchaseCostDao(ds);
        } else if ("skill".equals(cargoTable)) {
            return new SkillDao(ds);
        } else if ("skill_levels".equals(cargoTable)) {
            return new SkillLevelDao(ds);
        } else if ("skill_stats_per_level".equals(cargoTable)) {
            return new SkillStatsPerLevelDao(ds);
        } else if ("skill_quality".equals(cargoTable)) {
            return new SkillQualityDao(ds);
        } else if ("skill_quality_stats".equals(cargoTable)) {
            return new SkillQualityStatsDao(ds);
        } else if ("gem_levels".equals(cargoTable)) {
            return new GemLevelDao(ds);
        } else if ("passive_skill_connections".equals(cargoTable)) {
            return new PassiveSkillConnectionDao(ds);
        } else if ("mastery_effects".equals(cargoTable)) {
            return new MasteryEffectDao(ds);
        } else if ("mastery_groups".equals(cargoTable)) {
            return new MasteryGroupDao(ds);
        } else if ("character_classes".equals(cargoTable)) {
            return new CharacterClassDao(ds);
        } else if ("ascendancy_classes".equals(cargoTable)) {
            return new AscendancyClassDao(ds);
        } else if ("monsters".equals(cargoTable)) {
            return new MonsterDao(ds);
        } else if ("monster_types".equals(cargoTable)) {
            return new MonsterTypeDao(ds);
        } else if ("monster_base_stats".equals(cargoTable)) {
            return new MonsterBaseStatDao(ds);
        } else if ("monster_life_scaling".equals(cargoTable)) {
            return new MonsterLifeScalingDao(ds);
        } else if ("monster_map_multipliers".equals(cargoTable)) {
            return new MonsterMapMultiplierDao(ds);
        } else if ("monster_resistances".equals(cargoTable)) {
            return new MonsterResistanceDao(ds);
        } else if ("areas".equals(cargoTable)) {
            return new AreaDao(ds);
        } else if ("atlas_nodes".equals(cargoTable)) {
            return new AtlasNodeDao(ds);
        } else if ("delve_level_scaling".equals(cargoTable)) {
            return new DelveLevelScalingDao(ds);
        } else if ("delve_resources_per_level".equals(cargoTable)) {
            return new DelveResourcesPerLevelDao(ds);
        } else if ("delve_upgrades".equals(cargoTable)) {
            return new DelveUpgradesDao(ds);
        } else if ("delve_upgrade_stats".equals(cargoTable)) {
            return new DelveUpgradeStatsDao(ds);
        } else if ("heist_areas".equals(cargoTable)) {
            return new HeistAreasDao(ds);
        } else if ("heist_jobs".equals(cargoTable)) {
            return new HeistJobsDao(ds);
        } else if ("heist_npcs".equals(cargoTable)) {
            return new HeistNpcsDao(ds);
        } else if ("heist_npc_skills".equals(cargoTable)) {
            return new HeistNpcSkillsDao(ds);
        } else if ("heist_npc_stats".equals(cargoTable)) {
            return new HeistNpcStatsDao(ds);
        } else if ("heist_equipment".equals(cargoTable)) {
            return new HeistEquipmentDao(ds);
        } else if ("blight_crafting_recipes".equals(cargoTable)) {
            return new BlightCraftingRecipesDao(ds);
        } else if ("blight_crafting_recipes_items".equals(cargoTable)) {
            return new BlightCraftingRecipesItemsDao(ds);
        } else if ("blight_items".equals(cargoTable)) {
            return new BlightItemsDao(ds);
        } else if ("blight_towers".equals(cargoTable)) {
            return new BlightTowersDao(ds);
        } else if ("harvest_crafting_options".equals(cargoTable)) {
            return new HarvestCraftingOptionsDao(ds);
        } else if ("harvest_plant_boosters".equals(cargoTable)) {
            return new HarvestPlantBoostersDao(ds);
        } else if ("harvest_seeds".equals(cargoTable)) {
            return new HarvestSeedsDao(ds);
        } else if ("synthesis_areas".equals(cargoTable)) {
            return new SynthesisAreasDao(ds);
        } else if ("synthesis_corrupted_mods".equals(cargoTable)) {
            return new SynthesisCorruptedModsDao(ds);
        } else if ("synthesis_global_mods".equals(cargoTable)) {
            return new SynthesisGlobalModsDao(ds);
        } else if ("synthesis_mods".equals(cargoTable)) {
            return new SynthesisModsDao(ds);
        } else if ("bestiary_recipes".equals(cargoTable)) {
            return new BestiaryRecipesDao(ds);
        } else if ("bestiary_recipe_components".equals(cargoTable)) {
            return new BestiaryRecipeComponentsDao(ds);
        } else if ("incursion_rooms".equals(cargoTable)) {
            return new IncursionRoomsDao(ds);
        } else if ("pantheon".equals(cargoTable)) {
            return new PantheonDao(ds);
        } else if ("pantheon_souls".equals(cargoTable)) {
            return new PantheonSoulsDao(ds);
        } else if ("pantheon_stats".equals(cargoTable)) {
            return new PantheonStatsDao(ds);
        } else if ("versions".equals(cargoTable)) {
            return new VersionDao(ds);
        } else if ("legacy_variants".equals(cargoTable)) {
            return new LegacyVariantDao(ds);
        } else if ("prophecies".equals(cargoTable)) {
            return new ProphecyDao(ds);
        } else if ("quest_rewards".equals(cargoTable)) {
            return new QuestRewardDao(ds);
        } else if ("spawn_weights".equals(cargoTable)) {
            return new SpawnWeightDao(ds);
        } else if ("generic_stats".equals(cargoTable)) {
            return new GenericStatDao(ds);
        } else if ("tattoos".equals(cargoTable)) {
            return new TattooDao(ds);
        } else if ("tinctures".equals(cargoTable)) {
            return new TinctureDao(ds);
        } else if ("sentinels".equals(cargoTable)) {
            return new SentinelDao(ds);
        } else if ("idols".equals(cargoTable)) {
            return new IdolDao(ds);
        } else if ("grafts".equals(cargoTable)) {
            return new GraftDao(ds);
        } else if ("corpse_items".equals(cargoTable)) {
            return new CorpseItemDao(ds);
        } else if ("cosmetic_items".equals(cargoTable)) {
            return new CosmeticItemDao(ds);
        } else if ("hideout_doodads".equals(cargoTable)) {
            return new HideoutDoodadDao(ds);
        } else if ("guides".equals(cargoTable)) {
            return new GuideDao(ds);
        }
        throw new IllegalArgumentException("No DAO for: " + cargoTable);
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
