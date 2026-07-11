package com.poe.cache.model;

public class HeistNpcs {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** NPC identifier */
    private String npcId;
    /** NPC job identifier */
    private String jobId;
    /** NPC display name */
    private String name;
    /** NPC stat text */
    private String statText;

    public HeistNpcs() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getNpcId() { return npcId; }
    public void setNpcId(String npcId) { this.npcId = npcId; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatText() { return statText; }
    public void setStatText(String statText) { this.statText = statText; }
}
