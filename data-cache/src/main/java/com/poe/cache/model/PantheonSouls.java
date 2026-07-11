package com.poe.cache.model;

public class PantheonSouls {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Soul identifier */
    private String soulId;
    /** Item identifier */
    private String itemId;
    /** Soul display name */
    private String name;
    /** Ordinal position */
    private int ordinal;
    /** Stat display text */
    private String statText;
    /** Target area identifier */
    private String targetAreaId;
    /** Target monster identifier */
    private String targetMonsterId;

    public PantheonSouls() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getSoulId() { return soulId; }
    public void setSoulId(String soulId) { this.soulId = soulId; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getOrdinal() { return ordinal; }
    public void setOrdinal(int ordinal) { this.ordinal = ordinal; }
    public String getStatText() { return statText; }
    public void setStatText(String statText) { this.statText = statText; }
    public String getTargetAreaId() { return targetAreaId; }
    public void setTargetAreaId(String targetAreaId) { this.targetAreaId = targetAreaId; }
    public String getTargetMonsterId() { return targetMonsterId; }
    public void setTargetMonsterId(String targetMonsterId) { this.targetMonsterId = targetMonsterId; }
}
