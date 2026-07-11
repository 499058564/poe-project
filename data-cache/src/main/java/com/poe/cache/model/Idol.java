package com.poe.cache.model;

public class Idol {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Idol limit restriction */
    private String idolLimit;

    public Idol() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getIdolLimit() { return idolLimit; }
    public void setIdolLimit(String idolLimit) { this.idolLimit = idolLimit; }
}
