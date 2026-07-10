-- 翻译映射表 (source + domain 联合主键)
CREATE TABLE IF NOT EXISTS translations (
    source TEXT NOT NULL,
    target TEXT NOT NULL,
    domain TEXT NOT NULL,                -- 'item' / 'skill' / 'passive' / 'mod' / 'map'
    PRIMARY KEY (source, domain)
);
