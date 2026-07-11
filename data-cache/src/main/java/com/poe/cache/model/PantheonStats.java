package com.poe.cache.model;

public class PantheonStats {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Stat identifier */
    private String statId;
    /** Ordinal position */
    private int ordinal;
    /** Pantheon identifier */
    private String pantheonId;
    /** Pantheon ordinal position */
    private int pantheonOrdinal;
    /** Stat value text */
    private String value;

    public PantheonStats() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getStatId() { return statId; }
    public void setStatId(String statId) { this.statId = statId; }
    public int getOrdinal() { return ordinal; }
    public void setOrdinal(int ordinal) { this.ordinal = ordinal; }
    public String getPantheonId() { return pantheonId; }
    public void setPantheonId(String pantheonId) { this.pantheonId = pantheonId; }
    public int getPantheonOrdinal() { return pantheonOrdinal; }
    public void setPantheonOrdinal(int pantheonOrdinal) { this.pantheonOrdinal = pantheonOrdinal; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
