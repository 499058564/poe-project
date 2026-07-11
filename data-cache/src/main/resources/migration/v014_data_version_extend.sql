-- data_version 表扩展：支持多数据源版本记录
-- v014: 增加 source / source_version 列，用于追踪各表数据来源与版本

ALTER TABLE data_version ADD COLUMN source TEXT DEFAULT 'wiki';
ALTER TABLE data_version ADD COLUMN source_version TEXT;
