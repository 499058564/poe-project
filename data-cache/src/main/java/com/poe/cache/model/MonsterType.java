package com.poe.cache.model;

/**
 * 怪物类型实体，映射 monster_types 表。
 * <p>
 * 存储怪物类型的护甲/护盾/闪避倍率与抗性 ID。
 */
public class MonsterType {

    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 护甲倍率 */
    private int armourMultiplier;
    /** 伤害分布 */
    private double damageSpread;
    /** 能量护盾倍率 */
    private int energyShieldMultiplier;
    /** 闪避倍率 */
    private int evasionMultiplier;
    /** 怪物类型内部 ID */
    private String id;
    /** 关联的抗性配置 ID */
    private String monsterResistanceId;
    /** 标签列表 */
    private String tags;

    public MonsterType() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public int getArmourMultiplier() { return armourMultiplier; }
    public void setArmourMultiplier(int armourMultiplier) { this.armourMultiplier = armourMultiplier; }

    public double getDamageSpread() { return damageSpread; }
    public void setDamageSpread(double damageSpread) { this.damageSpread = damageSpread; }

    public int getEnergyShieldMultiplier() { return energyShieldMultiplier; }
    public void setEnergyShieldMultiplier(int energyShieldMultiplier) { this.energyShieldMultiplier = energyShieldMultiplier; }

    public int getEvasionMultiplier() { return evasionMultiplier; }
    public void setEvasionMultiplier(int evasionMultiplier) { this.evasionMultiplier = evasionMultiplier; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMonsterResistanceId() { return monsterResistanceId; }
    public void setMonsterResistanceId(String monsterResistanceId) { this.monsterResistanceId = monsterResistanceId; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
}
