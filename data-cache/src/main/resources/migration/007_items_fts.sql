-- FTS5 全文搜索（外部内容表，基于 base_items）
CREATE VIRTUAL TABLE IF NOT EXISTS items_fts USING fts5(
    name, name_zh, class,
    content='base_items', content_rowid='id'
);
