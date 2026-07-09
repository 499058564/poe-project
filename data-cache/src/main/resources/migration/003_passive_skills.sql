-- 天赋点表
CREATE TABLE IF NOT EXISTS passive_skills (
    id              INTEGER PRIMARY KEY,
    name            TEXT    NOT NULL,
    name_zh         TEXT,
    class           TEXT,
    ascendancy      TEXT,
    stats           TEXT,                -- JSON
    is_keystone     INTEGER DEFAULT 0,
    is_notable      INTEGER DEFAULT 0,
    is_jewel_socket INTEGER DEFAULT 0,
    x               REAL,
    y               REAL,
    connections     TEXT,                -- JSON array of neighbor skill IDs
    version         TEXT    NOT NULL
);
