package com.poe.cache.model;

public class Prophecy {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Prophecy objective description */
    private String objective;
    /** Prophecy prediction flavour text */
    private String predictionText;
    /** Unique prophecy identifier */
    private String prophecyId;
    /** Prophecy reward description */
    private String reward;
    /** Silver coin cost to seal */
    private int sealCost;

    public Prophecy() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getObjective() { return objective; }
    public void setObjective(String objective) { this.objective = objective; }
    public String getPredictionText() { return predictionText; }
    public void setPredictionText(String predictionText) { this.predictionText = predictionText; }
    public String getProphecyId() { return prophecyId; }
    public void setProphecyId(String prophecyId) { this.prophecyId = prophecyId; }
    public String getReward() { return reward; }
    public void setReward(String reward) { this.reward = reward; }
    public int getSealCost() { return sealCost; }
    public void setSealCost(int sealCost) { this.sealCost = sealCost; }
}
