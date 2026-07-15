package com.poe.cache.dao;

import com.poe.cache.model.WikiDataSyncInfo;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * wiki_data_sync_info 数据访问对象。
 */
public class WikiDataSyncInfoDao {

    private final DataSource dataSource;

    public WikiDataSyncInfoDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(WikiDataSyncInfo entity) throws SQLException {
        String sql = "INSERT INTO wiki_data_sync_info (wiki_table_info_id, wiki_table_name, sync_offset, sync_status, records_synced, records_total, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
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

    public void batchInsert(List<WikiDataSyncInfo> entities) throws SQLException {
        String sql = "INSERT INTO wiki_data_sync_info (wiki_table_info_id, wiki_table_name, sync_offset, sync_status, records_synced, records_total, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (WikiDataSyncInfo entity : entities) {
                setParams(ps, entity);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public Optional<WikiDataSyncInfo> findById(int id) throws SQLException {
        String sql = "SELECT * FROM wiki_data_sync_info WHERE id = ?";
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
     * 按表名查询同步信息。
     */
    public Optional<WikiDataSyncInfo> findByWikiTableName(String wikiTableName) throws SQLException {
        String sql = "SELECT * FROM wiki_data_sync_info WHERE wiki_table_name = ?";
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

    /**
     * 查询同步状态为指定值的记录。
     */
    public List<WikiDataSyncInfo> findBySyncStatus(String syncStatus) throws SQLException {
        String sql = "SELECT * FROM wiki_data_sync_info WHERE sync_status = ? ORDER BY id";
        List<WikiDataSyncInfo> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, syncStatus);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    public List<WikiDataSyncInfo> findAll() throws SQLException {
        String sql = "SELECT * FROM wiki_data_sync_info ORDER BY id";
        List<WikiDataSyncInfo> result = new ArrayList<>();
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
        String sql = "DELETE FROM wiki_data_sync_info WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void update(WikiDataSyncInfo entity) throws SQLException {
        String sql = "UPDATE wiki_data_sync_info SET sync_offset = ?, sync_status = ?, records_synced = ?, records_total = ?, update_time = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, entity.getSyncOffset());
            ps.setString(2, entity.getSyncStatus());
            ps.setInt(3, entity.getRecordsSynced());
            ps.setInt(4, entity.getRecordsTotal());
            ps.setString(5, entity.getUpdateTime());
            ps.setInt(6, entity.getId());
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM wiki_data_sync_info";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void setParams(PreparedStatement ps, WikiDataSyncInfo entity) throws SQLException {
        ps.setInt(1, entity.getWikiTableInfoId());
        ps.setString(2, entity.getWikiTableName());
        ps.setInt(3, entity.getSyncOffset());
        ps.setString(4, entity.getSyncStatus());
        ps.setInt(5, entity.getRecordsSynced());
        ps.setInt(6, entity.getRecordsTotal());
        ps.setString(7, entity.getCreateTime());
        ps.setString(8, entity.getUpdateTime());
    }

    private WikiDataSyncInfo mapRow(ResultSet rs) throws SQLException {
        WikiDataSyncInfo entity = new WikiDataSyncInfo();
        entity.setId(rs.getInt("id"));
        entity.setWikiTableInfoId(rs.getInt("wiki_table_info_id"));
        entity.setWikiTableName(rs.getString("wiki_table_name"));
        entity.setSyncOffset(rs.getInt("sync_offset"));
        entity.setSyncStatus(rs.getString("sync_status"));
        entity.setRecordsSynced(rs.getInt("records_synced"));
        entity.setRecordsTotal(rs.getInt("records_total"));
        entity.setCreateTime(rs.getString("create_time"));
        entity.setUpdateTime(rs.getString("update_time"));
        return entity;
    }
}
