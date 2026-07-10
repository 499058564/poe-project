-- 数据版本表 (记录各表最后同步状态)
CREATE TABLE IF NOT EXISTS data_version (
    table_name   TEXT PRIMARY KEY,
    last_sync    TEXT,
    record_count INTEGER DEFAULT 0,
    wiki_version TEXT
);
