package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.ModStat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * mod_stats 表数据访问对象。
 * <p>
 * 表使用 (page_id, stat_id) 作为复合主键。
 * 记录词缀的属性值（id/min/max），一条词缀可有多条属性。
 */
public class ModStatDao implements CrudRepository<ModStat, Integer> {

    private final DataSource dataSource;

    public ModStatDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 插入单条词缀属性记录。
     *
     * @param entity 词缀属性实体
     */
    @Override
    public void insert(ModStat entity) {
        String sql = "INSERT INTO mod_stats (page_id, page_name, stat_id, min_value, max_value) " +
            "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert mod_stat: " + entity.getPageId(), e);
        }
    }

    /**
     * 批量插入词缀属性（事务）。
     *
     * @param entities 词缀属性实体列表（非空）
     */
    @Override
    public void batchInsert(List<ModStat> entities) {
        String sql = "INSERT INTO mod_stats (page_id, page_name, stat_id, min_value, max_value) " +
            "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (ModStat entity : entities) {
                    setParams(ps, entity);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to batch insert mod_stats", e);
        }
    }

    /**
     * 按 page_id 查询第一条词缀属性。
     *
     * @param pageId Wiki 页面 ID
     * @return 词缀属性实体（可能为空）
     */
    @Override
    public Optional<ModStat> findById(Integer pageId) {
        String sql = "SELECT * FROM mod_stats WHERE page_id = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find mod_stat by id: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部词缀属性记录。
     *
     * @return 按 page_id, stat_id 升序排列的属性列表
     */
    @Override
    public List<ModStat> findAll() {
        List<ModStat> list = new ArrayList<>();
        String sql = "SELECT * FROM mod_stats ORDER BY page_id, stat_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all mod_stats", e);
        }
        return list;
    }

    /**
     * 按 page_id 删除该词缀的所有属性。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM mod_stats WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete mod_stats: " + pageId, e);
        }
    }

    /**
     * 统计词缀属性记录总数。
     *
     * @return mod_stats 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM mod_stats";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count mod_stats", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, ModStat s) throws SQLException {
        ps.setInt(1, s.getPageId());
        ps.setString(2, s.getPageName());
        ps.setString(3, s.getStatId());
        ps.setInt(4, s.getMinValue());
        ps.setInt(5, s.getMaxValue());
    }

    private ModStat mapRow(ResultSet rs) throws SQLException {
        ModStat s = new ModStat();
        s.setPageId(rs.getInt("page_id"));
        s.setPageName(rs.getString("page_name"));
        s.setStatId(rs.getString("stat_id"));
        s.setMinValue(rs.getInt("min_value"));
        s.setMaxValue(rs.getInt("max_value"));
        return s;
    }
}
