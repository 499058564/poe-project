-- 技能宝石表
CREATE TABLE IF NOT EXISTS skill_gems (
    id                INTEGER PRIMARY KEY,
    name              TEXT    NOT NULL,
    name_zh           TEXT,
    gem_type          TEXT    NOT NULL,   -- 'active' / 'support'
    gem_tags          TEXT,               -- JSON array: ["spell", "aoe", ...]
    primary_attribute TEXT,               -- 'str' / 'dex' / 'int'
    description       TEXT,
    quality_stats     TEXT,               -- JSON
    level_stats       TEXT,               -- JSON
    required_level    INTEGER DEFAULT 1,
    version           TEXT    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_skill_gems_name ON skill_gems(name);
