package com.poe.cache.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * wiki_data_sync_info — 自动生成的模型类。
 */
public class WikiDataSyncInfo {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

    public WikiDataSyncInfo(Integer wikiTableInfoId, String wikiTableName, Integer syncOffset, String syncStatus, Integer recordsSynced, Integer recordsTotal) {
        this.wikiTableInfoId = wikiTableInfoId;
        this.wikiTableName = wikiTableName;
        this.syncOffset = syncOffset;
        this.syncStatus = syncStatus;
        this.recordsSynced = recordsSynced;
        this.recordsTotal = recordsTotal;
        String now = FMT.format(LocalDateTime.now());
        this.createTime = now;
        this.updateTime = now;
    }

    /** 主键 */
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    /** wiki_table_info主键 */
    public Integer getWikiTableInfoId() { return wikiTableInfoId; }
    public void setWikiTableInfoId(Integer wikiTableInfoId) { this.wikiTableInfoId = wikiTableInfoId; }

    /** wiki_table名称 */
    public String getWikiTableName() { return wikiTableName; }
    public void setWikiTableName(String wikiTableName) { this.wikiTableName = wikiTableName; }

    /** sync偏移量 */
    public Integer getSyncOffset() { return syncOffset; }
    public void setSyncOffset(Integer syncOffset) { this.syncOffset = syncOffset; }

    /** sync状态 */
    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }

    /** records已同步 */
    public Integer getRecordsSynced() { return recordsSynced; }
    public void setRecordsSynced(Integer recordsSynced) { this.recordsSynced = recordsSynced; }

    /** records总数 */
    public Integer getRecordsTotal() { return recordsTotal; }
    public void setRecordsTotal(Integer recordsTotal) { this.recordsTotal = recordsTotal; }

    /** 创建时间 */
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    /** 更新时间 */
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }

}
