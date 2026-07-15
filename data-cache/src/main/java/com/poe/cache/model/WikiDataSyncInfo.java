package com.poe.cache.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * wiki_data_sync_info — Wiki 数据同步状态表。
 */
public class WikiDataSyncInfo {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 同步状态常量 */
    public static final String STATUS_PENDING = "0";
    public static final String STATUS_SYNCING = "1";
    public static final String STATUS_DONE = "2";

    private Integer id;
    private Integer wikiTableInfoId;
    private String wikiTableName;
    private Integer syncOffset;
    private String syncStatus;
    private Integer recordsSynced;
    private Integer recordsTotal;
    private String createTime;
    private String updateTime;

    public WikiDataSyncInfo() {}

    public WikiDataSyncInfo(Integer wikiTableInfoId, String wikiTableName) {
        this.wikiTableInfoId = wikiTableInfoId;
        this.wikiTableName = wikiTableName;
        this.syncOffset = 0;
        this.syncStatus = STATUS_PENDING;
        this.recordsSynced = 0;
        this.recordsTotal = 0;
        String now = FMT.format(LocalDateTime.now());
        this.createTime = now;
        this.updateTime = now;
    }

    /** 主键 */
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    /** 关联 wiki_table_info.id */
    public Integer getWikiTableInfoId() { return wikiTableInfoId; }
    public void setWikiTableInfoId(Integer wikiTableInfoId) { this.wikiTableInfoId = wikiTableInfoId; }

    /** Wiki 表名 */
    public String getWikiTableName() { return wikiTableName; }
    public void setWikiTableName(String wikiTableName) { this.wikiTableName = wikiTableName; }

    /** 同步偏移量 */
    public Integer getSyncOffset() { return syncOffset; }
    public void setSyncOffset(Integer syncOffset) { this.syncOffset = syncOffset; }

    /** 同步状态：0-未同步 1-同步中 2-同步完成 */
    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }

    /** 已同步记录数 */
    public Integer getRecordsSynced() { return recordsSynced; }
    public void setRecordsSynced(Integer recordsSynced) { this.recordsSynced = recordsSynced; }

    /** 总记录数 */
    public Integer getRecordsTotal() { return recordsTotal; }
    public void setRecordsTotal(Integer recordsTotal) { this.recordsTotal = recordsTotal; }

    /** 创建时间 */
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    /** 更新时间 */
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
}
