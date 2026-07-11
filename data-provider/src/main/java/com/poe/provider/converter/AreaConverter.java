package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Area;

/**
 * 将 Wiki Cargo {@code areas} 表行转换为 {@link Area} 模型。
 * <p>
 * Cargo 字段：act, area_level, area_type_tags, boss_monster_ids, connection_ids,
 * entry_npc, entry_text, flavour_text, has_waypoint, id, infobox_html,
 * is_hideout_area, is_labyrinth_airlock_area, is_labyrinth_area,
 * is_labyrinth_boss_area, is_legacy_map_area, is_map_area, is_town_area,
 * is_unique_map_area, is_vaal_area, level_restriction_max, loading_screen,
 * main_page, mainpage_categories, modifier_ids, monster_ids, name,
 * parent_area_id, release_version, removal_version, screenshot, stat_text,
 * strongbox_max_count, strongbox_spawn_chance, strongbox_weight_magic,
 * strongbox_weight_normal, strongbox_weight_rare, strongbox_weight_unique,
 * tags, vaal_area_ids, vaal_area_spawn_chance。
 */
public class AreaConverter implements DataConverter<Area> {

    /**
     * 将 Cargo 单行 JSON 转换为 Area 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 区域实体，字段缺失时使用默认值
     */
    @Override
    public Area convert(JsonNode row) {
        Area a = new Area();
        a.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        a.setPageName(row.path("_pageName").asText());
        a.setAct(ItemConverter.parseIntSafe(row, "act"));
        a.setAreaLevel(ItemConverter.parseIntSafe(row, "area_level"));
        a.setAreaTypeTags(ItemConverter.nullableText(row, "area_type_tags"));
        a.setBossMonsterIds(ItemConverter.nullableText(row, "boss_monster_ids"));
        a.setConnectionIds(ItemConverter.nullableText(row, "connection_ids"));
        a.setEntryNpc(ItemConverter.nullableText(row, "entry_npc"));
        a.setEntryText(ItemConverter.nullableText(row, "entry_text"));
        a.setFlavourText(ItemConverter.nullableText(row, "flavour_text"));
        a.setHasWaypoint(ItemConverter.parseBooleanSafe(row, "has_waypoint") ? 1 : 0);
        a.setId(row.path("id").asText());
        a.setInfoboxHtml(ItemConverter.nullableText(row, "infobox_html"));
        a.setIsHideoutArea(ItemConverter.parseBooleanSafe(row, "is_hideout_area") ? 1 : 0);
        a.setIsLabyrinthAirlockArea(ItemConverter.parseBooleanSafe(row, "is_labyrinth_airlock_area") ? 1 : 0);
        a.setIsLabyrinthArea(ItemConverter.parseBooleanSafe(row, "is_labyrinth_area") ? 1 : 0);
        a.setIsLabyrinthBossArea(ItemConverter.parseBooleanSafe(row, "is_labyrinth_boss_area") ? 1 : 0);
        a.setIsLegacyMapArea(ItemConverter.parseBooleanSafe(row, "is_legacy_map_area") ? 1 : 0);
        a.setIsMapArea(ItemConverter.parseBooleanSafe(row, "is_map_area") ? 1 : 0);
        a.setIsTownArea(ItemConverter.parseBooleanSafe(row, "is_town_area") ? 1 : 0);
        a.setIsUniqueMapArea(ItemConverter.parseBooleanSafe(row, "is_unique_map_area") ? 1 : 0);
        a.setIsVaalArea(ItemConverter.parseBooleanSafe(row, "is_vaal_area") ? 1 : 0);
        a.setLevelRestrictionMax(ItemConverter.parseIntSafe(row, "level_restriction_max"));
        a.setLoadingScreen(ItemConverter.nullableText(row, "loading_screen"));
        a.setMainPage(ItemConverter.nullableText(row, "main_page"));
        a.setMainpageCategories(ItemConverter.nullableText(row, "mainpage_categories"));
        a.setModifierIds(ItemConverter.nullableText(row, "modifier_ids"));
        a.setMonsterIds(ItemConverter.nullableText(row, "monster_ids"));
        a.setName(row.path("name").asText());
        a.setParentAreaId(ItemConverter.nullableText(row, "parent_area_id"));
        a.setReleaseVersion(ItemConverter.nullableText(row, "release_version"));
        a.setRemovalVersion(ItemConverter.nullableText(row, "removal_version"));
        a.setScreenshot(ItemConverter.nullableText(row, "screenshot"));
        a.setStatText(ItemConverter.nullableText(row, "stat_text"));
        a.setStrongboxMaxCount(ItemConverter.parseIntSafe(row, "strongbox_max_count"));
        a.setStrongboxSpawnChance(ItemConverter.parseIntSafe(row, "strongbox_spawn_chance"));
        a.setStrongboxWeightMagic(ItemConverter.parseIntSafe(row, "strongbox_weight_magic"));
        a.setStrongboxWeightNormal(ItemConverter.parseIntSafe(row, "strongbox_weight_normal"));
        a.setStrongboxWeightRare(ItemConverter.parseIntSafe(row, "strongbox_weight_rare"));
        a.setStrongboxWeightUnique(ItemConverter.parseIntSafe(row, "strongbox_weight_unique"));
        a.setTags(ItemConverter.nullableText(row, "tags"));
        a.setVaalAreaIds(ItemConverter.nullableText(row, "vaal_area_ids"));
        a.setVaalAreaSpawnChance(ItemConverter.parseIntSafe(row, "vaal_area_spawn_chance"));
        return a;
    }
}
