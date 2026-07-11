package com.poe.cache.model;

public class HarvestSeeds {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Consumed primal lifeforce percentage */
    private int consumedPrimalLifeforcePercentage;
    /** Consumed vivid lifeforce percentage */
    private int consumedVividLifeforcePercentage;
    /** Consumed wild lifeforce percentage */
    private int consumedWildLifeforcePercentage;
    /** Seed effect text */
    private String effect;
    /** Granted craft option IDs */
    private String grantedCraftOptionIds;
    /** Growth cycles required */
    private int growthCycles;
    /** Required nearby seed amount */
    private int requiredNearbySeedAmount;
    /** Required nearby seed tier */
    private int requiredNearbySeedTier;
    /** Seed tier */
    private int tier;
    /** Seed type */
    private String type;
    /** Seed type ID */
    private int typeId;

    public HarvestSeeds() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public int getConsumedPrimalLifeforcePercentage() { return consumedPrimalLifeforcePercentage; }
    public void setConsumedPrimalLifeforcePercentage(int consumedPrimalLifeforcePercentage) { this.consumedPrimalLifeforcePercentage = consumedPrimalLifeforcePercentage; }
    public int getConsumedVividLifeforcePercentage() { return consumedVividLifeforcePercentage; }
    public void setConsumedVividLifeforcePercentage(int consumedVividLifeforcePercentage) { this.consumedVividLifeforcePercentage = consumedVividLifeforcePercentage; }
    public int getConsumedWildLifeforcePercentage() { return consumedWildLifeforcePercentage; }
    public void setConsumedWildLifeforcePercentage(int consumedWildLifeforcePercentage) { this.consumedWildLifeforcePercentage = consumedWildLifeforcePercentage; }
    public String getEffect() { return effect; }
    public void setEffect(String effect) { this.effect = effect; }
    public String getGrantedCraftOptionIds() { return grantedCraftOptionIds; }
    public void setGrantedCraftOptionIds(String grantedCraftOptionIds) { this.grantedCraftOptionIds = grantedCraftOptionIds; }
    public int getGrowthCycles() { return growthCycles; }
    public void setGrowthCycles(int growthCycles) { this.growthCycles = growthCycles; }
    public int getRequiredNearbySeedAmount() { return requiredNearbySeedAmount; }
    public void setRequiredNearbySeedAmount(int requiredNearbySeedAmount) { this.requiredNearbySeedAmount = requiredNearbySeedAmount; }
    public int getRequiredNearbySeedTier() { return requiredNearbySeedTier; }
    public void setRequiredNearbySeedTier(int requiredNearbySeedTier) { this.requiredNearbySeedTier = requiredNearbySeedTier; }
    public int getTier() { return tier; }
    public void setTier(int tier) { this.tier = tier; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getTypeId() { return typeId; }
    public void setTypeId(int typeId) { this.typeId = typeId; }
}
