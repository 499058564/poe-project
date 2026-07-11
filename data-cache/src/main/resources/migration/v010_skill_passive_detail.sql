-- v010: 技能与天赋详情子表
-- 所有子表通过 page_id / page_name 与父表关联
-- 基于 2026-07-11 Cargo 字段验证结果（Special:CargoTables）

-- ============================
-- 技能详情
-- ============================

-- 1. skills: 技能宝石基本信息（12,390 行）
CREATE TABLE IF NOT EXISTS skills (
    page_id                   INTEGER NOT NULL PRIMARY KEY,
    page_name                 TEXT    NOT NULL,
    active_skill_name         TEXT,
    cast_time                 REAL    DEFAULT 0.0,
    description               TEXT,
    is_support                INTEGER DEFAULT 0,
    item_class_id_restriction TEXT,
    item_class_restriction    TEXT,
    max_level                 INTEGER DEFAULT 0,
    skill_id                  TEXT,
    stat_text                 TEXT
);
CREATE INDEX IF NOT EXISTS idx_skills_skill_id ON skills(skill_id);
CREATE INDEX IF NOT EXISTS idx_skills_is_support ON skills(is_support);

-- 2. skill_levels: 技能各等级数值（85,373 行）
CREATE TABLE IF NOT EXISTS skill_levels (
    page_id                        INTEGER NOT NULL,
    page_name                      TEXT    NOT NULL,
    attack_speed_multiplier        INTEGER DEFAULT 0,
    attack_time                    REAL    DEFAULT 0.0,
    cooldown                       REAL    DEFAULT 0.0,
    cost_amounts                   TEXT,
    cost_multiplier                REAL    DEFAULT 0.0,
    cost_types                     TEXT,
    critical_strike_chance         REAL    DEFAULT 0.0,
    damage_effectiveness           REAL    DEFAULT 0.0,
    damage_multiplier              REAL    DEFAULT 0.0,
    dexterity_requirement          INTEGER DEFAULT 0,
    duration                       REAL    DEFAULT 0.0,
    experience                     INTEGER DEFAULT 0,
    intelligence_requirement       INTEGER DEFAULT 0,
    level                          INTEGER DEFAULT 0,
    level_requirement              INTEGER DEFAULT 0,
    life_reservation_flat          INTEGER DEFAULT 0,
    life_reservation_percent       INTEGER DEFAULT 0,
    mana_reservation_flat          INTEGER DEFAULT 0,
    mana_reservation_percent       INTEGER DEFAULT 0,
    skill_level                    INTEGER DEFAULT 0,
    stat_text                      TEXT,
    stored_uses                    INTEGER DEFAULT 0,
    strength_requirement           INTEGER DEFAULT 0,
    vaal_soul_gain_prevention_time REAL    DEFAULT 0.0,
    vaal_souls_requirement         INTEGER DEFAULT 0,
    vaal_stored_uses               INTEGER DEFAULT 0,
    PRIMARY KEY (page_id, level)
);
CREATE INDEX IF NOT EXISTS idx_skill_levels_page_id ON skill_levels(page_id);
CREATE INDEX IF NOT EXISTS idx_skill_levels_level_req ON skill_levels(level_requirement);

-- 3. skill_stats_per_level: 技能每级属性数值（191,391 行）
CREATE TABLE IF NOT EXISTS skill_stats_per_level (
    page_id   INTEGER NOT NULL,
    page_name TEXT    NOT NULL,
    stat_id   TEXT,
    level     INTEGER DEFAULT 0,
    value     INTEGER DEFAULT 0,
    PRIMARY KEY (page_id, stat_id, level)
);
CREATE INDEX IF NOT EXISTS idx_skill_stats_per_level_page_id ON skill_stats_per_level(page_id);

-- 4. skill_quality: 技能品质效果（834 行）
CREATE TABLE IF NOT EXISTS skill_quality (
    page_id   INTEGER NOT NULL,
    page_name TEXT    NOT NULL,
    set_id    INTEGER DEFAULT 0,
    stat_text TEXT,
    weight    INTEGER DEFAULT 0,
    PRIMARY KEY (page_id, set_id)
);
CREATE INDEX IF NOT EXISTS idx_skill_quality_page_id ON skill_quality(page_id);

-- 5. skill_quality_stats: 技能品质属性数值（848 行）
CREATE TABLE IF NOT EXISTS skill_quality_stats (
    page_id   INTEGER NOT NULL,
    page_name TEXT    NOT NULL,
    stat_id   TEXT,
    set_id    INTEGER DEFAULT 0,
    value     INTEGER DEFAULT 0,
    PRIMARY KEY (page_id, stat_id, set_id)
);
CREATE INDEX IF NOT EXISTS idx_skill_quality_stats_page_id ON skill_quality_stats(page_id);

-- ============================
-- 宝石等级需求
-- ============================

-- 6. gem_levels: 宝石各等级的经验与属性需求（30,671 行）
CREATE TABLE IF NOT EXISTS gem_levels (
    page_id                INTEGER NOT NULL,
    page_name              TEXT    NOT NULL,
    experience             INTEGER DEFAULT 0,
    level                  INTEGER DEFAULT 0,
    required_dexterity     INTEGER DEFAULT 0,
    required_intelligence  INTEGER DEFAULT 0,
    required_level         INTEGER DEFAULT 0,
    required_strength      INTEGER DEFAULT 0,
    PRIMARY KEY (page_id, level)
);
CREATE INDEX IF NOT EXISTS idx_gem_levels_page_id ON gem_levels(page_id);

-- ============================
-- 天赋树详情
-- ============================

-- 7. passive_skill_connections: 天赋节点连接关系（6,717 行）
CREATE TABLE IF NOT EXISTS passive_skill_connections (
    page_id   INTEGER NOT NULL,
    page_name TEXT    NOT NULL,
    node_ids  TEXT,
    tree_id   TEXT,
    PRIMARY KEY (page_id, tree_id)
);
CREATE INDEX IF NOT EXISTS idx_passive_skill_connections_tree ON passive_skill_connections(tree_id);

-- 8. mastery_effects: 专精效果（353 行）
CREATE TABLE IF NOT EXISTS mastery_effects (
    page_id       INTEGER NOT NULL PRIMARY KEY,
    page_name     TEXT    NOT NULL,
    effect_id     TEXT,
    stat_ids      TEXT,
    stat_text     TEXT,
    stat_text_raw TEXT,
    stat_values   TEXT
);
CREATE INDEX IF NOT EXISTS idx_mastery_effects_effect_id ON mastery_effects(effect_id);

-- 9. mastery_groups: 专精分组（61 行）
CREATE TABLE IF NOT EXISTS mastery_groups (
    page_id   INTEGER NOT NULL PRIMARY KEY,
    page_name TEXT    NOT NULL,
    icon      TEXT,
    group_id  TEXT,
    name      TEXT
);
CREATE INDEX IF NOT EXISTS idx_mastery_groups_group_id ON mastery_groups(group_id);

-- ============================
-- 职业与升华
-- ============================

-- 10. character_classes: 基础职业（7 行）
CREATE TABLE IF NOT EXISTS character_classes (
    page_id      INTEGER NOT NULL PRIMARY KEY,
    page_name    TEXT    NOT NULL,
    dexterity    TEXT,
    flavour_text TEXT,
    class_id     INTEGER DEFAULT 0,
    intelligence TEXT,
    name         TEXT,
    str_id       TEXT,
    strength     TEXT
);
CREATE INDEX IF NOT EXISTS idx_character_classes_class_id ON character_classes(class_id);

-- 11. ascendancy_classes: 升华职业（20 行）
CREATE TABLE IF NOT EXISTS ascendancy_classes (
    page_id         INTEGER NOT NULL PRIMARY KEY,
    page_name       TEXT    NOT NULL,
    character_class TEXT,
    character_id    INTEGER DEFAULT 0,
    flavour_text    TEXT,
    ascendancy_id   INTEGER DEFAULT 0,
    name            TEXT
);
CREATE INDEX IF NOT EXISTS idx_ascendancy_classes_ascendancy_id ON ascendancy_classes(ascendancy_id);
CREATE INDEX IF NOT EXISTS idx_ascendancy_classes_character_id ON ascendancy_classes(character_id);
