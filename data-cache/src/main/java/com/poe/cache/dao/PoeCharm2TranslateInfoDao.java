package com.poe.cache.dao;

import com.poe.cache.model.PoeCharm2TranslateInfo;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * poecharm2_translate_info 数据访问对象。
 */
public class PoeCharm2TranslateInfoDao {

    private final DataSource dataSource;

    public PoeCharm2TranslateInfoDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(PoeCharm2TranslateInfo entity) throws SQLException {
        String sql = "INSERT INTO poecharm2_translate_info (english_name, chinese_name, cn_csv_file_name, create_time, update_time) VALUES (?, ?, ?, ?, ?)";
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

    public void batchInsert(List<PoeCharm2TranslateInfo> entities) throws SQLException {
        String sql = "INSERT INTO poecharm2_translate_info (english_name, chinese_name, cn_csv_file_name, create_time, update_time) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (PoeCharm2TranslateInfo entity : entities) {
                setParams(ps, entity);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public Optional<PoeCharm2TranslateInfo> findById(int id) throws SQLException {
        String sql = "SELECT * FROM poecharm2_translate_info WHERE id = ?";
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
     * 按英文名精确查找翻译。
     */
    public Optional<PoeCharm2TranslateInfo> findByEnglishName(String englishName) throws SQLException {
        String sql = "SELECT * FROM poecharm2_translate_info WHERE english_name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, englishName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 按 CSV 文件名查询所有翻译。
     */
    public List<PoeCharm2TranslateInfo> findByCsvFileName(String csvFileName) throws SQLException {
        String sql = "SELECT * FROM poecharm2_translate_info WHERE cn_csv_file_name = ? ORDER BY id";
        List<PoeCharm2TranslateInfo> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, csvFileName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    public List<PoeCharm2TranslateInfo> findAll() throws SQLException {
        String sql = "SELECT * FROM poecharm2_translate_info ORDER BY id";
        List<PoeCharm2TranslateInfo> result = new ArrayList<>();
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
        String sql = "DELETE FROM poecharm2_translate_info WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * 按 CSV 文件名批量删除。
     */
    public void deleteByCsvFileName(String csvFileName) throws SQLException {
        String sql = "DELETE FROM poecharm2_translate_info WHERE cn_csv_file_name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, csvFileName);
            ps.executeUpdate();
        }
    }

    public void update(PoeCharm2TranslateInfo entity) throws SQLException {
        String sql = "UPDATE poecharm2_translate_info SET english_name = ?, chinese_name = ?, cn_csv_file_name = ?, update_time = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getEnglishName());
            ps.setString(2, entity.getChineseName());
            ps.setString(3, entity.getCnCsvFileName());
            ps.setString(4, entity.getUpdateTime());
            ps.setInt(5, entity.getId());
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM poecharm2_translate_info";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void setParams(PreparedStatement ps, PoeCharm2TranslateInfo entity) throws SQLException {
        ps.setString(1, entity.getEnglishName());
        ps.setString(2, entity.getChineseName());
        ps.setString(3, entity.getCnCsvFileName());
        ps.setString(4, entity.getCreateTime());
        ps.setString(5, entity.getUpdateTime());
    }

    private PoeCharm2TranslateInfo mapRow(ResultSet rs) throws SQLException {
        PoeCharm2TranslateInfo entity = new PoeCharm2TranslateInfo();
        entity.setId(rs.getInt("id"));
        entity.setEnglishName(rs.getString("english_name"));
        entity.setChineseName(rs.getString("chinese_name"));
        entity.setCnCsvFileName(rs.getString("cn_csv_file_name"));
        entity.setCreateTime(rs.getString("create_time"));
        entity.setUpdateTime(rs.getString("update_time"));
        return entity;
    }
}
