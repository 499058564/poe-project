package com.poe.cache.model;

public class HarvestPlantBoosters {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Additional crafting options */
    private String additionalCraftingOptions;
    /** Extra chances modifier */
    private String extraChances;
    /** Lifeforce modifier */
    private String lifeforce;
    /** Effect radius */
    private int radius;

    public HarvestPlantBoosters() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getAdditionalCraftingOptions() { return additionalCraftingOptions; }
    public void setAdditionalCraftingOptions(String additionalCraftingOptions) { this.additionalCraftingOptions = additionalCraftingOptions; }
    public String getExtraChances() { return extraChances; }
    public void setExtraChances(String extraChances) { this.extraChances = extraChances; }
    public String getLifeforce() { return lifeforce; }
    public void setLifeforce(String lifeforce) { this.lifeforce = lifeforce; }
    public int getRadius() { return radius; }
    public void setRadius(int radius) { this.radius = radius; }
}
