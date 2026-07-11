package com.poe.cache.dao;

import com.poe.cache.model.Flask;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * flasks 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键，通过 page_name 与 base_items 表关联。
 * 记录药剂的充能、持续时间、生命/魔力恢复属性。
 */
public class FlaskDao implements CrudRepository<Flask, Integer> {

    private final javax.sql.DataSource dataSource;

    public FlaskDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 插入单条药剂记录。
     *
     * @param f 药剂实体
     */
    @Override
    public void insert(Flask f) {
        String sql = "INSERT INTO flasks (page_id, page_name, charges_max, charges_per_use, duration, life, mana) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, f);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert flask: " + f.getPageId(), e);
        }
    }

    /**
     * 批量插入药剂记录（事务）。
     * <p>
     * 使用 JDBC batch + 事务保证原子性，失败自动回滚。
     *
     * @param flasks 药剂实体列表（非空）
     */
    @Override
    public void batchInsert(List<Flask> flasks) {
        String sql = "INSERT INTO flasks (page_id, page_name, charges_max, charges_per_use, duration, life, mana) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Flask f : flasks) {
                    setParams(ps, f);
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
            throw new RuntimeException("Failed to batch insert flasks", e);
        }
    }

    /**
     * 根据主键查询药剂。
     *
     * @param pageId Wiki 页面 ID
     * @return 药剂实体（可能为空）
     */
    @Override
    public Optional<Flask> findById(Integer pageId) {
        String sql = "SELECT * FROM flasks WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find flask: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部药剂记录。
     *
     * @return 按 page_id 升序排列的药剂列表
     */
    @Override
    public List<Flask> findAll() {
        List<Flask> list = new ArrayList<>();
        String sql = "SELECT * FROM flasks ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all flasks", e);
        }
        return list;
    }

    /**
     * 根据主键删除药剂。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM flasks WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete flask: " + pageId, e);
        }
    }

    /**
     * 统计药剂记录总数。
     *
     * @return flasks 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM flasks";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count flasks", e);
        }
        return 0;
    }

    /** 设置 PreparedStatement 参数（绑定 Flask 字段） */
    private void setParams(PreparedStatement ps, Flask f) throws SQLException {
        ps.setInt(1, f.getPageId());
        ps.setString(2, f.getPageName());
        ps.setInt(3, f.getChargesMax());
        ps.setInt(4, f.getChargesPerUse());
        ps.setDouble(5, f.getDuration());
        ps.setInt(6, f.getLife());
        ps.setInt(7, f.getMana());
    }

    /** 从 ResultSet 映射一行到 Flask 实体 */
    private Flask mapRow(ResultSet rs) throws SQLException {
        Flask f = new Flask();
        f.setPageId(rs.getInt("page_id"));
        f.setPageName(rs.getString("page_name"));
        f.setChargesMax(rs.getInt("charges_max"));
        f.setChargesPerUse(rs.getInt("charges_per_use"));
        f.setDuration(rs.getDouble("duration"));
        f.setLife(rs.getInt("life"));
        f.setMana(rs.getInt("mana"));
        return f;
    }
}
