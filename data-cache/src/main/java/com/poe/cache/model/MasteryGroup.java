package com.poe.cache.model;

/**
 * 专精组实体，映射 mastery_groups 表。
 * <p>
 * 记录天赋专精的分组信息，包括图标和名称。
 */
public class MasteryGroup {

    /** Wiki 页面 ID */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 图标页面名称 */
    private String icon;
    /** 专精组 ID（Cargo 字段 id） */
    private String groupId;
    /** 专精组名称 */
    private String name;

    public MasteryGroup() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
