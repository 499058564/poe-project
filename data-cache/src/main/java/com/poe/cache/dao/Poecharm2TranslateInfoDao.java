package com.poe.cache.dao;

import com.poe.cache.model.Poecharm2TranslateInfo;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * poecharm2_translate_info 数据访问对象（自动生成）。
 */
public class Poecharm2TranslateInfoDao extends BaseDao<Poecharm2TranslateInfo> {

    public Poecharm2TranslateInfoDao(DataSource dataSource) {
        super(dataSource);
    }


    @Override
    protected String tableName() {
        return "poecharm2_translate_info";
    }

    @Override
    protected String insertColumns() {
        return "english_name, chinese_name, cn_csv_file_name, create_time, update_time";
    }

    @Override
    protected void bindInsertParams(PreparedStatement ps, Poecharm2TranslateInfo entity) throws SQLException {
        ps.setString(1, entity.getEnglishName());
        ps.setString(2, entity.getChineseName());
        ps.setString(3, entity.getCnCsvFileName());
        ps.setString(4, entity.getCreateTime());
        ps.setString(5, entity.getUpdateTime());
    }

    @Override
    protected void setEntityId(Poecharm2TranslateInfo entity, int id) {
        entity.setId(id);
    }

    @Override
    protected Poecharm2TranslateInfo mapRow(ResultSet rs) throws SQLException {
        Poecharm2TranslateInfo entity = new Poecharm2TranslateInfo();
        entity.setId(rs.getInt("id"));
        entity.setEnglishName(rs.getString("english_name"));
        entity.setChineseName(rs.getString("chinese_name"));
        entity.setCnCsvFileName(rs.getString("cn_csv_file_name"));
        entity.setCreateTime(rs.getString("create_time"));
        entity.setUpdateTime(rs.getString("update_time"));
        return entity;
    }


    public void update(Poecharm2TranslateInfo entity) throws SQLException {
        String sql = "UPDATE poecharm2_translate_info SET english_name = ?, chinese_name = ?, cn_csv_file_name = ?, create_time = ?, update_time = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getEnglishName());
            ps.setString(2, entity.getChineseName());
            ps.setString(3, entity.getCnCsvFileName());
            ps.setString(4, entity.getCreateTime());
            ps.setString(5, entity.getUpdateTime());
            ps.setInt(6, entity.getId());
            ps.executeUpdate();
        }
    }

}
