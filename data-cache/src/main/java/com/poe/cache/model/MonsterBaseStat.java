package com.poe.cache.model;

/**
 * 怪物基础属性实体，映射 monster_base_stats 表。
 * <p>
 * 按怪物等级存储基础命中/护甲/伤害/闪避/经验/生命值。
 * 主键为 level（自然键）。
 */
public class MonsterBaseStat {

    /** 怪物等级（主键） */
    private int level;
    /** 命中值 */
    private int accuracy;
    /** 护甲值 */
    private int armour;
    /** 伤害值 */
    private double damage;
    /** 闪避值 */
    private int evasion;
    /** 经验值 */
    private int experience;
    /** 生命值 */
    private int life;
    /** 召唤物生命值 */
    private int summonLife;

    public MonsterBaseStat() {}

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getAccuracy() { return accuracy; }
    public void setAccuracy(int accuracy) { this.accuracy = accuracy; }

    public int getArmour() { return armour; }
    public void setArmour(int armour) { this.armour = armour; }

    public double getDamage() { return damage; }
    public void setDamage(double damage) { this.damage = damage; }

    public int getEvasion() { return evasion; }
    public void setEvasion(int evasion) { this.evasion = evasion; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public int getLife() { return life; }
    public void setLife(int life) { this.life = life; }

    public int getSummonLife() { return summonLife; }
    public void setSummonLife(int summonLife) { this.summonLife = summonLife; }
}
