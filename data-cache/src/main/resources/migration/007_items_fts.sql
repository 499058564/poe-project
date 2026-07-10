-- FTS5 全文搜索（外部内容表，基于 base_items）
-- 使用 unicode61 分词器以支持 CJK 等 Unicode 字符
CREATE VIRTUAL TABLE IF NOT EXISTS items_fts USING fts5(
    name, name_zh, class,
    content='base_items', content_rowid='id',
    tokenize='unicode61'
);
