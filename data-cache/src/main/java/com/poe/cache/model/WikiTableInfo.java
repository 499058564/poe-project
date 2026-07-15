package com.poe.cache.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * wiki_table_info — Wiki 表元信息。
 */
public class WikiTableInfo {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Integer id;
    private String wikiTableName;
    private String tableDesc;
    private String createTime;
    private String updateTime;

    public WikiTableInfo() {}

    public WikiTableInfo(String wikiTableName, String tableDesc) {
        this.wikiTableName = wikiTableName;
        this.tableDesc = tableDesc;
        String now = FMT.format(LocalDateTime.now());
        this.createTime = now;
        this.updateTime = now;
    }

    /** 主键 */
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    /** Wiki 表名 */
    public String getWikiTableName() { return wikiTableName; }
    public void setWikiTableName(String wikiTableName) { this.wikiTableName = wikiTableName; }

    /** 表描述 */
    public String getTableDesc() { return tableDesc; }
    public void setTableDesc(String tableDesc) { this.tableDesc = tableDesc; }

    /** 创建时间 */
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    /** 更新时间 */
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
}
