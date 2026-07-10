-- 技能宝石表（Cargo skill_gems: 16 fields）
-- 数据全量来自 Wiki Cargo API，description/quality_stats/level_stats 由其他表补充
CREATE TABLE IF NOT EXISTS skill_gems (
    id                      INTEGER PRIMARY KEY,
    name                    TEXT    NOT NULL,
    name_zh                 TEXT,
    gem_type                TEXT,               -- 从 gem_tags 推导: active/support
    gem_tags                TEXT,               -- Cargo: gem_tags（逗号分隔）
    primary_attribute       TEXT,               -- Cargo: primary_attribute
    description             TEXT,               -- 来自 skill 系列其他表
    quality_stats           TEXT,               -- 来自 skill_quality 系列表
    level_stats             TEXT,               -- 来自 skill_levels 系列表
    required_level          INTEGER DEFAULT 1,  -- Cargo: max_level
    is_vaal_skill_gem       INTEGER DEFAULT 0,  -- Cargo: is_vaal_skill_gem
    support_gem_letter      TEXT,               -- Cargo: support_gem_letter
    support_gem_letter_html TEXT,               -- Cargo: support_gem_letter_html
    requires_intelligence   INTEGER DEFAULT 0,  -- Cargo: requires_intelligence
    requires_dexterity      INTEGER DEFAULT 0,  -- Cargo: requires_dexterity
    requires_strength       INTEGER DEFAULT 0,  -- Cargo: requires_strength
    awakened_variant_id     TEXT,               -- Cargo: awakened_variant_id
    regular_variant_id      TEXT,               -- Cargo: regular_variant_id
    vaal_variant_id         TEXT,               -- Cargo: vaal_variant_id
    secondary_skill_id      TEXT,               -- Cargo: secondary_skill_id
    ruthless_skill_id       TEXT,               -- Cargo: ruthless_skill_id
    ruthless_secondary_skill_id TEXT,           -- Cargo: ruthless_secondary_skill_id
    version                 TEXT    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_skill_gems_name ON skill_gems(name);
