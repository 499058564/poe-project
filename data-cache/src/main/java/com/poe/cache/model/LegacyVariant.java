package com.poe.cache.model;

public class LegacyVariant {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Version when this variant was removed */
    private String removalVersion;
    /** Implicit stat description */
    private String implicitStatText;
    /** Explicit stat description */
    private String explicitStatText;
    /** Combined stat text */
    private String statText;
    /** Base item identifier */
    private String baseItem;
    /** Required level for this variant */
    private int requiredLevel;

    public LegacyVariant() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getRemovalVersion() { return removalVersion; }
    public void setRemovalVersion(String removalVersion) { this.removalVersion = removalVersion; }
    public String getImplicitStatText() { return implicitStatText; }
    public void setImplicitStatText(String implicitStatText) { this.implicitStatText = implicitStatText; }
    public String getExplicitStatText() { return explicitStatText; }
    public void setExplicitStatText(String explicitStatText) { this.explicitStatText = explicitStatText; }
    public String getStatText() { return statText; }
    public void setStatText(String statText) { this.statText = statText; }
    public String getBaseItem() { return baseItem; }
    public void setBaseItem(String baseItem) { this.baseItem = baseItem; }
    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }
}
