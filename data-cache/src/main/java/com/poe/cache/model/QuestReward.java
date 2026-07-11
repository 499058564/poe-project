package com.poe.cache.model;

public class QuestReward {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Act number */
    private int act;
    /** Class IDs (comma-separated) */
    private String classIds;
    /** Class names (comma-separated) */
    private String classes;
    /** Item level of the reward */
    private int itemLevel;
    /** Additional notes */
    private String notes;
    /** Quest name */
    private String quest;
    /** Quest identifier */
    private String questId;
    /** Item rarity */
    private String rarity;
    /** Number of sockets */
    private int sockets;

    public QuestReward() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public int getAct() { return act; }
    public void setAct(int act) { this.act = act; }
    public String getClassIds() { return classIds; }
    public void setClassIds(String classIds) { this.classIds = classIds; }
    public String getClasses() { return classes; }
    public void setClasses(String classes) { this.classes = classes; }
    public int getItemLevel() { return itemLevel; }
    public void setItemLevel(int itemLevel) { this.itemLevel = itemLevel; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getQuest() { return quest; }
    public void setQuest(String quest) { this.quest = quest; }
    public String getQuestId() { return questId; }
    public void setQuestId(String questId) { this.questId = questId; }
    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }
    public int getSockets() { return sockets; }
    public void setSockets(int sockets) { this.sockets = sockets; }
}
