package com.poe.cache.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * wiki_table_field_info — Wiki 表字段元信息。
 */
public class WikiTableFieldInfo {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Integer id;
    private Integer wikiTableInfoId;
    private String wikiTableName;
    private String fieldName;
    private String fieldType;
    private String isList;
    private String delimiter;
    private String fieldDesc;
    private String createTime;
    private String updateTime;

    public WikiTableFieldInfo() {}

    public WikiTableFieldInfo(Integer wikiTableInfoId, String wikiTableName,
                               String fieldName, String fieldType) {
        this.wikiTableInfoId = wikiTableInfoId;
        this.wikiTableName = wikiTableName;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
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

    /** 字段名 */
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    /** 字段类型 */
    public String getFieldType() { return fieldType; }
    public void setFieldType(String fieldType) { this.fieldType = fieldType; }

    /** 字段是否为列表 */
    public String getIsList() { return isList; }
    public void setIsList(String isList) { this.isList = isList; }

    /** 分隔符 */
    public String getDelimiter() { return delimiter; }
    public void setDelimiter(String delimiter) { this.delimiter = delimiter; }

    /** 字段描述 */
    public String getFieldDesc() { return fieldDesc; }
    public void setFieldDesc(String fieldDesc) { this.fieldDesc = fieldDesc; }

    /** 创建时间 */
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    /** 更新时间 */
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
}
