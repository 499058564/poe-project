package com.poe.cache.model;

public class GenericStat {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Stat identifier */
    private String id;
    /** Display name */
    private String name;
    /** Stat description text (wikitext) */
    private String statText;
    /** Stat value */
    private int value;

    public GenericStat() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatText() { return statText; }
    public void setStatText(String statText) { this.statText = statText; }
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
}
