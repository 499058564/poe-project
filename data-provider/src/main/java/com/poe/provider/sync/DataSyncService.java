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

        // ---- 怪物 ----
        TABLE_CONFIGS.put("monsters", new TableConfig("monsters",
            "attack_speed,critical_strike_chance,damage_multiplier,endgame_mod_ids,"
            + "experience_multiplier,health_multiplier,is_boss,maximum_attack_distance,"
            + "metadata_id,minimum_attack_distance,mod_ids,model_size_multiplier,"
            + "monster_type_id,name,part1_mod_ids,part2_mod_ids,rarity,rarity_id,"
            + "size,skill_ids,tags"));
        TABLE_CONFIGS.put("monster_types", new TableConfig("monster_types",
            "armour_multiplier,damage_spread,energy_shield_multiplier,"
            + "evasion_multiplier,id,monster_resistance_id,tags"));
        TABLE_CONFIGS.put("monster_base_stats", new TableConfig("monster_base_stats",
            "accuracy,armour,damage,evasion,experience,level,life,summon_life"));
        TABLE_CONFIGS.put("monster_life_scaling", new TableConfig("monster_life_scaling",
            "level,magic,rare"));
        TABLE_CONFIGS.put("monster_map_multipliers", new TableConfig("monster_map_multipliers",
            "boss_damage,boss_item_quantity,boss_item_rarity,boss_life,damage,level,life"));
        TABLE_CONFIGS.put("monster_resistances", new TableConfig("monster_resistances",
            "id,maps_chaos,maps_cold,maps_fire,maps_lightning,"
            + "part1_chaos,part1_cold,part1_fire,part1_lightning,"
            + "part2_chaos,part2_cold,part2_fire,part2_lightning"));
        // ---- 区域 ----
        TABLE_CONFIGS.put("areas", new TableConfig("areas",
            "act,area_level,area_type_tags,boss_monster_ids,connection_ids,"
            + "entry_npc,entry_text,flavour_text,has_waypoint,id,infobox_html,"
            + "is_hideout_area,is_labyrinth_airlock_area,is_labyrinth_area,"
            + "is_labyrinth_boss_area,is_legacy_map_area,is_map_area,is_town_area,"
            + "is_unique_map_area,is_vaal_area,level_restriction_max,loading_screen,"
            + "main_page,mainpage_categories,modifier_ids,monster_ids,name,"
            + "parent_area_id,release_version,removal_version,screenshot,stat_text,"
            + "strongbox_max_count,strongbox_spawn_chance,strongbox_weight_magic,"
            + "strongbox_weight_normal,strongbox_weight_rare,strongbox_weight_unique,"
            + "tags,vaal_area_ids,vaal_area_spawn_chance"));
        // ---- 异界图鉴 ----
        TABLE_CONFIGS.put("atlas_nodes", new TableConfig("atlas_nodes",
            "area_id,connections,div_cards,id,is_off_atlas,"
            + "region_connections_0,region_connections_1,region_connections_2,"
            + "region_connections_3,region_connections_4,region_id,region_minimum,"
            + "series_id,tier_0,tier_1,tier_2,tier_3,tier_4"));

        // ---- Delve ----
        TABLE_CONFIGS.put("delve_level_scaling", new TableConfig("delve_level_scaling",
            "darkness_resistance,depth,light_radius,monster_damage,"
            + "monster_level,monster_life,sulphite_cost"));
        TABLE_CONFIGS.put("delve_resources_per_level", new TableConfig("delve_resources_per_level",
            "area_level,sulphite"));
        TABLE_CONFIGS.put("delve_upgrades", new TableConfig("delve_upgrades",
            "cost,level,type"));
        TABLE_CONFIGS.put("delve_upgrade_stats", new TableConfig("delve_upgrade_stats",
            "id,level,type,value"));

        // ---- Heist ----
        TABLE_CONFIGS.put("heist_areas", new TableConfig("heist_areas",
            "area_ids,blueprint_id,contract_id,id,job_ids,reward_text"));
        TABLE_CONFIGS.put("heist_jobs", new TableConfig("heist_jobs",
            "id,name"));
        TABLE_CONFIGS.put("heist_npcs", new TableConfig("heist_npcs",
            "id,job_id,name,stat_text"));
        TABLE_CONFIGS.put("heist_npc_skills", new TableConfig("heist_npc_skills",
            "job_id,level,npc_id"));
        TABLE_CONFIGS.put("heist_npc_stats", new TableConfig("heist_npc_stats",
            "npc_id,stat_id,value"));
        TABLE_CONFIGS.put("heist_equipment", new TableConfig("heist_equipment",
            "required_job_id,required_job_level"));

        // ---- Blight ----
        TABLE_CONFIGS.put("blight_crafting_recipes", new TableConfig("blight_crafting_recipes",
            "id,modifier_id,passive_id,type"));
        TABLE_CONFIGS.put("blight_crafting_recipes_items", new TableConfig("blight_crafting_recipes_items",
            "item_id,ordinal,recipe_id"));
        TABLE_CONFIGS.put("blight_items", new TableConfig("blight_items",
            "tier"));
        TABLE_CONFIGS.put("blight_towers", new TableConfig("blight_towers",
            "cost,description,icon,id,name,radius,tier"));

        // ---- Harvest ----
        TABLE_CONFIGS.put("harvest_crafting_options", new TableConfig("harvest_crafting_options",
            "cost_primal,cost_rancour,cost_sacred,cost_vivid,cost_wild,"
            + "effect,effect_html,id,ordinal"));
        TABLE_CONFIGS.put("harvest_plant_boosters", new TableConfig("harvest_plant_boosters",
            "additional_crafting_options,extra_chances,lifeforce,radius"));
        TABLE_CONFIGS.put("harvest_seeds", new TableConfig("harvest_seeds",
            "consumed_primal_lifeforce_percentage,consumed_vivid_lifeforce_percentage,"
            + "consumed_wild_lifeforce_percentage,effect,granted_craft_option_ids,"
            + "growth_cycles,required_nearby_seed_amount,required_nearby_seed_tier,"
            + "tier,type,type_id"));

        // ---- Synthesis ----
        TABLE_CONFIGS.put("synthesis_areas", new TableConfig("synthesis_areas",
            "id,max_level,min_level,name,size,weight"));
        TABLE_CONFIGS.put("synthesis_corrupted_mods", new TableConfig("synthesis_corrupted_mods",
            "item_class_id,mod_ids"));
        TABLE_CONFIGS.put("synthesis_global_mods", new TableConfig("synthesis_global_mods",
            "max_level,min_level,mod_id,weight"));
        TABLE_CONFIGS.put("synthesis_mods", new TableConfig("synthesis_mods",
            "item_class_ids,mod_ids,stat_id,stat_text,stat_value"));

        // ---- Bestiary ----
        TABLE_CONFIGS.put("bestiary_recipes", new TableConfig("bestiary_recipes",
            "game_mode,header,id,notes,subheader"));
        TABLE_CONFIGS.put("bestiary_recipe_components", new TableConfig("bestiary_recipe_components",
            "amount,component_id,recipe_id"));

        // ---- Incursion ----
        TABLE_CONFIGS.put("incursion_rooms", new TableConfig("incursion_rooms",
            "architect_metadata_id,architect_name,description,flavour_text,"
            + "icon,id,min_level,modifier_ids,name,stat_text,tier,upgrade_room_id"));

        // ---- Pantheon ----
        TABLE_CONFIGS.put("pantheon", new TableConfig("pantheon",
            "id,is_major_god"));
        TABLE_CONFIGS.put("pantheon_souls", new TableConfig("pantheon_souls",
            "id,item_id,name,ordinal,stat_text,target_area_id,target_monster_id"));
        TABLE_CONFIGS.put("pantheon_stats", new TableConfig("pantheon_stats",
            "id,ordinal,pantheon_id,pantheon_ordinal,value"));

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

        // ---- 杂项与历史数据 ----
        TABLE_CONFIGS.put("versions", new TableConfig("versions",
            "after,major_part,minor_part,patch_part,previous,release_date,revision_part,version"));
        TABLE_CONFIGS.put("legacy_variants", new TableConfig("legacy_variants",
            "removal_version,implicit_stat_text,explicit_stat_text,stat_text,base_item,required_level"));
        TABLE_CONFIGS.put("prophecies", new TableConfig("prophecies",
            "objective,prediction_text,prophecy_id,reward,seal_cost"));
        TABLE_CONFIGS.put("quest_rewards", new TableConfig("quest_rewards",
            "act,class_ids,classes,item_level,notes,quest,quest_id,rarity,sockets"));
        TABLE_CONFIGS.put("spawn_weights", new TableConfig("spawn_weights",
            "ordinal,tag,weight"));
        TABLE_CONFIGS.put("generic_stats", new TableConfig("generic_stats",
            "id,name,stat_text,value"));

        // ---- 赛季特有物品 ----
        TABLE_CONFIGS.put("tattoos", new TableConfig("tattoos",
            "max_adjacent,min_adjacent,skill_id,target,tattoo_limit,tribe"));
        TABLE_CONFIGS.put("tinctures", new TableConfig("tinctures",
            "cooldown,cooldown_html,cooldown_range_average,cooldown_range_colour,"
            + "cooldown_range_maximum,cooldown_range_minimum,cooldown_range_text,"
            + "debuff_interval,debuff_interval_html,debuff_interval_range_average,"
            + "debuff_interval_range_colour,debuff_interval_range_maximum,"
            + "debuff_interval_range_minimum,debuff_interval_range_text"));
        TABLE_CONFIGS.put("sentinels", new TableConfig("sentinels",
            "charge,charge_html,charge_range_average,charge_range_colour,"
            + "charge_range_maximum,charge_range_minimum,charge_range_text,"
            + "duration,duration_html,duration_range_average,duration_range_colour,"
            + "duration_range_maximum,duration_range_minimum,duration_range_text,"
            + "empowerment,empowerment_html,empowerment_range_average,empowerment_range_colour,"
            + "empowerment_range_maximum,empowerment_range_minimum,empowerment_range_text,"
            + "empowers,empowers_html,empowers_range_average,empowers_range_colour,"
            + "empowers_range_maximum,empowers_range_minimum,empowers_range_text,"
            + "monster,monster_level"));
        TABLE_CONFIGS.put("idols", new TableConfig("idols",
            "idol_limit"));
        TABLE_CONFIGS.put("grafts", new TableConfig("grafts",
            "skill_id"));
        TABLE_CONFIGS.put("corpse_items", new TableConfig("corpse_items",
            "monster_abilities,monster_category,monster_category_html,tier"));

        // ---- 杂项低优先级 ----
        TABLE_CONFIGS.put("cosmetic_items", new TableConfig("cosmetic_items",
            "cosmetic_type,target,theme"));
        TABLE_CONFIGS.put("hideout_doodads", new TableConfig("hideout_doodads",
            "is_master_doodad,variation_count"));
        TABLE_CONFIGS.put("guides", new TableConfig("guides",
            "date,subject,version"));
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
        } else if ("monsters".equals(cargoTable)) {
            return new MonsterDao(conn);
        } else if ("monster_types".equals(cargoTable)) {
            return new MonsterTypeDao(conn);
        } else if ("monster_base_stats".equals(cargoTable)) {
            return new MonsterBaseStatDao(conn);
        } else if ("monster_life_scaling".equals(cargoTable)) {
            return new MonsterLifeScalingDao(conn);
        } else if ("monster_map_multipliers".equals(cargoTable)) {
            return new MonsterMapMultiplierDao(conn);
        } else if ("monster_resistances".equals(cargoTable)) {
            return new MonsterResistanceDao(conn);
        } else if ("areas".equals(cargoTable)) {
            return new AreaDao(conn);
        } else if ("atlas_nodes".equals(cargoTable)) {
            return new AtlasNodeDao(conn);
        } else if ("delve_level_scaling".equals(cargoTable)) {
            return new DelveLevelScalingDao(conn);
        } else if ("delve_resources_per_level".equals(cargoTable)) {
            return new DelveResourcesPerLevelDao(conn);
        } else if ("delve_upgrades".equals(cargoTable)) {
            return new DelveUpgradesDao(conn);
        } else if ("delve_upgrade_stats".equals(cargoTable)) {
            return new DelveUpgradeStatsDao(conn);
        } else if ("heist_areas".equals(cargoTable)) {
            return new HeistAreasDao(conn);
        } else if ("heist_jobs".equals(cargoTable)) {
            return new HeistJobsDao(conn);
        } else if ("heist_npcs".equals(cargoTable)) {
            return new HeistNpcsDao(conn);
        } else if ("heist_npc_skills".equals(cargoTable)) {
            return new HeistNpcSkillsDao(conn);
        } else if ("heist_npc_stats".equals(cargoTable)) {
            return new HeistNpcStatsDao(conn);
        } else if ("heist_equipment".equals(cargoTable)) {
            return new HeistEquipmentDao(conn);
        } else if ("blight_crafting_recipes".equals(cargoTable)) {
            return new BlightCraftingRecipesDao(conn);
        } else if ("blight_crafting_recipes_items".equals(cargoTable)) {
            return new BlightCraftingRecipesItemsDao(conn);
        } else if ("blight_items".equals(cargoTable)) {
            return new BlightItemsDao(conn);
        } else if ("blight_towers".equals(cargoTable)) {
            return new BlightTowersDao(conn);
        } else if ("harvest_crafting_options".equals(cargoTable)) {
            return new HarvestCraftingOptionsDao(conn);
        } else if ("harvest_plant_boosters".equals(cargoTable)) {
            return new HarvestPlantBoostersDao(conn);
        } else if ("harvest_seeds".equals(cargoTable)) {
            return new HarvestSeedsDao(conn);
        } else if ("synthesis_areas".equals(cargoTable)) {
            return new SynthesisAreasDao(conn);
        } else if ("synthesis_corrupted_mods".equals(cargoTable)) {
            return new SynthesisCorruptedModsDao(conn);
        } else if ("synthesis_global_mods".equals(cargoTable)) {
            return new SynthesisGlobalModsDao(conn);
        } else if ("synthesis_mods".equals(cargoTable)) {
            return new SynthesisModsDao(conn);
        } else if ("bestiary_recipes".equals(cargoTable)) {
            return new BestiaryRecipesDao(conn);
        } else if ("bestiary_recipe_components".equals(cargoTable)) {
            return new BestiaryRecipeComponentsDao(conn);
        } else if ("incursion_rooms".equals(cargoTable)) {
            return new IncursionRoomsDao(conn);
        } else if ("pantheon".equals(cargoTable)) {
            return new PantheonDao(conn);
        } else if ("pantheon_souls".equals(cargoTable)) {
            return new PantheonSoulsDao(conn);
        } else if ("pantheon_stats".equals(cargoTable)) {
            return new PantheonStatsDao(conn);
        } else if ("versions".equals(cargoTable)) {
            return new VersionDao(conn);
        } else if ("legacy_variants".equals(cargoTable)) {
            return new LegacyVariantDao(conn);
        } else if ("prophecies".equals(cargoTable)) {
            return new ProphecyDao(conn);
        } else if ("quest_rewards".equals(cargoTable)) {
            return new QuestRewardDao(conn);
        } else if ("spawn_weights".equals(cargoTable)) {
            return new SpawnWeightDao(conn);
        } else if ("generic_stats".equals(cargoTable)) {
            return new GenericStatDao(conn);
        } else if ("tattoos".equals(cargoTable)) {
            return new TattooDao(conn);
        } else if ("tinctures".equals(cargoTable)) {
            return new TinctureDao(conn);
        } else if ("sentinels".equals(cargoTable)) {
            return new SentinelDao(conn);
        } else if ("idols".equals(cargoTable)) {
            return new IdolDao(conn);
        } else if ("grafts".equals(cargoTable)) {
            return new GraftDao(conn);
        } else if ("corpse_items".equals(cargoTable)) {
            return new CorpseItemDao(conn);
        } else if ("cosmetic_items".equals(cargoTable)) {
            return new CosmeticItemDao(conn);
        } else if ("hideout_doodads".equals(cargoTable)) {
            return new HideoutDoodadDao(conn);
        } else if ("guides".equals(cargoTable)) {
            return new GuideDao(conn);
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
