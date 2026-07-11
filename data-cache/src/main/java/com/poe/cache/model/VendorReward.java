package com.poe.cache.model;

/**
 * 商人奖励，映射 vendor_rewards 表。
 * <p>
 * 记录任务完成后可从 NPC 获得的物品奖励信息。
 * 通过 _pageID 与 base_items 表关联。
 */
public class VendorReward {
    /** Wiki 页面 ID，关联 base_items.id */
    private Integer pageId;
    /** Wiki 页面名称（_pageName），即奖励物品名称 */
    private String pageName;
    /** 章节编号 */
    private int act;
    /** 可选该奖励的职业 ID 列表（逗号分隔） */
    private String classIds;
    /** 可选该奖励的职业名称列表（逗号分隔） */
    private String classes;
    /** 提供奖励的 NPC 名称 */
    private String npc;
    /** 任务名称 */
    private String quest;
    /** 任务 ID */
    private String questId;

    public VendorReward() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public int getAct() { return act; }
    public void setAct(int act) { this.act = act; }

    public String getClassIds() { return classIds; }
    public void setClassIds(String classIds) { this.classIds = classIds; }

    public String getClasses() { return classes; }
    public void setClasses(String classes) { this.classes = classes; }

    public String getNpc() { return npc; }
    public void setNpc(String npc) { this.npc = npc; }

    public String getQuest() { return quest; }
    public void setQuest(String quest) { this.quest = quest; }

    public String getQuestId() { return questId; }
    public void setQuestId(String questId) { this.questId = questId; }
}
