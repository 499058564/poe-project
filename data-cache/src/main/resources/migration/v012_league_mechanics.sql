-- v012: 联盟机制数据（Delve/Heist/Blight/Harvest/Synthesis/Bestiary/Incursion/Pantheon）

-- ==================== Delve ====================

CREATE TABLE IF NOT EXISTS delve_level_scaling (
    depth           INTEGER PRIMARY KEY,
    page_name       TEXT,
    darkness_resistance INTEGER DEFAULT 0,
    light_radius    REAL DEFAULT 0.0,
    monster_damage  REAL DEFAULT 0.0,
    monster_level   INTEGER DEFAULT 0,
    monster_life    REAL DEFAULT 0.0,
    sulphite_cost   INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS delve_resources_per_level (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT,
    area_level  INTEGER DEFAULT 0,
    sulphite    INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS delve_upgrades (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT,
    cost        INTEGER DEFAULT 0,
    level       INTEGER DEFAULT 0,
    type        TEXT
);

CREATE TABLE IF NOT EXISTS delve_upgrade_stats (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT,
    cargo_id    TEXT,
    level       INTEGER DEFAULT 0,
    type        TEXT,
    value       REAL DEFAULT 0.0
);

-- ==================== Heist ====================

CREATE TABLE IF NOT EXISTS heist_areas (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT,
    area_id     TEXT,
    area_ids    TEXT,
    blueprint_id TEXT,
    contract_id TEXT,
    job_ids     TEXT,
    reward_text TEXT
);

CREATE TABLE IF NOT EXISTS heist_jobs (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT,
    job_id      TEXT,
    name        TEXT
);

CREATE TABLE IF NOT EXISTS heist_npcs (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT,
    npc_id      TEXT,
    job_id      TEXT,
    name        TEXT,
    stat_text   TEXT
);

CREATE TABLE IF NOT EXISTS heist_npc_skills (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT,
    job_id      TEXT,
    level       TEXT,
    npc_id      TEXT
);

CREATE TABLE IF NOT EXISTS heist_npc_stats (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT,
    npc_id      TEXT,
    stat_id     TEXT,
    value       REAL DEFAULT 0.0
);

CREATE TABLE IF NOT EXISTS heist_equipment (
    page_id             INTEGER PRIMARY KEY,
    page_name           TEXT,
    required_job_id     TEXT,
    required_job_level  INTEGER DEFAULT 0
);

-- ==================== Blight ====================

CREATE TABLE IF NOT EXISTS blight_crafting_recipes (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT,
    recipe_id   TEXT,
    modifier_id TEXT,
    passive_id  TEXT,
    type        TEXT
);

CREATE TABLE IF NOT EXISTS blight_crafting_recipes_items (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT,
    item_id     TEXT,
    ordinal     INTEGER DEFAULT 0,
    recipe_id   TEXT
);

CREATE TABLE IF NOT EXISTS blight_items (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT,
    tier        TEXT
);

CREATE TABLE IF NOT EXISTS blight_towers (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT,
    cost        INTEGER DEFAULT 0,
    description TEXT,
    icon        TEXT,
    tower_id    TEXT,
    name        TEXT,
    radius      INTEGER DEFAULT 0,
    tier        TEXT
);

-- ==================== Harvest ====================

CREATE TABLE IF NOT EXISTS harvest_crafting_options (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT,
    option_id   TEXT,
    cost_primal INTEGER DEFAULT 0,
    cost_rancour INTEGER DEFAULT 0,
    cost_sacred INTEGER DEFAULT 0,
    cost_vivid  INTEGER DEFAULT 0,
    cost_wild   INTEGER DEFAULT 0,
    effect      TEXT,
    effect_html TEXT,
    ordinal     INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS harvest_plant_boosters (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT,
    additional_crafting_options TEXT,
    extra_chances   TEXT,
    lifeforce       TEXT,
    radius          INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS harvest_seeds (
    page_id                             INTEGER PRIMARY KEY,
    page_name                           TEXT,
    consumed_primal_lifeforce_percentage INTEGER DEFAULT 0,
    consumed_vivid_lifeforce_percentage  INTEGER DEFAULT 0,
    consumed_wild_lifeforce_percentage   INTEGER DEFAULT 0,
    effect                              TEXT,
    granted_craft_option_ids            TEXT,
    growth_cycles                       INTEGER DEFAULT 0,
    required_nearby_seed_amount         INTEGER DEFAULT 0,
    required_nearby_seed_tier           INTEGER DEFAULT 0,
    tier                                INTEGER DEFAULT 0,
    type                                TEXT,
    type_id                             INTEGER DEFAULT 0
);

-- ==================== Synthesis ====================

CREATE TABLE IF NOT EXISTS synthesis_areas (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT,
    area_id     TEXT,
    max_level   INTEGER DEFAULT 0,
    min_level   INTEGER DEFAULT 0,
    name        TEXT,
    size        INTEGER DEFAULT 0,
    weight      INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS synthesis_corrupted_mods (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT,
    item_class_id TEXT,
    mod_ids     TEXT
);

CREATE TABLE IF NOT EXISTS synthesis_global_mods (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT,
    max_level   INTEGER DEFAULT 0,
    min_level   INTEGER DEFAULT 0,
    mod_id      TEXT,
    weight      INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS synthesis_mods (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT,
    item_class_ids  TEXT,
    mod_ids     TEXT,
    stat_id     TEXT,
    stat_text   TEXT,
    stat_value  REAL DEFAULT 0.0
);

-- ==================== Bestiary ====================

CREATE TABLE IF NOT EXISTS bestiary_recipes (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT,
    recipe_id   TEXT,
    game_mode   TEXT,
    header      TEXT,
    notes       TEXT,
    subheader   TEXT
);

CREATE TABLE IF NOT EXISTS bestiary_recipe_components (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT,
    amount      INTEGER DEFAULT 0,
    component_id TEXT,
    recipe_id   TEXT
);

-- ==================== Incursion ====================

CREATE TABLE IF NOT EXISTS incursion_rooms (
    page_id                 INTEGER PRIMARY KEY,
    page_name               TEXT,
    architect_metadata_id   TEXT,
    architect_name          TEXT,
    description             TEXT,
    flavour_text            TEXT,
    icon                    TEXT,
    room_id                 TEXT,
    min_level               INTEGER DEFAULT 0,
    modifier_ids            TEXT,
    name                    TEXT,
    stat_text               TEXT,
    tier                    INTEGER DEFAULT 0,
    upgrade_room_id         TEXT
);

-- ==================== Pantheon ====================

CREATE TABLE IF NOT EXISTS pantheon (
    page_id         INTEGER PRIMARY KEY,
    page_name       TEXT,
    god_name        TEXT,
    is_major_god    INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS pantheon_souls (
    page_id         INTEGER PRIMARY KEY,
    page_name       TEXT,
    soul_id         TEXT,
    item_id         TEXT,
    name            TEXT,
    ordinal         INTEGER DEFAULT 0,
    stat_text       TEXT,
    target_area_id  TEXT,
    target_monster_id TEXT
);

CREATE TABLE IF NOT EXISTS pantheon_stats (
    page_id         INTEGER PRIMARY KEY,
    page_name       TEXT,
    stat_id         TEXT,
    ordinal         INTEGER DEFAULT 0,
    pantheon_id     TEXT,
    pantheon_ordinal INTEGER DEFAULT 0,
    value           TEXT
);

-- ==================== Indexes ====================

CREATE INDEX IF NOT EXISTS idx_delve_level_scaling_depth ON delve_level_scaling(depth);
CREATE INDEX IF NOT EXISTS idx_delve_resources_per_level_area_level ON delve_resources_per_level(area_level);
CREATE INDEX IF NOT EXISTS idx_delve_upgrades_type ON delve_upgrades(type);
CREATE INDEX IF NOT EXISTS idx_delve_upgrade_stats_type ON delve_upgrade_stats(type);
CREATE INDEX IF NOT EXISTS idx_heist_areas_area_id ON heist_areas(area_id);
CREATE INDEX IF NOT EXISTS idx_heist_npcs_job_id ON heist_npcs(job_id);
CREATE INDEX IF NOT EXISTS idx_heist_npc_skills_npc_id ON heist_npc_skills(npc_id);
CREATE INDEX IF NOT EXISTS idx_heist_npc_skills_job_id ON heist_npc_skills(job_id);
CREATE INDEX IF NOT EXISTS idx_heist_npc_stats_npc_id ON heist_npc_stats(npc_id);
CREATE INDEX IF NOT EXISTS idx_heist_equipment_required_job_id ON heist_equipment(required_job_id);
CREATE INDEX IF NOT EXISTS idx_blight_crafting_recipes_type ON blight_crafting_recipes(type);
CREATE INDEX IF NOT EXISTS idx_blight_crafting_recipes_items_recipe_id ON blight_crafting_recipes_items(recipe_id);
CREATE INDEX IF NOT EXISTS idx_blight_towers_tier ON blight_towers(tier);
CREATE INDEX IF NOT EXISTS idx_harvest_crafting_options_option_id ON harvest_crafting_options(option_id);
CREATE INDEX IF NOT EXISTS idx_harvest_seeds_type ON harvest_seeds(type);
CREATE INDEX IF NOT EXISTS idx_synthesis_areas_name ON synthesis_areas(name);
CREATE INDEX IF NOT EXISTS idx_synthesis_global_mods_mod_id ON synthesis_global_mods(mod_id);
CREATE INDEX IF NOT EXISTS idx_synthesis_mods_stat_id ON synthesis_mods(stat_id);
CREATE INDEX IF NOT EXISTS idx_bestiary_recipes_recipe_id ON bestiary_recipes(recipe_id);
CREATE INDEX IF NOT EXISTS idx_bestiary_recipe_components_recipe_id ON bestiary_recipe_components(recipe_id);
CREATE INDEX IF NOT EXISTS idx_incursion_rooms_room_id ON incursion_rooms(room_id);
CREATE INDEX IF NOT EXISTS idx_pantheon_god_name ON pantheon(god_name);
CREATE INDEX IF NOT EXISTS idx_pantheon_souls_name ON pantheon_souls(name);
CREATE INDEX IF NOT EXISTS idx_pantheon_stats_pantheon_id ON pantheon_stats(pantheon_id);
