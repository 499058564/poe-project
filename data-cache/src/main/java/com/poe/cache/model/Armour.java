package com.poe.cache.model;

/**
 * 护甲特有属性，映射 armours 表。
 * <p>
 * 通过 _pageName 与 base_items 表关联，包含护甲、闪避、护盾、结界等防御属性。
 */
public class Armour {
    private Integer pageId;
    private String pageName;
    private int armourMin;
    private int armourMax;
    private int evasionMin;
    private int evasionMax;
    private int energyShieldMin;
    private int energyShieldMax;
    private int wardMin;
    private int wardMax;
    private int movementSpeed;

    public Armour() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public int getArmourMin() { return armourMin; }
    public void setArmourMin(int armourMin) { this.armourMin = armourMin; }

    public int getArmourMax() { return armourMax; }
    public void setArmourMax(int armourMax) { this.armourMax = armourMax; }

    public int getEvasionMin() { return evasionMin; }
    public void setEvasionMin(int evasionMin) { this.evasionMin = evasionMin; }

    public int getEvasionMax() { return evasionMax; }
    public void setEvasionMax(int evasionMax) { this.evasionMax = evasionMax; }

    public int getEnergyShieldMin() { return energyShieldMin; }
    public void setEnergyShieldMin(int energyShieldMin) { this.energyShieldMin = energyShieldMin; }

    public int getEnergyShieldMax() { return energyShieldMax; }
    public void setEnergyShieldMax(int energyShieldMax) { this.energyShieldMax = energyShieldMax; }

    public int getWardMin() { return wardMin; }
    public void setWardMin(int wardMin) { this.wardMin = wardMin; }

    public int getWardMax() { return wardMax; }
    public void setWardMax(int wardMax) { this.wardMax = wardMax; }

    public int getMovementSpeed() { return movementSpeed; }
    public void setMovementSpeed(int movementSpeed) { this.movementSpeed = movementSpeed; }
}
