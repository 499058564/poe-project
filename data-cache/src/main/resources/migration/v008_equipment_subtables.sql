-- 装备物品子表（从 Wiki Cargo 子表同步）
-- 每张表通过 page_id / page_name 与 base_items 表关联

-- 1. weapons: 武器伤害/攻速/暴击属性
CREATE TABLE IF NOT EXISTS weapons (
    page_id                 INTEGER PRIMARY KEY,
    page_name               TEXT    NOT NULL,
    attack_speed            REAL    DEFAULT 0,
    critical_strike_chance  REAL    DEFAULT 0,
    weapon_range            REAL    DEFAULT 0,
    physical_damage_min     INTEGER DEFAULT 0,
    physical_damage_max     INTEGER DEFAULT 0,
    fire_damage_min         INTEGER DEFAULT 0,
    fire_damage_max         INTEGER DEFAULT 0,
    cold_damage_min         INTEGER DEFAULT 0,
    cold_damage_max         INTEGER DEFAULT 0,
    lightning_damage_min    INTEGER DEFAULT 0,
    lightning_damage_max    INTEGER DEFAULT 0,
    chaos_damage_min        INTEGER DEFAULT 0,
    chaos_damage_max        INTEGER DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_weapons_page_name ON weapons(page_name);

-- 2. armours: 护甲/闪避/护盾/结界属性
CREATE TABLE IF NOT EXISTS armours (
    page_id            INTEGER PRIMARY KEY,
    page_name          TEXT    NOT NULL,
    armour_min         INTEGER DEFAULT 0,
    armour_max         INTEGER DEFAULT 0,
    evasion_min        INTEGER DEFAULT 0,
    evasion_max        INTEGER DEFAULT 0,
    energy_shield_min  INTEGER DEFAULT 0,
    energy_shield_max  INTEGER DEFAULT 0,
    ward_min           INTEGER DEFAULT 0,
    ward_max           INTEGER DEFAULT 0,
    movement_speed     INTEGER DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_armours_page_name ON armours(page_name);

-- 3. shields: 格挡属性
CREATE TABLE IF NOT EXISTS shields (
    page_id    INTEGER PRIMARY KEY,
    page_name  TEXT    NOT NULL,
    block      INTEGER DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_shields_page_name ON shields(page_name);

-- 4. amulets: 护身符属性
CREATE TABLE IF NOT EXISTS amulets (
    page_id       INTEGER PRIMARY KEY,
    page_name     TEXT    NOT NULL,
    is_talisman   INTEGER DEFAULT 0,
    talisman_tier INTEGER DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_amulets_page_name ON amulets(page_name);

-- 5. flasks: 药剂属性
CREATE TABLE IF NOT EXISTS flasks (
    page_id         INTEGER PRIMARY KEY,
    page_name       TEXT    NOT NULL,
    charges_max     INTEGER DEFAULT 0,
    charges_per_use INTEGER DEFAULT 0,
    duration        REAL    DEFAULT 0,
    life            INTEGER DEFAULT 0,
    mana            INTEGER DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_flasks_page_name ON flasks(page_name);

-- 6. jewels: 珠宝属性
CREATE TABLE IF NOT EXISTS jewels (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT    NOT NULL,
    jewel_limit TEXT,
    radius_html TEXT
);
CREATE INDEX IF NOT EXISTS idx_jewels_page_name ON jewels(page_name);

-- 7. stackables: 通货/可堆叠物品
CREATE TABLE IF NOT EXISTS stackables (
    page_id                INTEGER PRIMARY KEY,
    page_name              TEXT    NOT NULL,
    stack_size             INTEGER DEFAULT 0,
    stack_size_currency_tab INTEGER DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_stackables_page_name ON stackables(page_name);

-- 8. maps: 异界地图
CREATE TABLE IF NOT EXISTS maps (
    page_id                INTEGER PRIMARY KEY,
    page_name              TEXT    NOT NULL,
    area_id                TEXT,
    area_level             INTEGER DEFAULT 0,
    guild_character        TEXT,
    series                 TEXT,
    tier                   INTEGER DEFAULT 0,
    unique_area_id         TEXT,
    unique_area_level      INTEGER DEFAULT 0,
    unique_guild_character TEXT
);
CREATE INDEX IF NOT EXISTS idx_maps_page_name ON maps(page_name);

-- 9. map_fragments: 地图碎片
CREATE TABLE IF NOT EXISTS map_fragments (
    page_id            INTEGER PRIMARY KEY,
    page_name          TEXT    NOT NULL,
    map_fragment_limit INTEGER DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_map_fragments_page_name ON map_fragments(page_name);

-- 10. map_series: 地图系列
CREATE TABLE IF NOT EXISTS map_series (
    page_id   INTEGER PRIMARY KEY,
    page_name TEXT    NOT NULL,
    series_id TEXT,
    name      TEXT,
    ordinal   INTEGER DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_map_series_page_name ON map_series(page_name);

-- 11. divination_cards: 命运卡
CREATE TABLE IF NOT EXISTS divination_cards (
    page_id         INTEGER PRIMARY KEY,
    page_name       TEXT    NOT NULL,
    card_art        TEXT,
    card_background TEXT
);
CREATE INDEX IF NOT EXISTS idx_divination_cards_page_name ON divination_cards(page_name);
