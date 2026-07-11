package com.poe.cache.model;

/**
 * 异界地图怪物倍率实体，映射 monster_map_multipliers 表。
 * <p>
 * 按地图等级存储怪物/Boss 的生命/伤害/掉落倍率。
 * 主键为 level（自然键）。
 */
public class MonsterMapMultiplier {

    /** 地图等级（主键） */
    private int level;
    /** Boss 伤害倍率 */
    private int bossDamage;
    /** Boss 物品掉落数量倍率 */
    private int bossItemQuantity;
    /** Boss 物品掉落稀有度倍率 */
    private int bossItemRarity;
    /** Boss 生命倍率 */
    private int bossLife;
    /** 怪物伤害倍率 */
    private int damage;
    /** 怪物生命倍率 */
    private int life;

    public MonsterMapMultiplier() {}

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getBossDamage() { return bossDamage; }
    public void setBossDamage(int bossDamage) { this.bossDamage = bossDamage; }

    public int getBossItemQuantity() { return bossItemQuantity; }
    public void setBossItemQuantity(int bossItemQuantity) { this.bossItemQuantity = bossItemQuantity; }

    public int getBossItemRarity() { return bossItemRarity; }
    public void setBossItemRarity(int bossItemRarity) { this.bossItemRarity = bossItemRarity; }

    public int getBossLife() { return bossLife; }
    public void setBossLife(int bossLife) { this.bossLife = bossLife; }

    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }

    public int getLife() { return life; }
    public void setLife(int life) { this.life = life; }
}
