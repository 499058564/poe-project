package com.poe.cache.model;

public class HeistAreas {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Heist area identifier */
    private String areaId;
    /** Related area IDs */
    private String areaIds;
    /** Blueprint identifier */
    private String blueprintId;
    /** Contract identifier */
    private String contractId;
    /** Required job IDs */
    private String jobIds;
    /** Reward text description */
    private String rewardText;

    public HeistAreas() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getAreaId() { return areaId; }
    public void setAreaId(String areaId) { this.areaId = areaId; }
    public String getAreaIds() { return areaIds; }
    public void setAreaIds(String areaIds) { this.areaIds = areaIds; }
    public String getBlueprintId() { return blueprintId; }
    public void setBlueprintId(String blueprintId) { this.blueprintId = blueprintId; }
    public String getContractId() { return contractId; }
    public void setContractId(String contractId) { this.contractId = contractId; }
    public String getJobIds() { return jobIds; }
    public void setJobIds(String jobIds) { this.jobIds = jobIds; }
    public String getRewardText() { return rewardText; }
    public void setRewardText(String rewardText) { this.rewardText = rewardText; }
}
