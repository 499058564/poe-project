package com.poe.cache.model;

/**
 * 怪物生命倍率实体，映射 monster_life_scaling 表。
 * <p>
 * 按怪物等级存储普通/魔法/稀有怪物的生命倍率。
 * 主键为 level（自然键）。
 */
public class MonsterLifeScaling {

    /** 怪物等级（主键） */
    private int level;
    /** 魔法怪物生命倍率 */
    private int magic;
    /** 稀有怪物生命倍率 */
    private int rare;

    public MonsterLifeScaling() {}

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getMagic() { return magic; }
    public void setMagic(int magic) { this.magic = magic; }

    public int getRare() { return rare; }
    public void setRare(int rare) { this.rare = rare; }
}
