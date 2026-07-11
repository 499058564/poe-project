-- v013: 杂项与历史数据表

-- 1. versions
CREATE TABLE IF NOT EXISTS versions (
    page_id INTEGER PRIMARY KEY,
    page_name TEXT,
    after TEXT,
    major_part INTEGER,
    minor_part INTEGER,
    patch_part INTEGER,
    previous TEXT,
    release_date TEXT,
    revision_part TEXT,
    version TEXT NOT NULL UNIQUE
);

-- 2. legacy_variants
CREATE TABLE IF NOT EXISTS legacy_variants (
    page_id INTEGER PRIMARY KEY,
    page_name TEXT,
    removal_version TEXT,
    implicit_stat_text TEXT,
    explicit_stat_text TEXT,
    stat_text TEXT,
    base_item TEXT,
    required_level INTEGER
);

-- 3. prophecies
CREATE TABLE IF NOT EXISTS prophecies (
    page_id INTEGER PRIMARY KEY,
    page_name TEXT,
    objective TEXT,
    prediction_text TEXT,
    prophecy_id TEXT,
    reward TEXT,
    seal_cost INTEGER
);

-- 4. quest_rewards
CREATE TABLE IF NOT EXISTS quest_rewards (
    page_id INTEGER PRIMARY KEY,
    page_name TEXT,
    act INTEGER,
    class_ids TEXT,
    classes TEXT,
    item_level INTEGER,
    notes TEXT,
    quest TEXT,
    quest_id TEXT,
    rarity TEXT,
    sockets INTEGER
);

-- 5. spawn_weights
CREATE TABLE IF NOT EXISTS spawn_weights (
    page_id INTEGER NOT NULL,
    page_name TEXT,
    ordinal INTEGER,
    tag TEXT,
    weight INTEGER,
    PRIMARY KEY (page_id, ordinal)
);

-- 6. generic_stats
CREATE TABLE IF NOT EXISTS generic_stats (
    page_id INTEGER NOT NULL,
    page_name TEXT,
    id TEXT,
    name TEXT,
    stat_text TEXT,
    value INTEGER,
    PRIMARY KEY (page_id, id)
);

-- 7. tattoos
CREATE TABLE IF NOT EXISTS tattoos (
    page_id INTEGER PRIMARY KEY,
    page_name TEXT,
    max_adjacent INTEGER,
    min_adjacent INTEGER,
    skill_id TEXT,
    target TEXT,
    tattoo_limit TEXT,
    tribe INTEGER
);

-- 8. tinctures
CREATE TABLE IF NOT EXISTS tinctures (
    page_id INTEGER PRIMARY KEY,
    page_name TEXT,
    cooldown REAL,
    cooldown_html TEXT,
    cooldown_range_average REAL,
    cooldown_range_colour TEXT,
    cooldown_range_maximum REAL,
    cooldown_range_minimum REAL,
    cooldown_range_text TEXT,
    debuff_interval REAL,
    debuff_interval_html TEXT,
    debuff_interval_range_average REAL,
    debuff_interval_range_colour TEXT,
    debuff_interval_range_maximum REAL,
    debuff_interval_range_minimum REAL,
    debuff_interval_range_text TEXT
);

-- 9. sentinels
CREATE TABLE IF NOT EXISTS sentinels (
    page_id INTEGER PRIMARY KEY,
    page_name TEXT,
    charge INTEGER,
    charge_html TEXT,
    charge_range_average REAL,
    charge_range_colour TEXT,
    charge_range_maximum REAL,
    charge_range_minimum REAL,
    charge_range_text TEXT,
    duration INTEGER,
    duration_html TEXT,
    duration_range_average REAL,
    duration_range_colour TEXT,
    duration_range_maximum REAL,
    duration_range_minimum REAL,
    duration_range_text TEXT,
    empowerment INTEGER,
    empowerment_html TEXT,
    empowerment_range_average REAL,
    empowerment_range_colour TEXT,
    empowerment_range_maximum REAL,
    empowerment_range_minimum REAL,
    empowerment_range_text TEXT,
    empowers INTEGER,
    empowers_html TEXT,
    empowers_range_average REAL,
    empowers_range_colour TEXT,
    empowers_range_maximum REAL,
    empowers_range_minimum REAL,
    empowers_range_text TEXT,
    monster TEXT,
    monster_level INTEGER
);

-- 10. idols
CREATE TABLE IF NOT EXISTS idols (
    page_id INTEGER PRIMARY KEY,
    page_name TEXT,
    idol_limit TEXT
);

-- 11. grafts
CREATE TABLE IF NOT EXISTS grafts (
    page_id INTEGER PRIMARY KEY,
    page_name TEXT,
    skill_id TEXT
);

-- 12. corpse_items
CREATE TABLE IF NOT EXISTS corpse_items (
    page_id INTEGER PRIMARY KEY,
    page_name TEXT,
    monster_abilities TEXT,
    monster_category TEXT,
    monster_category_html TEXT,
    tier INTEGER
);

-- 13. cosmetic_items
CREATE TABLE IF NOT EXISTS cosmetic_items (
    page_id INTEGER PRIMARY KEY,
    page_name TEXT,
    cosmetic_type TEXT,
    target TEXT,
    theme TEXT
);

-- 14. hideout_doodads
CREATE TABLE IF NOT EXISTS hideout_doodads (
    page_id INTEGER PRIMARY KEY,
    page_name TEXT,
    is_master_doodad INTEGER,
    variation_count INTEGER
);

-- 15. guides
CREATE TABLE IF NOT EXISTS guides (
    page_id INTEGER PRIMARY KEY,
    page_name TEXT,
    date TEXT,
    subject TEXT,
    version TEXT
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_versions_version ON versions(version);
CREATE INDEX IF NOT EXISTS idx_legacy_variants_base_item ON legacy_variants(base_item);
CREATE INDEX IF NOT EXISTS idx_prophecies_prophecy_id ON prophecies(prophecy_id);
CREATE INDEX IF NOT EXISTS idx_quest_rewards_act ON quest_rewards(act);
CREATE INDEX IF NOT EXISTS idx_quest_rewards_quest_id ON quest_rewards(quest_id);
CREATE INDEX IF NOT EXISTS idx_spawn_weights_tag ON spawn_weights(tag);
CREATE INDEX IF NOT EXISTS idx_generic_stats_id ON generic_stats(id);
CREATE INDEX IF NOT EXISTS idx_tattoos_skill_id ON tattoos(skill_id);
CREATE INDEX IF NOT EXISTS idx_tinctures_page_id ON tinctures(page_id);
CREATE INDEX IF NOT EXISTS idx_sentinels_page_id ON sentinels(page_id);
CREATE INDEX IF NOT EXISTS idx_grafts_skill_id ON grafts(skill_id);
CREATE INDEX IF NOT EXISTS idx_corpse_items_tier ON corpse_items(tier);
CREATE INDEX IF NOT EXISTS idx_cosmetic_items_type ON cosmetic_items(cosmetic_type);
CREATE INDEX IF NOT EXISTS idx_guides_version ON guides(version);
