package com.poe.cache.model;

public class HeistNpcSkills {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Job identifier */
    private String jobId;
    /** Skill level range (e.g., "1-5") */
    private String level;
    /** NPC identifier */
    private String npcId;

    public HeistNpcSkills() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getNpcId() { return npcId; }
    public void setNpcId(String npcId) { this.npcId = npcId; }
}
