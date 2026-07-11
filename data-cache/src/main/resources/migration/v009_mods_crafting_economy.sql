-- v009: 词缀子表 + 工艺/配方 + 经济数据
-- 所有子表通过 page_id / page_name 与父表关联
-- 基于 2026-07-11 实际 Cargo 字段验证结果

-- ============================
-- 词缀子表（大表，分批同步）
-- ============================

-- 1. mod_stats: 词缀属性值（5.7 万行）
CREATE TABLE IF NOT EXISTS mod_stats (
    page_id    INTEGER NOT NULL,
    page_name  TEXT    NOT NULL,
    stat_id    TEXT,
    min_value  INTEGER DEFAULT 0,
    max_value  INTEGER DEFAULT 0,
    PRIMARY KEY (page_id, stat_id)
);
CREATE INDEX IF NOT EXISTS idx_mod_stats_page_id ON mod_stats(page_id);

-- 2. mod_spawn_weights: 词缀生成权重（4.9 万行）
CREATE TABLE IF NOT EXISTS mod_spawn_weights (
    page_id    INTEGER NOT NULL,
    page_name  TEXT    NOT NULL,
    ordinal    INTEGER DEFAULT 0,
    tag        TEXT,
    value      INTEGER DEFAULT 0,
    PRIMARY KEY (page_id, ordinal)
);
CREATE INDEX IF NOT EXISTS idx_mod_spawn_weights_page_id ON mod_spawn_weights(page_id);

-- 3. mod_generation_weights: 词缀生成类型权重（3,773 行）
CREATE TABLE IF NOT EXISTS mod_generation_weights (
    page_id    INTEGER NOT NULL,
    page_name  TEXT    NOT NULL,
    ordinal    INTEGER DEFAULT 0,
    tag        TEXT,
    value      INTEGER DEFAULT 0,
    PRIMARY KEY (page_id, ordinal)
);
CREATE INDEX IF NOT EXISTS idx_mod_generation_weights_page_id ON mod_generation_weights(page_id);

-- 4. mod_sell_prices: 词缀出售价格（2.4 万行）
CREATE TABLE IF NOT EXISTS mod_sell_prices (
    page_id       INTEGER NOT NULL,
    page_name     TEXT    NOT NULL,
    amount        INTEGER DEFAULT 0,
    currency_name TEXT,
    PRIMARY KEY (page_id, currency_name)
);
CREATE INDEX IF NOT EXISTS idx_mod_sell_prices_page_id ON mod_sell_prices(page_id);

-- ============================
-- 物品-词缀关联
-- ============================

-- 5. item_mods: 物品关联词缀（1 万行）
CREATE TABLE IF NOT EXISTS item_mods (
    page_id               INTEGER NOT NULL,
    page_name             TEXT    NOT NULL,
    mod_id                TEXT,
    is_explicit           INTEGER DEFAULT 0,
    is_implicit              INTEGER DEFAULT 0,
    is_map_fragment_bonus INTEGER DEFAULT 0,
    is_random             INTEGER DEFAULT 0,
    text                  TEXT,
    PRIMARY KEY (page_id, mod_id)
);
CREATE INDEX IF NOT EXISTS idx_item_mods_page_id ON item_mods(page_id);
CREATE INDEX IF NOT EXISTS idx_item_mods_mod_id ON item_mods(mod_id);

-- 6. item_stats: 物品固定属性（1.2 万行）
CREATE TABLE IF NOT EXISTS item_stats (
    page_id    INTEGER NOT NULL,
    page_name  TEXT    NOT NULL,
    avg        INTEGER DEFAULT 0,
    stat_id    TEXT,
    max_value  INTEGER DEFAULT 0,
    min_value  INTEGER DEFAULT 0,
    mod_id     TEXT,
    PRIMARY KEY (page_id, stat_id)
);
CREATE INDEX IF NOT EXISTS idx_item_stats_page_id ON item_stats(page_id);

-- 7. item_buffs: 物品 Buff 效果（61 行）
CREATE TABLE IF NOT EXISTS item_buffs (
    page_id     INTEGER PRIMARY KEY,
    page_name   TEXT    NOT NULL,
    buff_values TEXT,
    icon        TEXT,
    buff_id     TEXT,
    stat_text   TEXT
);
CREATE INDEX IF NOT EXISTS idx_item_buffs_page_name ON item_buffs(page_name);

-- ============================
-- 工艺/配方
-- ============================

-- 8. crafting_bench_options: 工艺台选项（838 行）
CREATE TABLE IF NOT EXISTS crafting_bench_options (
    page_id                          INTEGER,
    page_name                        TEXT    NOT NULL,
    option_id                        INTEGER PRIMARY KEY,
    name                             TEXT,
    affix_type                       TEXT,
    mod_id                           TEXT,
    mod_group                        TEXT,
    rank                             INTEGER DEFAULT 0,
    required_level                   INTEGER DEFAULT 0,
    npc                              TEXT,
    description                      TEXT,
    recipe_unlock_location           TEXT,
    unlock_category                  TEXT,
    unlock_category_description      TEXT,
    item_class_categories            TEXT,
    item_classes                     TEXT,
    item_classes_ids                 TEXT,
    links                            INTEGER DEFAULT 0,
    ordinal                          INTEGER DEFAULT 0,
    socket_colours                   TEXT,
    sockets                          INTEGER DEFAULT 0,
    unveils_required                 INTEGER DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_crafting_bench_options_page_id ON crafting_bench_options(page_id);
CREATE INDEX IF NOT EXISTS idx_crafting_bench_options_name ON crafting_bench_options(name);

-- 9. crafting_bench_options_costs: 工艺消耗（812 行）
CREATE TABLE IF NOT EXISTS crafting_bench_options_costs (
    page_id       INTEGER,
    page_name     TEXT    NOT NULL,
    option_id     INTEGER NOT NULL,
    amount        INTEGER DEFAULT 0,
    currency_name TEXT,
    PRIMARY KEY (option_id, currency_name)
);
CREATE INDEX IF NOT EXISTS idx_crafting_bench_options_costs_option_id ON crafting_bench_options_costs(option_id);

-- 10. essences: 精华（106 行）
CREATE TABLE IF NOT EXISTS essences (
    page_id           INTEGER PRIMARY KEY,
    page_name         TEXT    NOT NULL,
    category          TEXT,
    level             INTEGER DEFAULT 0,
    level_restriction INTEGER DEFAULT 0,
    type              INTEGER DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_essences_page_name ON essences(page_name);

-- 11. fossils: 化石（25 行）
CREATE TABLE IF NOT EXISTS fossils (
    page_id                 INTEGER PRIMARY KEY,
    page_name               TEXT    NOT NULL,
    added_modifier_ids      TEXT,
    allowed_tags            TEXT,
    base_item_id            TEXT,
    can_enchant             INTEGER DEFAULT 0,
    can_mirror              INTEGER DEFAULT 0,
    can_quality             INTEGER DEFAULT 0,
    can_roll_white_sockets  INTEGER DEFAULT 0,
    corrupted_essence_chance INTEGER DEFAULT 0,
    forbidden_tags          TEXT,
    forced_modifier_ids     TEXT,
    is_lucky                INTEGER DEFAULT 0,
    sell_price_modifier_ids TEXT
);
CREATE INDEX IF NOT EXISTS idx_fossils_page_name ON fossils(page_name);

-- 12. fossil_weights: 化石权重（37 行）
CREATE TABLE IF NOT EXISTS fossil_weights (
    page_id       INTEGER NOT NULL,
    page_name     TEXT    NOT NULL,
    base_item_id  TEXT,
    ordinal       INTEGER DEFAULT 0,
    tag           TEXT,
    weight_type   TEXT,
    weight        INTEGER DEFAULT 0,
    PRIMARY KEY (base_item_id, tag)
);
CREATE INDEX IF NOT EXISTS idx_fossil_weights_base_item_id ON fossil_weights(base_item_id);

-- ============================
-- 经济数据
-- ============================

-- 13. vendor_rewards: 商人奖励（1,303 行）
CREATE TABLE IF NOT EXISTS vendor_rewards (
    page_id    INTEGER NOT NULL,
    page_name  TEXT    NOT NULL,
    act        INTEGER DEFAULT 0,
    class_ids  TEXT,
    classes    TEXT,
    npc        TEXT,
    quest      TEXT,
    quest_id   TEXT,
    PRIMARY KEY (page_id, quest_id, npc)
);
CREATE INDEX IF NOT EXISTS idx_vendor_rewards_page_id ON vendor_rewards(page_id);

-- 14. item_sell_prices: 物品出售价格（2,602 行）
CREATE TABLE IF NOT EXISTS item_sell_prices (
    page_id       INTEGER NOT NULL,
    page_name     TEXT    NOT NULL,
    amount        INTEGER DEFAULT 0,
    currency_name TEXT,
    PRIMARY KEY (page_id, currency_name)
);
CREATE INDEX IF NOT EXISTS idx_item_sell_prices_page_id ON item_sell_prices(page_id);

-- 15. item_purchase_costs: 物品购买成本（930 行）
CREATE TABLE IF NOT EXISTS item_purchase_costs (
    page_id       INTEGER NOT NULL,
    page_name     TEXT    NOT NULL,
    amount        INTEGER DEFAULT 0,
    currency_name TEXT,
    rarity        TEXT,
    PRIMARY KEY (page_id, currency_name, rarity)
);
CREATE INDEX IF NOT EXISTS idx_item_purchase_costs_page_id ON item_purchase_costs(page_id);
