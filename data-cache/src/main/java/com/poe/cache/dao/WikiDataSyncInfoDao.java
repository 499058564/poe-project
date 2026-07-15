package com.poe.cache.dao;

import com.poe.cache.model.WikiDataSyncInfo;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * wiki_data_sync_info 数据访问对象（自动生成）。
 */
public class WikiDataSyncInfoDao extends BaseDao<WikiDataSyncInfo> {

    public WikiDataSyncInfoDao(DataSource dataSource) {
        super(dataSource);
    }


    @Override
    protected String tableName() {
        return "wiki_data_sync_info";
    }

    @Override
    protected String insertColumns() {
        return "wiki_table_info_id, wiki_table_name, sync_offset, sync_status, records_synced, records_total, create_time, update_time";
    }

    @Override
    protected void bindInsertParams(PreparedStatement ps, WikiDataSyncInfo entity) throws SQLException {
        ps.setInt(1, entity.getWikiTableInfoId());
        ps.setString(2, entity.getWikiTableName());
        ps.setInt(3, entity.getSyncOffset());
        ps.setString(4, entity.getSyncStatus());
        ps.setInt(5, entity.getRecordsSynced());
        ps.setInt(6, entity.getRecordsTotal());
        ps.setString(7, entity.getCreateTime());
        ps.setString(8, entity.getUpdateTime());
    }

    @Override
    protected void setEntityId(WikiDataSyncInfo entity, int id) {
        entity.setId(id);
    }

    @Override
    protected WikiDataSyncInfo mapRow(ResultSet rs) throws SQLException {
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


    public void update(WikiDataSyncInfo entity) throws SQLException {
        String sql = "UPDATE wiki_data_sync_info SET wiki_table_info_id = ?, wiki_table_name = ?, sync_offset = ?, sync_status = ?, records_synced = ?, records_total = ?, create_time = ?, update_time = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, entity.getWikiTableInfoId());
            ps.setString(2, entity.getWikiTableName());
            ps.setInt(3, entity.getSyncOffset());
            ps.setString(4, entity.getSyncStatus());
            ps.setInt(5, entity.getRecordsSynced());
            ps.setInt(6, entity.getRecordsTotal());
            ps.setString(7, entity.getCreateTime());
            ps.setString(8, entity.getUpdateTime());
            ps.setInt(9, entity.getId());
            ps.executeUpdate();
        }
    }

}
