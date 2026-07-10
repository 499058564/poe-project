package com.poe.cache.model;

/**
 * 技能宝石实体，映射 skill_gems 表。
 * <p>
 * {@code gemTags}、{@code qualityStats}、{@code levelStats} 以 JSON 字符串存储，
 * 其结构与 PoE Wiki API 返回格式一致。
 */
public class SkillGem {
    /** PoE 内部宝石 ID，主键 */
    private Integer id;
    /** 英文名称 */
    private String name;
    /** 中文译名，可能为 null */
    private String nameZh;
    /** 宝石类型：active（主动）或 support（辅助） */
    private String gemType;
    /** JSON: 宝石标签，如 ["spell", "aoe", "lightning"] */
    private String gemTags;
    /** 主属性：str（力量）/ dex（敏捷）/ int（智力） */
    private String primaryAttribute;
    /** 技能描述文本 */
    private String description;
    /** JSON: 品质加成数据 */
    private String qualityStats;
    /** JSON: 等级成长数据 */
    private String levelStats;
    /** 使用所需的最低角色等级 */
    private int requiredLevel;
    /** 数据所属游戏版本号，如 "3.24" */
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

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
