package com.poe.cache.model;

/**
 * 技能宝石实体，映射 skill_gems 表。
 * <p>
 * {@code gemTags} 以逗号分隔字符串存储，与 Cargo API 返回格式一致。
 * 16 个字段全部来自 Cargo skill_gems 表，description/quality_stats/level_stats 由其他表补充。
 */
public class SkillGem {
    /** PoE 内部宝石 ID，主键 */
    private Integer id;
    /** 技能标识名（Cargo: skill_id），如 "Absolution" */
    private String name;
    /** 中文译名，可能为 null */
    private String nameZh;
    /** 宝石类型：从 gem_tags 推导 (active/support) */
    private String gemType;
    /** Cargo: gem_tags，逗号分隔，如 "Spell,Minion,Lightning" */
    private String gemTags;
    /** Cargo: primary_attribute，'strength'/'dexterity'/'intelligence' */
    private String primaryAttribute;
    /** 技能描述文本（来自 skill 系列其他表） */
    private String description;
    /** JSON: 品质加成数据（来自 skill_quality 系列表） */
    private String qualityStats;
    /** JSON: 等级成长数据（来自 skill_levels 系列表） */
    private String levelStats;
    /** Cargo: max_level，宝石最大等级 */
    private int requiredLevel;
    /** Cargo: is_vaal_skill_gem，是否为瓦尔技能宝石 */
    private boolean vaalSkillGem;
    /** Cargo: support_gem_letter，辅助宝石字母标识（如 "1", "+" ） */
    private String supportGemLetter;
    /** Cargo: support_gem_letter_html，辅助宝石字母 HTML 形式 */
    private String supportGemLetterHtml;
    /** Cargo: requires_intelligence */
    private boolean requiresIntelligence;
    /** Cargo: requires_dexterity */
    private boolean requiresDexterity;
    /** Cargo: requires_strength */
    private boolean requiresStrength;
    /** Cargo: awakened_variant_id */
    private String awakenedVariantId;
    /** Cargo: regular_variant_id */
    private String regularVariantId;
    /** Cargo: vaal_variant_id */
    private String vaalVariantId;
    /** Cargo: secondary_skill_id */
    private String secondarySkillId;
    /** Cargo: ruthless_skill_id */
    private String ruthlessSkillId;
    /** Cargo: ruthless_secondary_skill_id */
    private String ruthlessSecondarySkillId;
    /** 数据所属游戏版本号 */
    private String version;

    public SkillGem() {}

    public SkillGem(Integer id, String name, String gemType, String version) {
        this.id = id;
        this.name = name;
        this.gemType = gemType;
        this.version = version;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameZh() { return nameZh; }
    public void setNameZh(String nameZh) { this.nameZh = nameZh; }

    public String getGemType() { return gemType; }
    public void setGemType(String gemType) { this.gemType = gemType; }

    public String getGemTags() { return gemTags; }
    public void setGemTags(String gemTags) { this.gemTags = gemTags; }

    public String getPrimaryAttribute() { return primaryAttribute; }
    public void setPrimaryAttribute(String primaryAttribute) { this.primaryAttribute = primaryAttribute; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getQualityStats() { return qualityStats; }
    public void setQualityStats(String qualityStats) { this.qualityStats = qualityStats; }

    public String getLevelStats() { return levelStats; }
    public void setLevelStats(String levelStats) { this.levelStats = levelStats; }

    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }

    public boolean isVaalSkillGem() { return vaalSkillGem; }
    public void setVaalSkillGem(boolean vaalSkillGem) { this.vaalSkillGem = vaalSkillGem; }

    public String getSupportGemLetter() { return supportGemLetter; }
    public void setSupportGemLetter(String supportGemLetter) { this.supportGemLetter = supportGemLetter; }

    public String getSupportGemLetterHtml() { return supportGemLetterHtml; }
    public void setSupportGemLetterHtml(String supportGemLetterHtml) { this.supportGemLetterHtml = supportGemLetterHtml; }

    public boolean isRequiresIntelligence() { return requiresIntelligence; }
    public void setRequiresIntelligence(boolean requiresIntelligence) { this.requiresIntelligence = requiresIntelligence; }

    public boolean isRequiresDexterity() { return requiresDexterity; }
    public void setRequiresDexterity(boolean requiresDexterity) { this.requiresDexterity = requiresDexterity; }

    public boolean isRequiresStrength() { return requiresStrength; }
    public void setRequiresStrength(boolean requiresStrength) { this.requiresStrength = requiresStrength; }

    public String getAwakenedVariantId() { return awakenedVariantId; }
    public void setAwakenedVariantId(String awakenedVariantId) { this.awakenedVariantId = awakenedVariantId; }

    public String getRegularVariantId() { return regularVariantId; }
    public void setRegularVariantId(String regularVariantId) { this.regularVariantId = regularVariantId; }

    public String getVaalVariantId() { return vaalVariantId; }
    public void setVaalVariantId(String vaalVariantId) { this.vaalVariantId = vaalVariantId; }

    public String getSecondarySkillId() { return secondarySkillId; }
    public void setSecondarySkillId(String secondarySkillId) { this.secondarySkillId = secondarySkillId; }

    public String getRuthlessSkillId() { return ruthlessSkillId; }
    public void setRuthlessSkillId(String ruthlessSkillId) { this.ruthlessSkillId = ruthlessSkillId; }

    public String getRuthlessSecondarySkillId() { return ruthlessSecondarySkillId; }
    public void setRuthlessSecondarySkillId(String ruthlessSecondarySkillId) { this.ruthlessSecondarySkillId = ruthlessSecondarySkillId; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
