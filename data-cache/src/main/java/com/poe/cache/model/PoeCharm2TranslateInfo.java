package com.poe.cache.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * poecharm2_translate_info — PoeCharm2 翻译数据表。
 */
public class PoeCharm2TranslateInfo {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Integer id;
    private String englishName;
    private String chineseName;
    private String cnCsvFileName;
    private String createTime;
    private String updateTime;

    public PoeCharm2TranslateInfo() {}

    public PoeCharm2TranslateInfo(String englishName, String chineseName, String cnCsvFileName) {
        this.englishName = englishName;
        this.chineseName = chineseName;
        this.cnCsvFileName = cnCsvFileName;
        String now = FMT.format(LocalDateTime.now());
        this.createTime = now;
        this.updateTime = now;
    }

    /** 主键 */
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    /** 英文名 */
    public String getEnglishName() { return englishName; }
    public void setEnglishName(String englishName) { this.englishName = englishName; }

    /** 中文名 */
    public String getChineseName() { return chineseName; }
    public void setChineseName(String chineseName) { this.chineseName = chineseName; }

    /** 中文 CSV 文件名 */
    public String getCnCsvFileName() { return cnCsvFileName; }
    public void setCnCsvFileName(String cnCsvFileName) { this.cnCsvFileName = cnCsvFileName; }

    /** 创建时间 */
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    /** 更新时间 */
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
}
