package com.poe.cache.dao;

import com.poe.cache.model.WikiTableFieldInfo;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * wiki_table_field_info 数据访问对象。
 */
public class WikiTableFieldInfoDao {

    private final DataSource dataSource;

    public WikiTableFieldInfoDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(WikiTableFieldInfo entity) throws SQLException {
        String sql = "INSERT INTO wiki_table_field_info (wiki_table_info_id, wiki_table_name, field_name, field_type, is_list, delimiter, field_desc, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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

    public void batchInsert(List<WikiTableFieldInfo> entities) throws SQLException {
        String sql = "INSERT INTO wiki_table_field_info (wiki_table_info_id, wiki_table_name, field_name, field_type, is_list, delimiter, field_desc, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (WikiTableFieldInfo entity : entities) {
                setParams(ps, entity);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public Optional<WikiTableFieldInfo> findById(int id) throws SQLException {
        String sql = "SELECT * FROM wiki_table_field_info WHERE id = ?";
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
     * 按表信息 ID 查询该表的所有字段。
     */
    public List<WikiTableFieldInfo> findByWikiTableInfoId(int wikiTableInfoId) throws SQLException {
        String sql = "SELECT * FROM wiki_table_field_info WHERE wiki_table_info_id = ? ORDER BY id";
        List<WikiTableFieldInfo> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, wikiTableInfoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    /**
     * 按表名查询该表的所有字段。
     */
    public List<WikiTableFieldInfo> findByWikiTableName(String wikiTableName) throws SQLException {
        String sql = "SELECT * FROM wiki_table_field_info WHERE wiki_table_name = ? ORDER BY id";
        List<WikiTableFieldInfo> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, wikiTableName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    public List<WikiTableFieldInfo> findAll() throws SQLException {
        String sql = "SELECT * FROM wiki_table_field_info ORDER BY id";
        List<WikiTableFieldInfo> result = new ArrayList<>();
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
        String sql = "DELETE FROM wiki_table_field_info WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * 按表信息 ID 批量删除字段。
     */
    public void deleteByWikiTableInfoId(int wikiTableInfoId) throws SQLException {
        String sql = "DELETE FROM wiki_table_field_info WHERE wiki_table_info_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, wikiTableInfoId);
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM wiki_table_field_info";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void setParams(PreparedStatement ps, WikiTableFieldInfo entity) throws SQLException {
        ps.setInt(1, entity.getWikiTableInfoId());
        ps.setString(2, entity.getWikiTableName());
        ps.setString(3, entity.getFieldName());
        ps.setString(4, entity.getFieldType());
        ps.setString(5, entity.getIsList());
        ps.setString(6, entity.getDelimiter());
        ps.setString(7, entity.getFieldDesc());
        ps.setString(8, entity.getCreateTime());
        ps.setString(9, entity.getUpdateTime());
    }

    private WikiTableFieldInfo mapRow(ResultSet rs) throws SQLException {
        WikiTableFieldInfo entity = new WikiTableFieldInfo();
        entity.setId(rs.getInt("id"));
        entity.setWikiTableInfoId(rs.getInt("wiki_table_info_id"));
        entity.setWikiTableName(rs.getString("wiki_table_name"));
        entity.setFieldName(rs.getString("field_name"));
        entity.setFieldType(rs.getString("field_type"));
        entity.setIsList(rs.getString("is_list"));
        entity.setDelimiter(rs.getString("delimiter"));
        entity.setFieldDesc(rs.getString("field_desc"));
        entity.setCreateTime(rs.getString("create_time"));
        entity.setUpdateTime(rs.getString("update_time"));
        return entity;
    }
}
