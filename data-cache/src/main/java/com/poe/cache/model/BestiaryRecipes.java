package com.poe.cache.model;

public class BestiaryRecipes {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Recipe identifier */
    private String recipeId;
    /** Game mode (e.g., "Standard", "Hardcore") */
    private String gameMode;
    /** Recipe header text */
    private String header;
    /** Recipe notes */
    private String notes;
    /** Recipe subheader text */
    private String subheader;

    public BestiaryRecipes() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getRecipeId() { return recipeId; }
    public void setRecipeId(String recipeId) { this.recipeId = recipeId; }
    public String getGameMode() { return gameMode; }
    public void setGameMode(String gameMode) { this.gameMode = gameMode; }
    public String getHeader() { return header; }
    public void setHeader(String header) { this.header = header; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getSubheader() { return subheader; }
    public void setSubheader(String subheader) { this.subheader = subheader; }
}
