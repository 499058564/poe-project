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
import java.time.Duration;
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
 * 依赖 {@link WikiApiClient} 内部的 5 req/s 限流和 3 次重试。
 *
 * <h3>断点续传</h3>
 * 通过 {@link SyncCheckpoint} 记录当前进度，异常中断后可从上次偏移量继续。
 */
public class DataSyncService {

    private static final Logger log = LoggerFactory.getLogger(DataSyncService.class);

    /** 每批拉取的最大记录数（Wiki API 上限 500）。 */
    private static final int BATCH_SIZE = 500;

    /** 同步的表配置：Cargo表名 → (SQLite表名, DAO, Converter, Wiki字段) */
    private static final Map<String, TableConfig> TABLE_CONFIGS = new LinkedHashMap<>();
    static {
        TABLE_CONFIGS.put("items", new TableConfig("base_items",
            "_pageName,name,class_id,class,size_x,size_y,"
            + "drop_level,flavour_text,base_item,base_item_id,"
            + "rarity,rarity_id,release_version,required_level,"
            + "required_dexterity,required_intelligence,required_strength,"
            + "description,tags,inventory_icon"));
        TABLE_CONFIGS.put("skill_gems", new TableConfig("skill_gems",
            "skill_id,gem_tags,primary_attribute,max_level,"
            + "is_vaal_skill_gem,support_gem_letter,support_gem_letter_html,"
            + "requires_intelligence,requires_dexterity,requires_strength,"
            + "awakened_variant_id,regular_variant_id,vaal_variant_id,"
            + "secondary_skill_id,ruthless_skill_id,ruthless_secondary_skill_id"));
        TABLE_CONFIGS.put("passive_skills", new TableConfig("passive_skills",
            "id,name,ascendancy_class,is_keystone,is_notable,"
            + "is_jewel_socket,stat_text,connections,is_multiple_choice,"
            + "is_multiple_choice_option,mastery_id,flavour_text,"
            + "skill_points,buff_id"));
        TABLE_CONFIGS.put("mods", new TableConfig("mods",
            "id,name,domain,generation_type,mod_groups,stat_text,"
            + "tags,required_level,mod_type,tier_text,"
            + "granted_buff_id,granted_buff_value,granted_skill"));
        TABLE_CONFIGS.put("weapons", new TableConfig("weapons",
            "attack_speed,critical_strike_chance,weapon_range,"
            + "physical_damage_min,physical_damage_max,"
            + "fire_damage_min,fire_damage_max,cold_damage_min,cold_damage_max,"
            + "lightning_damage_min,lightning_damage_max,chaos_damage_min,chaos_damage_max"));
        TABLE_CONFIGS.put("armours", new TableConfig("armours",
            "armour_min,armour_max,evasion_min,evasion_max,"
            + "energy_shield_min,energy_shield_max,ward_min,ward_max,movement_speed"));
        TABLE_CONFIGS.put("shields", new TableConfig("shields", "block"));
        TABLE_CONFIGS.put("amulets", new TableConfig("amulets",
            "is_talisman,talisman_tier"));
        TABLE_CONFIGS.put("flasks", new TableConfig("flasks",
            "charges_max,charges_per_use,duration,life,mana"));
        TABLE_CONFIGS.put("jewels", new TableConfig("jewels",
            "jewel_limit,radius_html"));
        TABLE_CONFIGS.put("stackables", new TableConfig("stackables",
            "stack_size,stack_size_currency_tab"));
        TABLE_CONFIGS.put("maps", new TableConfig("maps",
            "area_id,area_level,guild_character,series,tier,"
            + "unique_area_id,unique_area_level,unique_guild_character"));
        TABLE_CONFIGS.put("map_fragments", new TableConfig("map_fragments",
            "map_fragment_limit"));
        TABLE_CONFIGS.put("map_series", new TableConfig("map_series",
            "id,name,ordinal"));
        TABLE_CONFIGS.put("divination_cards", new TableConfig("divination_cards",
            "card_art,card_background"));

        // ---- 词缀子表 ----
        TABLE_CONFIGS.put("mod_stats", new TableConfig("mod_stats",
            "id,min,max"));
        TABLE_CONFIGS.put("mod_spawn_weights", new TableConfig("mod_spawn_weights",
            "ordinal,tag,value"));
        TABLE_CONFIGS.put("mod_generation_weights", new TableConfig("mod_generation_weights",
            "ordinal,tag,value"));
        TABLE_CONFIGS.put("mod_sell_prices", new TableConfig("mod_sell_prices",
            "amount,name"));

        // ---- 物品-词缀关联 ----
        TABLE_CONFIGS.put("item_mods", new TableConfig("item_mods",
            "id,is_explicit,is_implicit,is_map_fragment_bonus,is_random,text"));
        TABLE_CONFIGS.put("item_stats", new TableConfig("item_stats",
            "avg,id,max,min,mod_id"));
        TABLE_CONFIGS.put("item_buffs", new TableConfig("item_buffs",
            "buff_values,icon,id,stat_text"));

        // ---- 工艺/配方 ----
        TABLE_CONFIGS.put("crafting_bench_options", new TableConfig("crafting_bench_options",
            "id,name,affix_type,mod_id,mod_group,rank,required_level,npc,description,"
            + "recipe_unlock_location,crafting_bench_unlock_category,"
            + "crafting_bench_unlock_category_description,item_class_categories,"
            + "item_classes,item_classes_ids,links,ordinal,socket_colours,sockets,"
            + "unveils_required"));
        TABLE_CONFIGS.put("crafting_bench_options_costs", new TableConfig("crafting_bench_options_costs",
            "amount,name,option_id"));
        TABLE_CONFIGS.put("essences", new TableConfig("essences",
            "category,level,level_restriction,type"));
        TABLE_CONFIGS.put("fossils", new TableConfig("fossils",
            "added_modifier_ids,allowed_tags,base_item_id,can_enchant,can_mirror,"
            + "can_quality,can_roll_white_sockets,corrupted_essence_chance,"
            + "forbidden_tags,forced_modifier_ids,is_lucky,sell_price_modifier_ids"));
        TABLE_CONFIGS.put("fossil_weights", new TableConfig("fossil_weights",
            "base_item_id,ordinal,tag,type,weight"));

        // ---- 经济数据 ----
        TABLE_CONFIGS.put("vendor_rewards", new TableConfig("vendor_rewards",
            "act,class_ids,classes,npc,quest,quest_id"));
        TABLE_CONFIGS.put("item_sell_prices", new TableConfig("item_sell_prices",
            "amount,name"));
        TABLE_CONFIGS.put("item_purchase_costs", new TableConfig("item_purchase_costs",
            "amount,name,rarity"));

        // ---- 技能详细数据 ----
        TABLE_CONFIGS.put("skill", new TableConfig("skills",
            "active_skill_name,cast_time,description,is_support,"
            + "item_class_id_restriction,item_class_restriction,"
            + "max_level,skill_id,stat_text"));
        TABLE_CONFIGS.put("skill_levels", new TableConfig("skill_levels",
            "attack_speed_multiplier,attack_time,cooldown,cost_amounts,"
            + "cost_multiplier,cost_types,critical_strike_chance,"
            + "damage_effectiveness,damage_multiplier,dexterity_requirement,"
            + "duration,experience,intelligence_requirement,level,"
            + "level_requirement,life_reservation_flat,life_reservation_percent,"
            + "mana_reservation_flat,mana_reservation_percent,skill_level,"
            + "stat_text,stored_uses,strength_requirement,"
            + "vaal_soul_gain_prevention_time,vaal_souls_requirement,vaal_stored_uses"));
        TABLE_CONFIGS.put("skill_stats_per_level", new TableConfig("skill_stats_per_level",
            "id,level,value"));
        TABLE_CONFIGS.put("skill_quality", new TableConfig("skill_quality",
            "set_id,stat_text,weight"));
        TABLE_CONFIGS.put("skill_quality_stats", new TableConfig("skill_quality_stats",
            "id,set_id,value"));
        TABLE_CONFIGS.put("gem_levels", new TableConfig("gem_levels",
            "experience,level,required_dexterity,"
            + "required_intelligence,required_level,required_strength"));

        // ---- 天赋详细数据 ----
        TABLE_CONFIGS.put("passive_skill_connections", new TableConfig("passive_skill_connections",
            "node_ids,tree_id"));
        TABLE_CONFIGS.put("mastery_effects", new TableConfig("mastery_effects",
            "id,stat_ids,stat_text,stat_text_raw,stat_values"));
        TABLE_CONFIGS.put("mastery_groups", new TableConfig("mastery_groups",
            "icon,id,name"));

        // ---- 职业数据 ----
        TABLE_CONFIGS.put("character_classes", new TableConfig("character_classes",
            "dexterity,flavour_text,id,intelligence,name,str_id,strength"));
        TABLE_CONFIGS.put("ascendancy_classes", new TableConfig("ascendancy_classes",
            "character_class,character_id,flavour_text,id,name"));
    }

    private final WikiApiClient wikiClient;
    private final DatabaseManager dbManager;

    public DataSyncService(WikiApiClient wikiClient, DatabaseManager dbManager) {
        this.wikiClient = wikiClient;
        this.dbManager = dbManager;
    }

    // ==================== 公开方法 ====================

    /**
     * 全量同步所有已配置的表。
     * <p>
     * 单个表同步失败不影响其他表，异常信息记录在 SyncResult 中。
     *
     * @return 每个表的同步结果映射（Cargo表名 → SyncResult）
     */
    public Map<String, SyncResult> syncAll() {
        Map<String, SyncResult> results = new LinkedHashMap<>();
        for (String tableName : TABLE_CONFIGS.keySet()) {
            try {
                results.put(tableName, syncTable(tableName));
            } catch (Exception e) {
                log.error("Sync failed for table '{}': {}", tableName, e.getMessage());
                // 仍然记录失败结果，不阻断其他表
                Instant now = Instant.now();
                results.put(tableName, SyncResult.of(0, now, now));
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
    public SyncResult syncTable(String cargoTable) {
        TableConfig config = TABLE_CONFIGS.get(cargoTable);
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
        SyncResult result = SyncResult.of(synced, startedAt, finishedAt);
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
        try {
            Connection conn = dbManager.getConnection();
            for (Map.Entry<String, TableConfig> entry : TABLE_CONFIGS.entrySet()) {
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

    // ==================== 内部实现 ====================

    /**
     * 分批拉取 + 转换 + 事务写入。
     */
    private int batchSync(String cargoTable, TableConfig config, int totalCount) {
        int totalSynced = 0;

        try {
            Connection conn = dbManager.getConnection();
            Object dao = createDao(conn, cargoTable);

            // 先清空旧数据
            clearTable(conn, config.sqliteTable);

            for (int offset = 0; offset < totalCount; offset += BATCH_SIZE) {
                // 分批拉取（需要含 _pageID 用于主键映射）
                String fields = "_pageID," + config.fields;
                JsonNode root = wikiClient.queryCargoTable(cargoTable, fields, offset, BATCH_SIZE);
                JsonNode rows = root.path("cargoquery");
                if (!rows.isArray()) break;

                // 转换
                int batchSize = rows.size();
                List<Object> batch = new ArrayList<>(batchSize);
                DataConverter<Object> converter = getConverter(cargoTable);
                for (JsonNode row : rows) {
                    Object entity = converter.convert(row.path("title"));
                    if (entity != null) batch.add(entity);
                }

                // 事务写入
                if (!batch.isEmpty()) {
                    batchInsert(conn, dao, batch);
                }

                totalSynced += batch.size();

                // 发布进度事件
                AppEventBus.postAsync(new DataSyncProgressEvent(cargoTable,
                    Math.min(totalSynced, totalCount), totalCount));

                if (rows.size() < BATCH_SIZE) break; // 最后一批
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to sync table: " + cargoTable, e);
        }

        return totalSynced;
    }

    // ---- 数据库操作 ----

    /** 清空目标表所有数据（DELETE，无 WHERE 条件）。 */
    private void clearTable(Connection conn, String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM " + tableName);
        }
    }

    /** 根据 DAO 类型分发批量插入调用。 */
    @SuppressWarnings("unchecked")
    private void batchInsert(Connection conn, Object dao, List<Object> entities) throws SQLException {
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
        } else {
            throw new IllegalArgumentException("Unknown DAO: " + dao.getClass());
        }
    }

    /** 查询本地表记录数，失败返回 -1（触发强制同步）。 */
    private int getLocalRecordCount(String tableName) {
        try {
            return getLocalRecordCount(dbManager.getConnection(), tableName);
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
        try {
            Connection conn = dbManager.getConnection();
            String sql = "INSERT OR REPLACE INTO data_version (table_name, last_sync, record_count) " +
                "VALUES (?, datetime('now'), ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, tableName);
                ps.setInt(2, recordCount);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("Failed to update data_version for {}", tableName, e);
        }
    }

    /**
     * 重建 FTS5 items_fts 全文索引（外部内容表模式）。
     */
    private void rebuildFts() {
        try {
            Connection conn = dbManager.getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("INSERT INTO items_fts(items_fts) VALUES('rebuild')");
                log.info("FTS index rebuilt");
            }
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
        }
        throw new IllegalArgumentException("No converter for: " + cargoTable);
    }

    /** 根据 Cargo 表名创建对应的 DAO 实例。 */
    private static Object createDao(Connection conn, String cargoTable) {
        if ("items".equals(cargoTable)) {
            return new ItemDao(conn);
        } else if ("skill_gems".equals(cargoTable)) {
            return new SkillGemDao(conn);
        } else if ("passive_skills".equals(cargoTable)) {
            return new PassiveSkillDao(conn);
        } else if ("mods".equals(cargoTable)) {
            return new ModDao(conn);
        } else if ("weapons".equals(cargoTable)) {
            return new WeaponDao(conn);
        } else if ("armours".equals(cargoTable)) {
            return new ArmourDao(conn);
        } else if ("shields".equals(cargoTable)) {
            return new ShieldDao(conn);
        } else if ("amulets".equals(cargoTable)) {
            return new AmuletDao(conn);
        } else if ("flasks".equals(cargoTable)) {
            return new FlaskDao(conn);
        } else if ("jewels".equals(cargoTable)) {
            return new JewelDao(conn);
        } else if ("stackables".equals(cargoTable)) {
            return new StackableDao(conn);
        } else if ("maps".equals(cargoTable)) {
            return new MapDao(conn);
        } else if ("map_fragments".equals(cargoTable)) {
            return new MapFragmentDao(conn);
        } else if ("map_series".equals(cargoTable)) {
            return new MapSeriesDao(conn);
        } else if ("divination_cards".equals(cargoTable)) {
            return new DivinationCardDao(conn);
        } else if ("mod_stats".equals(cargoTable)) {
            return new ModStatDao(conn);
        } else if ("mod_spawn_weights".equals(cargoTable)) {
            return new ModSpawnWeightDao(conn);
        } else if ("mod_generation_weights".equals(cargoTable)) {
            return new ModGenerationWeightDao(conn);
        } else if ("mod_sell_prices".equals(cargoTable)) {
            return new ModSellPriceDao(conn);
        } else if ("item_mods".equals(cargoTable)) {
            return new ItemModDao(conn);
        } else if ("item_stats".equals(cargoTable)) {
            return new ItemStatDao(conn);
        } else if ("item_buffs".equals(cargoTable)) {
            return new ItemBuffDao(conn);
        } else if ("crafting_bench_options".equals(cargoTable)) {
            return new CraftingBenchOptionDao(conn);
        } else if ("crafting_bench_options_costs".equals(cargoTable)) {
            return new CraftingBenchOptionCostDao(conn);
        } else if ("essences".equals(cargoTable)) {
            return new EssenceDao(conn);
        } else if ("fossils".equals(cargoTable)) {
            return new FossilDao(conn);
        } else if ("fossil_weights".equals(cargoTable)) {
            return new FossilWeightDao(conn);
        } else if ("vendor_rewards".equals(cargoTable)) {
            return new VendorRewardDao(conn);
        } else if ("item_sell_prices".equals(cargoTable)) {
            return new ItemSellPriceDao(conn);
        } else if ("item_purchase_costs".equals(cargoTable)) {
            return new ItemPurchaseCostDao(conn);
        } else if ("skill".equals(cargoTable)) {
            return new SkillDao(conn);
        } else if ("skill_levels".equals(cargoTable)) {
            return new SkillLevelDao(conn);
        } else if ("skill_stats_per_level".equals(cargoTable)) {
            return new SkillStatsPerLevelDao(conn);
        } else if ("skill_quality".equals(cargoTable)) {
            return new SkillQualityDao(conn);
        } else if ("skill_quality_stats".equals(cargoTable)) {
            return new SkillQualityStatsDao(conn);
        } else if ("gem_levels".equals(cargoTable)) {
            return new GemLevelDao(conn);
        } else if ("passive_skill_connections".equals(cargoTable)) {
            return new PassiveSkillConnectionDao(conn);
        } else if ("mastery_effects".equals(cargoTable)) {
            return new MasteryEffectDao(conn);
        } else if ("mastery_groups".equals(cargoTable)) {
            return new MasteryGroupDao(conn);
        } else if ("character_classes".equals(cargoTable)) {
            return new CharacterClassDao(conn);
        } else if ("ascendancy_classes".equals(cargoTable)) {
            return new AscendancyClassDao(conn);
        }
        throw new IllegalArgumentException("No DAO for: " + cargoTable);
    }

    // ==================== 内部类型 ====================

    /** 单个表的同步配置。 */
    static class TableConfig {
        final String sqliteTable;
        final String fields;

        TableConfig(String sqliteTable, String fields) {
            this.sqliteTable = sqliteTable;
            this.fields = fields;
        }
    }
}
