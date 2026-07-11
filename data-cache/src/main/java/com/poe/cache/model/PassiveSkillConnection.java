package com.poe.cache.model;

/**
 * 被动技能连接关系实体，映射 passive_skill_connections 表。
 * <p>
 * 记录天赋树中被动技能节点间的连接关系，nodeIds 以逗号分隔存储。
 */
public class PassiveSkillConnection {

    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 连接的节点 ID 列表（逗号分隔） */
    private String nodeIds;
    /** 天赋树 ID */
    private String treeId;

    public PassiveSkillConnection() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getNodeIds() { return nodeIds; }
    public void setNodeIds(String nodeIds) { this.nodeIds = nodeIds; }

    public String getTreeId() { return treeId; }
    public void setTreeId(String treeId) { this.treeId = treeId; }
}
