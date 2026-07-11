-- v011: 怪物、区域与异界图鉴数据
-- 基于 2026-07-11 Cargo 字段验证结果（Special:CargoTables）

-- ============================
-- 怪物数据
-- ============================

-- 1. monsters: 怪物基本属性（10,642 行）
CREATE TABLE IF NOT EXISTS monsters (
    page_id                    INTEGER NOT NULL PRIMARY KEY,
    page_name                  TEXT    NOT NULL,
    attack_speed               INTEGER DEFAULT 0,
    critical_strike_chance     INTEGER DEFAULT 0,
    damage_multiplier          REAL    DEFAULT 0.0,
    endgame_mod_ids            TEXT,
    experience_multiplier      REAL    DEFAULT 0.0,
    health_multiplier          REAL    DEFAULT 0.0,
    is_boss                    INTEGER DEFAULT 0,
    maximum_attack_distance    INTEGER DEFAULT 0,
    metadata_id                TEXT,
    minimum_attack_distance    INTEGER DEFAULT 0,
    mod_ids                    TEXT,
    model_size_multiplier      REAL    DEFAULT 0.0,
    monster_type_id            TEXT,
    name                       TEXT,
    part1_mod_ids              TEXT,
    part2_mod_ids              TEXT,
    rarity                     TEXT,
    rarity_id                  TEXT,
    size                       INTEGER DEFAULT 0,
    skill_ids                  TEXT,
    tags                       TEXT
);
CREATE INDEX IF NOT EXISTS idx_monsters_name ON monsters(name);
CREATE INDEX IF NOT EXISTS idx_monsters_monster_type_id ON monsters(monster_type_id);
CREATE INDEX IF NOT EXISTS idx_monsters_is_boss ON monsters(is_boss);

-- 2. monster_types: 怪物类型定义（3,534 行）
CREATE TABLE IF NOT EXISTS monster_types (
    page_id                  INTEGER NOT NULL PRIMARY KEY,
    page_name                TEXT    NOT NULL,
    armour_multiplier        INTEGER DEFAULT 0,
    damage_spread            REAL    DEFAULT 0.0,
    energy_shield_multiplier INTEGER DEFAULT 0,
    evasion_multiplier       INTEGER DEFAULT 0,
    id                       TEXT,
    monster_resistance_id    TEXT,
    tags                     TEXT
);
CREATE INDEX IF NOT EXISTS idx_monster_types_id ON monster_types(id);
CREATE INDEX IF NOT EXISTS idx_monster_types_resistance_id ON monster_types(monster_resistance_id);

-- 3. monster_base_stats: 怪物基础属性（100 行，按等级）
CREATE TABLE IF NOT EXISTS monster_base_stats (
    level       INTEGER NOT NULL PRIMARY KEY,
    accuracy    INTEGER DEFAULT 0,
    armour      INTEGER DEFAULT 0,
    damage      REAL    DEFAULT 0.0,
    evasion     INTEGER DEFAULT 0,
    experience  INTEGER DEFAULT 0,
    life        INTEGER DEFAULT 0,
    summon_life INTEGER DEFAULT 0
);

-- 4. monster_life_scaling: 怪物生命倍率（100 行，按等级）
CREATE TABLE IF NOT EXISTS monster_life_scaling (
    level INTEGER NOT NULL PRIMARY KEY,
    magic INTEGER DEFAULT 0,
    rare  INTEGER DEFAULT 0
);

-- 5. monster_map_multipliers: 异界地图怪物倍率（82 行，按等级）
CREATE TABLE IF NOT EXISTS monster_map_multipliers (
    level               INTEGER NOT NULL PRIMARY KEY,
    boss_damage         INTEGER DEFAULT 0,
    boss_item_quantity  INTEGER DEFAULT 0,
    boss_item_rarity    INTEGER DEFAULT 0,
    boss_life           INTEGER DEFAULT 0,
    damage              INTEGER DEFAULT 0,
    life                INTEGER DEFAULT 0
);

-- 6. monster_resistances: 怪物抗性配置（50 行）
CREATE TABLE IF NOT EXISTS monster_resistances (
    page_id          INTEGER NOT NULL PRIMARY KEY,
    page_name        TEXT    NOT NULL,
    resistance_id    TEXT,
    maps_chaos       INTEGER DEFAULT 0,
    maps_cold        INTEGER DEFAULT 0,
    maps_fire        INTEGER DEFAULT 0,
    maps_lightning   INTEGER DEFAULT 0,
    part1_chaos      INTEGER DEFAULT 0,
    part1_cold       INTEGER DEFAULT 0,
    part1_fire       INTEGER DEFAULT 0,
    part1_lightning  INTEGER DEFAULT 0,
    part2_chaos      INTEGER DEFAULT 0,
    part2_cold       INTEGER DEFAULT 0,
    part2_fire       INTEGER DEFAULT 0,
    part2_lightning  INTEGER DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_monster_resistances_resistance_id ON monster_resistances(resistance_id);

-- ============================
-- 区域数据
-- ============================

-- 7. areas: 区域信息（1,884 行）
CREATE TABLE IF NOT EXISTS areas (
    page_id                     INTEGER NOT NULL PRIMARY KEY,
    page_name                   TEXT    NOT NULL,
    act                         INTEGER DEFAULT 0,
    area_level                  INTEGER DEFAULT 0,
    area_type_tags              TEXT,
    boss_monster_ids            TEXT,
    connection_ids              TEXT,
    entry_npc                   TEXT,
    entry_text                  TEXT,
    flavour_text                TEXT,
    has_waypoint                INTEGER DEFAULT 0,
    id                          TEXT,
    infobox_html                TEXT,
    is_hideout_area             INTEGER DEFAULT 0,
    is_labyrinth_airlock_area   INTEGER DEFAULT 0,
    is_labyrinth_area           INTEGER DEFAULT 0,
    is_labyrinth_boss_area      INTEGER DEFAULT 0,
    is_legacy_map_area          INTEGER DEFAULT 0,
    is_map_area                 INTEGER DEFAULT 0,
    is_town_area                INTEGER DEFAULT 0,
    is_unique_map_area          INTEGER DEFAULT 0,
    is_vaal_area                INTEGER DEFAULT 0,
    level_restriction_max       INTEGER DEFAULT 0,
    loading_screen              TEXT,
    main_page                   TEXT,
    mainpage_categories         TEXT,
    modifier_ids                TEXT,
    monster_ids                 TEXT,
    name                        TEXT,
    parent_area_id              TEXT,
    release_version             TEXT,
    removal_version             TEXT,
    screenshot                  TEXT,
    stat_text                   TEXT,
    strongbox_max_count         INTEGER DEFAULT 0,
    strongbox_spawn_chance      INTEGER DEFAULT 0,
    strongbox_weight_magic      INTEGER DEFAULT 0,
    strongbox_weight_normal     INTEGER DEFAULT 0,
    strongbox_weight_rare       INTEGER DEFAULT 0,
    strongbox_weight_unique     INTEGER DEFAULT 0,
    tags                        TEXT,
    vaal_area_ids               TEXT,
    vaal_area_spawn_chance      INTEGER DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_areas_name ON areas(name);
CREATE INDEX IF NOT EXISTS idx_areas_act ON areas(act);
CREATE INDEX IF NOT EXISTS idx_areas_area_level ON areas(area_level);
CREATE INDEX IF NOT EXISTS idx_areas_is_map_area ON areas(is_map_area);
CREATE INDEX IF NOT EXISTS idx_areas_is_town_area ON areas(is_town_area);

-- ============================
-- 异界图鉴数据
-- ============================

-- 8. atlas_nodes: 异界图鉴节点（3,414 行）
CREATE TABLE IF NOT EXISTS atlas_nodes (
    page_id               INTEGER NOT NULL PRIMARY KEY,
    page_name             TEXT    NOT NULL,
    area_id               TEXT,
    connections           TEXT,
    div_cards             TEXT,
    id                    TEXT,
    is_off_atlas          INTEGER DEFAULT 0,
    region_connections_0  TEXT,
    region_connections_1  TEXT,
    region_connections_2  TEXT,
    region_connections_3  TEXT,
    region_connections_4  TEXT,
    region_id             TEXT,
    region_minimum        INTEGER DEFAULT 0,
    series_id             INTEGER DEFAULT 0,
    tier_0                INTEGER DEFAULT 0,
    tier_1                INTEGER DEFAULT 0,
    tier_2                INTEGER DEFAULT 0,
    tier_3                INTEGER DEFAULT 0,
    tier_4                INTEGER DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_atlas_nodes_area_id ON atlas_nodes(area_id);
CREATE INDEX IF NOT EXISTS idx_atlas_nodes_region_id ON atlas_nodes(region_id);
CREATE INDEX IF NOT EXISTS idx_atlas_nodes_series_id ON atlas_nodes(series_id);
