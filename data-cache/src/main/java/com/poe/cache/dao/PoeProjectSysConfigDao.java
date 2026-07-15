package com.poe.cache.dao;

import com.poe.cache.model.PoeProjectSysConfig;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * poe_project_sys_config 数据访问对象（自动生成）。
 */
public class PoeProjectSysConfigDao extends BaseDao<PoeProjectSysConfig> {

    public PoeProjectSysConfigDao(DataSource dataSource) {
        super(dataSource);
    }


    @Override
    protected String tableName() {
        return "poe_project_sys_config";
    }

    @Override
    protected String insertColumns() {
        return "config_key, config_value, field_type, date_type_format, number_type_scale, config_desc, create_time, update_time";
    }

    @Override
    protected void bindInsertParams(PreparedStatement ps, PoeProjectSysConfig entity) throws SQLException {
        ps.setString(1, entity.getConfigKey());
        ps.setString(2, entity.getConfigValue());
        ps.setString(3, entity.getFieldType());
        ps.setString(4, entity.getDateTypeFormat());
        ps.setString(5, entity.getNumberTypeScale());
        ps.setString(6, entity.getConfigDesc());
        ps.setString(7, entity.getCreateTime());
        ps.setString(8, entity.getUpdateTime());
    }

    @Override
    protected void setEntityId(PoeProjectSysConfig entity, int id) {
        entity.setId(id);
    }

    @Override
    protected PoeProjectSysConfig mapRow(ResultSet rs) throws SQLException {
        PoeProjectSysConfig entity = new PoeProjectSysConfig();
        entity.setId(rs.getInt("id"));
        entity.setConfigKey(rs.getString("config_key"));
        entity.setConfigValue(rs.getString("config_value"));
        entity.setFieldType(rs.getString("field_type"));
        entity.setDateTypeFormat(rs.getString("date_type_format"));
        entity.setNumberTypeScale(rs.getString("number_type_scale"));
        entity.setConfigDesc(rs.getString("config_desc"));
        entity.setCreateTime(rs.getString("create_time"));
        entity.setUpdateTime(rs.getString("update_time"));
        return entity;
    }


    public void update(PoeProjectSysConfig entity) throws SQLException {
        String sql = "UPDATE poe_project_sys_config SET config_key = ?, config_value = ?, field_type = ?, date_type_format = ?, number_type_scale = ?, config_desc = ?, create_time = ?, update_time = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getConfigKey());
            ps.setString(2, entity.getConfigValue());
            ps.setString(3, entity.getFieldType());
            ps.setString(4, entity.getDateTypeFormat());
            ps.setString(5, entity.getNumberTypeScale());
            ps.setString(6, entity.getConfigDesc());
            ps.setString(7, entity.getCreateTime());
            ps.setString(8, entity.getUpdateTime());
            ps.setInt(9, entity.getId());
            ps.executeUpdate();
        }
    }

}
