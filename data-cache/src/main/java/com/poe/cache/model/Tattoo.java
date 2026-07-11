package com.poe.cache.model;

public class Tattoo {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Maximum adjacent passives required */
    private int maxAdjacent;
    /** Minimum adjacent passives required */
    private int minAdjacent;
    /** Granted skill identifier */
    private String skillId;
    /** Target node type */
    private String target;
    /** Tattoo limit restriction */
    private String tattooLimit;
    /** Tribe affiliation */
    private int tribe;

    public Tattoo() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public int getMaxAdjacent() { return maxAdjacent; }
    public void setMaxAdjacent(int maxAdjacent) { this.maxAdjacent = maxAdjacent; }
    public int getMinAdjacent() { return minAdjacent; }
    public void setMinAdjacent(int minAdjacent) { this.minAdjacent = minAdjacent; }
    public String getSkillId() { return skillId; }
    public void setSkillId(String skillId) { this.skillId = skillId; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getTattooLimit() { return tattooLimit; }
    public void setTattooLimit(String tattooLimit) { this.tattooLimit = tattooLimit; }
    public int getTribe() { return tribe; }
    public void setTribe(int tribe) { this.tribe = tribe; }
}
