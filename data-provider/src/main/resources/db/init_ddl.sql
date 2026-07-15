-- poe wiki表信息
CREATE TABLE IF NOT EXISTS wiki_table_info (
     id               INTEGER PRIMARY KEY,
     wiki_table_name  TEXT    NOT NULL,
     table_desc       TEXT,
     create_time      TEXT    NOT NULL,
     update_time      TEXT    NOT NULL
);

-- poe wiki表字段信息
CREATE TABLE IF NOT EXISTS wiki_table_field_info (
     id                   INTEGER PRIMARY KEY,
     wiki_table_info_id   INTEGER NOT NULL,
     wiki_table_name      TEXT    NOT NULL,
     field_name           TEXT    NOT NULL,
     field_type           TEXT    NOT NULL,
     is_list              TEXT,
     delimiter            TEXT,
     field_desc           TEXT,
     create_time          TEXT    NOT NULL,
     update_time          TEXT    NOT NULL
);

-- 配置表
CREATE TABLE IF NOT EXISTS poe_project_sys_config (
    id                  INTEGER PRIMARY KEY,
    config_key          TEXT    NOT NULL,
    config_value        TEXT    NOT NULL,
    field_type          TEXT    NOT NULL,
    date_type_format    TEXT,
    number_type_scale   TEXT,
    config_desc         TEXT,
    create_time         TEXT    NOT NULL,
    update_time         TEXT    NOT NULL
);

-- wiki数据同步表
CREATE TABLE IF NOT EXISTS wiki_data_sync_info (
    id                   INTEGER PRIMARY KEY,
    wiki_table_info_id   INTEGER NOT NULL,
    wiki_table_name      TEXT    NOT NULL,
    sync_offset          INTEGER NOT NULL,
    sync_status          TEXT    NOT NULL,
    records_synced       INTEGER NOT NULL,
    records_total        INTEGER NOT NULL,
    create_time          TEXT    NOT NULL,
    update_time          TEXT    NOT NULL
);

-- poeCharm2翻译表
CREATE TABLE IF NOT EXISTS poecharm2_translate_info (
    id                  INTEGER PRIMARY KEY,
    english_name        TEXT NOT NULL,
    chinese_name        TEXT NOT NULL,
    cn_csv_file_name    TEXT NOT NULL,
    create_time         TEXT    NOT NULL,
    update_time         TEXT    NOT NULL
);