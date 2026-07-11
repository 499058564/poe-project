package com.poe.cache.model;

public class HideoutDoodad {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Whether this is a master-specific doodad (0/1) */
    private int isMasterDoodad;
    /** Number of visual variations */
    private int variationCount;

    public HideoutDoodad() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public int getIsMasterDoodad() { return isMasterDoodad; }
    public void setIsMasterDoodad(int isMasterDoodad) { this.isMasterDoodad = isMasterDoodad; }
    public int getVariationCount() { return variationCount; }
    public void setVariationCount(int variationCount) { this.variationCount = variationCount; }
}
