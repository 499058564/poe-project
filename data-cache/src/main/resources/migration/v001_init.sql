-- v001: 初始表结构（各表定义见同级目录下 001~007 文件）

-- 001_base_items.sql      — 基础物品表
-- 002_skill_gems.sql      — 技能宝石表
-- 003_passive_skills.sql  — 天赋点表
-- 004_mods.sql            — 词缀表
-- 005_data_version.sql    — 数据版本表
-- 006_translations.sql    — 翻译映射表
-- 007_items_fts.sql       — FTS5 全文搜索虚拟表

-- 执行顺序：按编号依次执行，不可打乱（FTS 外部内容表依赖 base_items）。
