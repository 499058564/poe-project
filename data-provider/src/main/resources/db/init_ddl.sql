-- poe wiki表信息
CREATE TABLE IF NOT EXISTS wiki_table_info (
     id               INTEGER PRIMARY KEY -- 'id',
     wiki_table_name             TEXT    NOT NULL -- '表名',
     table_desc         TEXT -- '描叙',
     create_time       TEXT    NOT NULL -- '创建时间',
     update_time       TEXT    NOT NULL -- '更新时间',
);

-- poe wiki表字段信息
CREATE TABLE IF NOT EXISTS wiki_table_field_info (
     id               INTEGER PRIMARY KEY -- 'id',
     wiki_table_info_id          INTEGER NOT NULL -- '表信息id',
     wiki_table_name             TEXT    NOT NULL -- '表名',
     field_name       TEXT    NOT NULL -- '字段名',
     field_type       TEXT    NOT NULL -- '字段类型',
     is_list           TEXT  -- '字段是否为列表',
     delimiter         TEXT  -- '分隔符',
     field_desc        TEXT  -- '字段描叙',
     create_time       TEXT    NOT NULL -- '创建时间',
     update_time       TEXT    NOT NULL -- '创建时间',
);

-- 配置表
CREATE TABLE IF NOT EXISTS poe_project_sys_config (
    id               INTEGER PRIMARY KEY -- 'id',
    config_key        TEXT    NOT NULL -- '配置key',
    config_value        TEXT    NOT NULL -- '配置value',
    field_type       TEXT    NOT NULL -- '配置类型 string-字符串 date-时间 number-数字',
    date_type_format  TEXT  -- '日期类型格式',
    number_type_scale  TEXT  -- '数字类型小数位 为0表示整数',
    config_desc        TEXT -- '配置描叙',
    create_time       TEXT    NOT NULL -- '创建时间',
    update_time       TEXT    NOT NULL -- '更新时间',
);

-- wiki数据同步表
CREATE TABLE IF NOT EXISTS wiki_data_sync_info (
    id               INTEGER PRIMARY KEY -- 'id',
    wiki_table_info_id          INTEGER NOT NULL -- '表信息id',
    wiki_table_name             TEXT    NOT NULL -- '表名',
    sync_offset          INTEGER  NOT NULL -- '同步偏移量',
    sync_status          TEXT  NOT NULL -- '同步状态 0-未同步 1-同步中 2-同步完成',
    records_synced       INTEGER  NOT NULL -- '已同步记录数',
    records_total        INTEGER  NOT NULL -- '总记录数',
    create_time       TEXT    NOT NULL -- '创建时间',
    update_time       TEXT    NOT NULL -- '更新时间',
);

-- poeCharm2翻译表
CREATE TABLE IF NOT EXISTS wiki_data_sync_info (
    id               INTEGER PRIMARY KEY -- 'id',
    english_name      TEXT NOT NULL -- '英文名',
    chinese_name      TEXT NOT NULL -- '中文名',
    cn_csv_file_name     TEXT NOT NULL -- '中文csv文件名',
    create_time       TEXT    NOT NULL -- '创建时间',
    update_time       TEXT    NOT NULL -- '更新时间',
);


