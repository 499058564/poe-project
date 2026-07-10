-- 基础物品表
CREATE TABLE IF NOT EXISTS base_items (
    id               INTEGER PRIMARY KEY,
    name             TEXT    NOT NULL,
    name_zh          TEXT,
    class            TEXT    NOT NULL,
    inventory_width  INTEGER DEFAULT 0,
    inventory_height INTEGER DEFAULT 0,
    requirements     TEXT,          -- JSON: [{name: "str", value: 100}, ...]
    implicits        TEXT,          -- JSON: [{text: "..."}, ...]
    properties       TEXT,          -- JSON: [{name: "Armour", values: ["100"]}, ...]
    flavour_text     TEXT,
    drop_level       INTEGER DEFAULT 0,
    wiki_url         TEXT,
    version          TEXT    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_base_items_name ON base_items(name);
CREATE INDEX IF NOT EXISTS idx_base_items_class ON base_items(class);
