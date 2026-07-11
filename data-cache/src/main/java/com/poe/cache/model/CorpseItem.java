package com.poe.cache.model;

public class CorpseItem {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Monster abilities description */
    private String monsterAbilities;
    /** Monster category */
    private String monsterCategory;
    /** Monster category HTML */
    private String monsterCategoryHtml;
    /** Corpse tier */
    private int tier;

    public CorpseItem() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getMonsterAbilities() { return monsterAbilities; }
    public void setMonsterAbilities(String monsterAbilities) { this.monsterAbilities = monsterAbilities; }
    public String getMonsterCategory() { return monsterCategory; }
    public void setMonsterCategory(String monsterCategory) { this.monsterCategory = monsterCategory; }
    public String getMonsterCategoryHtml() { return monsterCategoryHtml; }
    public void setMonsterCategoryHtml(String monsterCategoryHtml) { this.monsterCategoryHtml = monsterCategoryHtml; }
    public int getTier() { return tier; }
    public void setTier(int tier) { this.tier = tier; }
}
