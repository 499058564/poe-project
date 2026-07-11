package com.poe.cache.model;

public class DelveLevelScaling {
    /** Primary key - depth level */
    private int depth;
    /** Wiki page name */
    private String pageName;
    /** Darkness resistance at this depth */
    private int darknessResistance;
    /** Light radius at this depth */
    private double lightRadius;
    /** Monster damage multiplier at this depth */
    private double monsterDamage;
    /** Monster level at this depth */
    private int monsterLevel;
    /** Monster life multiplier at this depth */
    private double monsterLife;
    /** Sulphite cost for this depth */
    private int sulphiteCost;

    public DelveLevelScaling() {}

    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public int getDarknessResistance() { return darknessResistance; }
    public void setDarknessResistance(int darknessResistance) { this.darknessResistance = darknessResistance; }
    public double getLightRadius() { return lightRadius; }
    public void setLightRadius(double lightRadius) { this.lightRadius = lightRadius; }
    public double getMonsterDamage() { return monsterDamage; }
    public void setMonsterDamage(double monsterDamage) { this.monsterDamage = monsterDamage; }
    public int getMonsterLevel() { return monsterLevel; }
    public void setMonsterLevel(int monsterLevel) { this.monsterLevel = monsterLevel; }
    public double getMonsterLife() { return monsterLife; }
    public void setMonsterLife(double monsterLife) { this.monsterLife = monsterLife; }
    public int getSulphiteCost() { return sulphiteCost; }
    public void setSulphiteCost(int sulphiteCost) { this.sulphiteCost = sulphiteCost; }
}
