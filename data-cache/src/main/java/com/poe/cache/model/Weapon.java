package com.poe.cache.model;

/**
 * 武器特有属性，映射 weapons 表。
 * <p>
 * 通过 _pageName 与 base_items 表关联，存储武器专属的伤害、暴击、攻速等字段。
 */
public class Weapon {
    private Integer pageId;
    private String pageName;
    private double attackSpeed;
    private double criticalStrikeChance;
    private double weaponRange;
    private int physicalDamageMin;
    private int physicalDamageMax;
    private int fireDamageMin;
    private int fireDamageMax;
    private int coldDamageMin;
    private int coldDamageMax;
    private int lightningDamageMin;
    private int lightningDamageMax;
    private int chaosDamageMin;
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
