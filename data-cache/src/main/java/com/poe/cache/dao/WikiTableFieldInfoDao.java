package com.poe.cache.dao;

import com.poe.cache.model.WikiTableFieldInfo;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * wiki_table_field_info 数据访问对象（自动生成）。
 */
public class WikiTableFieldInfoDao extends BaseDao<WikiTableFieldInfo> {

    public WikiTableFieldInfoDao(DataSource dataSource) {
        super(dataSource);
    }


    @Override
    protected String tableName() {
        return "wiki_table_field_info";
    }

    @Override
    protected String insertColumns() {
        return "wiki_table_info_id, wiki_table_name, field_name, field_type, is_list, delimiter, field_desc, create_time, update_time";
    }

    @Override
    protected void bindInsertParams(PreparedStatement ps, WikiTableFieldInfo entity) throws SQLException {
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

    @Override
    protected void setEntityId(WikiTableFieldInfo entity, int id) {
        entity.setId(id);
    }

    @Override
    protected WikiTableFieldInfo mapRow(ResultSet rs) throws SQLException {
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


    public void update(WikiTableFieldInfo entity) throws SQLException {
        String sql = "UPDATE wiki_table_field_info SET wiki_table_info_id = ?, wiki_table_name = ?, field_name = ?, field_type = ?, is_list = ?, delimiter = ?, field_desc = ?, create_time = ?, update_time = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, entity.getWikiTableInfoId());
            ps.setString(2, entity.getWikiTableName());
            ps.setString(3, entity.getFieldName());
            ps.setString(4, entity.getFieldType());
            ps.setString(5, entity.getIsList());
            ps.setString(6, entity.getDelimiter());
            ps.setString(7, entity.getFieldDesc());
            ps.setString(8, entity.getCreateTime());
            ps.setString(9, entity.getUpdateTime());
            ps.setInt(10, entity.getId());
            ps.executeUpdate();
        }
    }

}
