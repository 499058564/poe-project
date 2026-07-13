-- v016: 补全 passive_skills 和 mods 表的缺失 Cargo 字段
-- 基于 TABLE_CONFIGS 与 Wiki Cargo 字段对比结果

-- ============================
-- passive_skills 补全 6 个字段
-- ============================

ALTER TABLE passive_skills ADD COLUMN is_multiple_choice INTEGER DEFAULT 0;
ALTER TABLE passive_skills ADD COLUMN is_multiple_choice_option INTEGER DEFAULT 0;
ALTER TABLE passive_skills ADD COLUMN mastery_id TEXT;
ALTER TABLE passive_skills ADD COLUMN flavour_text TEXT;
ALTER TABLE passive_skills ADD COLUMN skill_points INTEGER DEFAULT 0;
ALTER TABLE passive_skills ADD COLUMN buff_id TEXT;

CREATE INDEX IF NOT EXISTS idx_passive_skills_mastery_id ON passive_skills(mastery_id);
CREATE INDEX IF NOT EXISTS idx_passive_skills_buff_id ON passive_skills(buff_id);

-- ============================
-- mods 补全 4 个字段
-- ============================

ALTER TABLE mods ADD COLUMN tier_text TEXT;
ALTER TABLE mods ADD COLUMN granted_buff_id TEXT;
ALTER TABLE mods ADD COLUMN granted_buff_value INTEGER DEFAULT 0;
ALTER TABLE mods ADD COLUMN granted_skill TEXT;

CREATE INDEX IF NOT EXISTS idx_mods_granted_buff_id ON mods(granted_buff_id);
CREATE INDEX IF NOT EXISTS idx_mods_granted_skill ON mods(granted_skill);
