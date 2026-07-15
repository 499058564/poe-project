package com.poe.cache.dao;

import com.poe.cache.model.WikiTableInfo;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * wiki_table_info 数据访问对象（自动生成）。
 */
public class WikiTableInfoDao extends BaseDao<WikiTableInfo> {

    public WikiTableInfoDao(DataSource dataSource) {
        super(dataSource);
    }


    @Override
    protected String tableName() {
        return "wiki_table_info";
    }

    @Override
    protected String insertColumns() {
        return "wiki_table_name, table_desc, create_time, update_time";
    }

    @Override
    protected void bindInsertParams(PreparedStatement ps, WikiTableInfo entity) throws SQLException {
        ps.setString(1, entity.getWikiTableName());
        ps.setString(2, entity.getTableDesc());
        ps.setString(3, entity.getCreateTime());
        ps.setString(4, entity.getUpdateTime());
    }

    @Override
    protected void setEntityId(WikiTableInfo entity, int id) {
        entity.setId(id);
    }

    @Override
    protected WikiTableInfo mapRow(ResultSet rs) throws SQLException {
        WikiTableInfo entity = new WikiTableInfo();
        entity.setId(rs.getInt("id"));
        entity.setWikiTableName(rs.getString("wiki_table_name"));
        entity.setTableDesc(rs.getString("table_desc"));
        entity.setCreateTime(rs.getString("create_time"));
        entity.setUpdateTime(rs.getString("update_time"));
        return entity;
    }


    public void update(WikiTableInfo entity) throws SQLException {
        String sql = "UPDATE wiki_table_info SET wiki_table_name = ?, table_desc = ?, create_time = ?, update_time = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getWikiTableName());
            ps.setString(2, entity.getTableDesc());
            ps.setString(3, entity.getCreateTime());
            ps.setString(4, entity.getUpdateTime());
            ps.setInt(5, entity.getId());
            ps.executeUpdate();
        }
    }

}
