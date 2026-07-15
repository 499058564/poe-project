package com.poe.cache.dao;

import com.poe.cache.model.WikiTableInfo;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * wiki_table_info 数据访问对象。
 */
public class WikiTableInfoDao {

    private final DataSource dataSource;

    public WikiTableInfoDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(WikiTableInfo entity) throws SQLException {
        String sql = "INSERT INTO wiki_table_info (wiki_table_name, table_desc, create_time, update_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entity.getWikiTableName());
            ps.setString(2, entity.getTableDesc());
            ps.setString(3, entity.getCreateTime());
            ps.setString(4, entity.getUpdateTime());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    entity.setId(rs.getInt(1));
                }
            }
        }
    }

    public void batchInsert(List<WikiTableInfo> entities) throws SQLException {
        String sql = "INSERT INTO wiki_table_info (wiki_table_name, table_desc, create_time, update_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (WikiTableInfo entity : entities) {
                ps.setString(1, entity.getWikiTableName());
                ps.setString(2, entity.getTableDesc());
                ps.setString(3, entity.getCreateTime());
                ps.setString(4, entity.getUpdateTime());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public Optional<WikiTableInfo> findById(int id) throws SQLException {
        String sql = "SELECT * FROM wiki_table_info WHERE id = ?";
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

    public Optional<WikiTableInfo> findByWikiTableName(String wikiTableName) throws SQLException {
        String sql = "SELECT * FROM wiki_table_info WHERE wiki_table_name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, wikiTableName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<WikiTableInfo> findAll() throws SQLException {
        String sql = "SELECT * FROM wiki_table_info ORDER BY id";
        List<WikiTableInfo> result = new ArrayList<>();
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
        String sql = "DELETE FROM wiki_table_info WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM wiki_table_info";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public void update(WikiTableInfo entity) throws SQLException {
        String sql = "UPDATE wiki_table_info SET wiki_table_name = ?, table_desc = ?, update_time = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getWikiTableName());
            ps.setString(2, entity.getTableDesc());
            ps.setString(3, entity.getUpdateTime());
            ps.setInt(4, entity.getId());
            ps.executeUpdate();
        }
    }

    private WikiTableInfo mapRow(ResultSet rs) throws SQLException {
        WikiTableInfo entity = new WikiTableInfo();
        entity.setId(rs.getInt("id"));
        entity.setWikiTableName(rs.getString("wiki_table_name"));
        entity.setTableDesc(rs.getString("table_desc"));
        entity.setCreateTime(rs.getString("create_time"));
        entity.setUpdateTime(rs.getString("update_time"));
        return entity;
    }
}
