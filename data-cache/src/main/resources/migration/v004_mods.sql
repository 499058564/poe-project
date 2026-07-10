-- 词缀表
CREATE TABLE IF NOT EXISTS mods (
    id              INTEGER PRIMARY KEY,
    name            TEXT    NOT NULL,
    name_zh         TEXT,
    mod_type        TEXT,                -- 'prefix' / 'suffix' / 'implicit' / 'enchant'
    domain          TEXT,
    generation_type TEXT,
    mod_group       TEXT,
    stats           TEXT,                -- JSON
    spawn_tags      TEXT,                -- JSON
    spawn_weights   TEXT,                -- JSON
    required_level  INTEGER DEFAULT 0,
    version         TEXT    NOT NULL
);
