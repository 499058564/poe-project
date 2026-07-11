package com.poe.cache.model;

public class Guide {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Publication date */
    private String date;
    /** Guide subject/description */
    private String subject;
    /** Game version this guide was written for */
    private String version;

    public Guide() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
