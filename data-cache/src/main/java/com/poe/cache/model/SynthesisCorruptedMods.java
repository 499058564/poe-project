package com.poe.cache.model;

public class SynthesisCorruptedMods {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Item class identifier */
    private String itemClassId;
    /** Modifier IDs (comma-separated) */
    private String modIds;

    public SynthesisCorruptedMods() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getItemClassId() { return itemClassId; }
    public void setItemClassId(String itemClassId) { this.itemClassId = itemClassId; }
    public String getModIds() { return modIds; }
    public void setModIds(String modIds) { this.modIds = modIds; }
}
