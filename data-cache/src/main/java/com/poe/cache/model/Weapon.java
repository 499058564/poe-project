package com.poe.cache.model;

/**
 * 武器特有属性，映射 weapons 表。
 * <p>
 * 通过 _pageName 与 base_items 表关联，存储武器专属的伤害、暴击、攻速等字段。
 */
public class Weapon {
    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName），关联 base_items */
    private String pageName;
    /** 每秒攻击次数 */
    private double attackSpeed;
    /** 基础暴击率 */
    private double criticalStrikeChance;
    /** 武器攻击范围 */
    private double weaponRange;
    /** 物理伤害下限 */
    private int physicalDamageMin;
    /** 物理伤害上限 */
    private int physicalDamageMax;
    /** 火焰伤害下限 */
    private int fireDamageMin;
    /** 火焰伤害上限 */
    private int fireDamageMax;
    /** 冰霜伤害下限 */
    private int coldDamageMin;
    /** 冰霜伤害上限 */
    private int coldDamageMax;
    /** 闪电伤害下限 */
    private int lightningDamageMin;
    /** 闪电伤害上限 */
    private int lightningDamageMax;
    /** 混沌伤害下限 */
    private int chaosDamageMin;
    /** 混沌伤害上限 */
    private int chaosDamageMax;

    public Weapon() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public double getAttackSpeed() { return attackSpeed; }
    public void setAttackSpeed(double attackSpeed) { this.attackSpeed = attackSpeed; }

    public double getCriticalStrikeChance() { return criticalStrikeChance; }
    public void setCriticalStrikeChance(double criticalStrikeChance) { this.criticalStrikeChance = criticalStrikeChance; }

    public double getWeaponRange() { return weaponRange; }
    public void setWeaponRange(double weaponRange) { this.weaponRange = weaponRange; }

    public int getPhysicalDamageMin() { return physicalDamageMin; }
    public void setPhysicalDamageMin(int physicalDamageMin) { this.physicalDamageMin = physicalDamageMin; }

    public int getPhysicalDamageMax() { return physicalDamageMax; }
    public void setPhysicalDamageMax(int physicalDamageMax) { this.physicalDamageMax = physicalDamageMax; }

    public int getFireDamageMin() { return fireDamageMin; }
    public void setFireDamageMin(int fireDamageMin) { this.fireDamageMin = fireDamageMin; }

    public int getFireDamageMax() { return fireDamageMax; }
    public void setFireDamageMax(int fireDamageMax) { this.fireDamageMax = fireDamageMax; }

    public int getColdDamageMin() { return coldDamageMin; }
    public void setColdDamageMin(int coldDamageMin) { this.coldDamageMin = coldDamageMin; }

    public int getColdDamageMax() { return coldDamageMax; }
    public void setColdDamageMax(int coldDamageMax) { this.coldDamageMax = coldDamageMax; }

    public int getLightningDamageMin() { return lightningDamageMin; }
    public void setLightningDamageMin(int lightningDamageMin) { this.lightningDamageMin = lightningDamageMin; }

    public int getLightningDamageMax() { return lightningDamageMax; }
    public void setLightningDamageMax(int lightningDamageMax) { this.lightningDamageMax = lightningDamageMax; }

    public int getChaosDamageMin() { return chaosDamageMin; }
    public void setChaosDamageMin(int chaosDamageMin) { this.chaosDamageMin = chaosDamageMin; }

    public int getChaosDamageMax() { return chaosDamageMax; }
    public void setChaosDamageMax(int chaosDamageMax) { this.chaosDamageMax = chaosDamageMax; }
}
