package com.poe.cache.dao;

import com.poe.cache.model.PoeProjectSysConfig;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * poe_project_sys_config 数据访问对象。
 */
public class PoeProjectSysConfigDao {

    private final DataSource dataSource;

    public PoeProjectSysConfigDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(PoeProjectSysConfig entity) throws SQLException {
        String sql = "INSERT INTO poe_project_sys_config (config_key, config_value, field_type, date_type_format, number_type_scale, config_desc, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParams(ps, entity);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    entity.setId(rs.getInt(1));
                }
            }
        }
    }

    public void batchInsert(List<PoeProjectSysConfig> entities) throws SQLException {
        String sql = "INSERT INTO poe_project_sys_config (config_key, config_value, field_type, date_type_format, number_type_scale, config_desc, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (PoeProjectSysConfig entity : entities) {
                setParams(ps, entity);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public Optional<PoeProjectSysConfig> findById(int id) throws SQLException {
        String sql = "SELECT * FROM poe_project_sys_config WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 按配置键精确查找。
     */
    public Optional<PoeProjectSysConfig> findByConfigKey(String configKey) throws SQLException {
        String sql = "SELECT * FROM poe_project_sys_config WHERE config_key = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, configKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<PoeProjectSysConfig> findAll() throws SQLException {
        String sql = "SELECT * FROM poe_project_sys_config ORDER BY id";
        List<PoeProjectSysConfig> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM poe_project_sys_config WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void update(PoeProjectSysConfig entity) throws SQLException {
        String sql = "UPDATE poe_project_sys_config SET config_key = ?, config_value = ?, field_type = ?, date_type_format = ?, number_type_scale = ?, config_desc = ?, update_time = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getConfigKey());
            ps.setString(2, entity.getConfigValue());
            ps.setString(3, entity.getFieldType());
            ps.setString(4, entity.getDateTypeFormat());
            ps.setString(5, entity.getNumberTypeScale());
            ps.setString(6, entity.getConfigDesc());
            ps.setString(7, entity.getUpdateTime());
            ps.setInt(8, entity.getId());
            ps.executeUpdate();
        }
    }

    /**
     * 按 key 更新 value。
     */
    public void upsertByKey(PoeProjectSysConfig entity) throws SQLException {
        Optional<PoeProjectSysConfig> existing = findByConfigKey(entity.getConfigKey());
        if (existing.isPresent()) {
            entity.setId(existing.get().getId());
            update(entity);
        } else {
            insert(entity);
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM poe_project_sys_config";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void setParams(PreparedStatement ps, PoeProjectSysConfig entity) throws SQLException {
        ps.setString(1, entity.getConfigKey());
        ps.setString(2, entity.getConfigValue());
        ps.setString(3, entity.getFieldType());
        ps.setString(4, entity.getDateTypeFormat());
        ps.setString(5, entity.getNumberTypeScale());
        ps.setString(6, entity.getConfigDesc());
        ps.setString(7, entity.getCreateTime());
        ps.setString(8, entity.getUpdateTime());
    }

    private PoeProjectSysConfig mapRow(ResultSet rs) throws SQLException {
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
}
