package com.poe.cache.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * poecharm2_translate_info — 自动生成的模型类。
 */
public class Poecharm2TranslateInfo {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Integer id;
    private String englishName;
    private String chineseName;
    private String cnCsvFileName;
    private String createTime;
    private String updateTime;

    public Poecharm2TranslateInfo() {}

    public Poecharm2TranslateInfo(String englishName, String chineseName, String cnCsvFileName) {
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

    /** english名称 */
    public String getEnglishName() { return englishName; }
    public void setEnglishName(String englishName) { this.englishName = englishName; }

    /** chinese名称 */
    public String getChineseName() { return chineseName; }
    public void setChineseName(String chineseName) { this.chineseName = chineseName; }

    /** cn_csv_file名称 */
    public String getCnCsvFileName() { return cnCsvFileName; }
    public void setCnCsvFileName(String cnCsvFileName) { this.cnCsvFileName = cnCsvFileName; }

    /** 创建时间 */
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    /** 更新时间 */
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }

}
