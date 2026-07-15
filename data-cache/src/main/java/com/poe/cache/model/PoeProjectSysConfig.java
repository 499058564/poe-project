package com.poe.cache.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * poe_project_sys_config — 自动生成的模型类。
 */
public class PoeProjectSysConfig {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Integer id;
    private String configKey;
    private String configValue;
    private String fieldType;
    private String dateTypeFormat;
    private String numberTypeScale;
    private String configDesc;
    private String createTime;
    private String updateTime;

    public PoeProjectSysConfig() {}

    public PoeProjectSysConfig(String configKey, String configValue, String fieldType) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.fieldType = fieldType;
        String now = FMT.format(LocalDateTime.now());
        this.createTime = now;
        this.updateTime = now;
    }

    /** 主键 */
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    /** config键 */
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }

    /** config值 */
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }

    /** field类型 */
    public String getFieldType() { return fieldType; }
    public void setFieldType(String fieldType) { this.fieldType = fieldType; }

    /** date_type格式 */
    public String getDateTypeFormat() { return dateTypeFormat; }
    public void setDateTypeFormat(String dateTypeFormat) { this.dateTypeFormat = dateTypeFormat; }

    /** number_type小数位 */
    public String getNumberTypeScale() { return numberTypeScale; }
    public void setNumberTypeScale(String numberTypeScale) { this.numberTypeScale = numberTypeScale; }

    /** config描述 */
    public String getConfigDesc() { return configDesc; }
    public void setConfigDesc(String configDesc) { this.configDesc = configDesc; }

    /** 创建时间 */
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    /** 更新时间 */
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }

}
