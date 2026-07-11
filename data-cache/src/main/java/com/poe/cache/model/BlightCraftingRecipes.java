package com.poe.cache.model;

public class BlightCraftingRecipes {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Recipe identifier */
    private String recipeId;
    /** Modifier identifier */
    private String modifierId;
    /** Passive skill identifier */
    private String passiveId;
    /** Recipe type */
    private String type;

    public BlightCraftingRecipes() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getRecipeId() { return recipeId; }
    public void setRecipeId(String recipeId) { this.recipeId = recipeId; }
    public String getModifierId() { return modifierId; }
    public void setModifierId(String modifierId) { this.modifierId = modifierId; }
    public String getPassiveId() { return passiveId; }
    public void setPassiveId(String passiveId) { this.passiveId = passiveId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
