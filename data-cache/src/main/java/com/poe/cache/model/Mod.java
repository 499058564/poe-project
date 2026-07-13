package com.poe.cache.model;

/**
 * 词缀实体，映射 mods 表。
 * <p>
 * {@code stats}、{@code spawnTags}、{@code spawnWeights} 以 JSON 字符串存储。
 */
public class Mod {
    /** PoE 内部词缀 ID，主键 */
    private Integer id;
    /** 英文名称 */
    private String name;
    /** 中文译名，可能为 null */
    private String nameZh;
    /** 词缀类型：prefix（前缀）/ suffix（后缀）/ implicit（基底）/ enchant（附魔） */
    private String modType;
    /** 适用范围，如 "item"、"area"、"monster" */
    private String domain;
    /** 生成类型，如 "prefix"、"suffix"、"unique" */
    private String generationType;
    /** 词缀组名，用于互斥控制 */
    private String modGroup;
    /** JSON: 词缀效果，如 [{id:"base_maximum_life", min:20, max:30}] */
    private String stats;
    /** JSON: 可生成的物品标签，如 ["ring", "amulet"] */
    private String spawnTags;
    /** JSON: 各标签的生成权重，如 [{tag:"ring", weight:1000}] */
    private String spawnWeights;
    /** 出现所需的最低物品等级 */
    private int requiredLevel;
    /** Cargo: tier_text，词缀层级文本（如 "of the Order"） */
    private String tierText;
    /** Cargo: granted_buff_id，授予的增益 ID */
    private String grantedBuffId;
    /** Cargo: granted_buff_value，授予的增益值 */
    private int grantedBuffValue;
    /** Cargo: granted_skill，授予的技能 */
    private String grantedSkill;
    /** 数据所属游戏版本号，如 "3.24" */
    private String version;

    public Mod() {}

    public Mod(Integer id, String name, String version) {
        this.id = id;
        this.name = name;
        this.version = version;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameZh() { return nameZh; }
    public void setNameZh(String nameZh) { this.nameZh = nameZh; }

    public String getModType() { return modType; }
    public void setModType(String modType) { this.modType = modType; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getGenerationType() { return generationType; }
    public void setGenerationType(String generationType) { this.generationType = generationType; }

    public String getModGroup() { return modGroup; }
    public void setModGroup(String modGroup) { this.modGroup = modGroup; }

    public String getStats() { return stats; }
    public void setStats(String stats) { this.stats = stats; }

    public String getSpawnTags() { return spawnTags; }
    public void setSpawnTags(String spawnTags) { this.spawnTags = spawnTags; }

    public String getSpawnWeights() { return spawnWeights; }
    public void setSpawnWeights(String spawnWeights) { this.spawnWeights = spawnWeights; }

    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }

    public String getTierText() { return tierText; }
    public void setTierText(String tierText) { this.tierText = tierText; }

    public String getGrantedBuffId() { return grantedBuffId; }
    public void setGrantedBuffId(String grantedBuffId) { this.grantedBuffId = grantedBuffId; }

    public int getGrantedBuffValue() { return grantedBuffValue; }
    public void setGrantedBuffValue(int grantedBuffValue) { this.grantedBuffValue = grantedBuffValue; }

    public String getGrantedSkill() { return grantedSkill; }
    public void setGrantedSkill(String grantedSkill) { this.grantedSkill = grantedSkill; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
