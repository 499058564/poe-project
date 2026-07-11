package com.poe.cache.dao;

import com.poe.cache.model.Jewel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * jewels 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键，通过 page_name 与 base_items 表关联。
 * 记录珠宝的限制数量和影响范围 HTML。
 */
public class JewelDao implements CrudRepository<Jewel, Integer> {

    private final javax.sql.DataSource dataSource;

    public JewelDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 插入单条珠宝记录。
     *
     * @param j 珠宝实体
     */
    @Override
    public void insert(Jewel j) {
        String sql = "INSERT INTO jewels (page_id, page_name, jewel_limit, radius_html) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, j);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert jewel: " + j.getPageId(), e);
        }
    }

    /**
     * 批量插入珠宝记录（事务）。
     * <p>
     * 使用 JDBC batch + 事务保证原子性，失败自动回滚。
     *
     * @param jewels 珠宝实体列表（非空）
     */
    @Override
    public void batchInsert(List<Jewel> jewels) {
        String sql = "INSERT INTO jewels (page_id, page_name, jewel_limit, radius_html) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Jewel j : jewels) {
                    setParams(ps, j);
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
            throw new RuntimeException("Failed to batch insert jewels", e);
        }
    }

    /**
     * 根据主键查询珠宝。
     *
     * @param pageId Wiki 页面 ID
     * @return 珠宝实体（可能为空）
     */
    @Override
    public Optional<Jewel> findById(Integer pageId) {
        String sql = "SELECT * FROM jewels WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find jewel: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部珠宝记录。
     *
     * @return 按 page_id 升序排列的珠宝列表
     */
    @Override
    public List<Jewel> findAll() {
        List<Jewel> list = new ArrayList<>();
        String sql = "SELECT * FROM jewels ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all jewels", e);
        }
        return list;
    }

    /**
     * 根据主键删除珠宝。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM jewels WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete jewel: " + pageId, e);
        }
    }

    /**
     * 统计珠宝记录总数。
     *
     * @return jewels 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM jewels";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count jewels", e);
        }
        return 0;
    }

    /** 设置 PreparedStatement 参数（绑定 Jewel 字段） */
    private void setParams(PreparedStatement ps, Jewel j) throws SQLException {
        ps.setInt(1, j.getPageId());
        ps.setString(2, j.getPageName());
        ps.setString(3, j.getJewelLimit());
        ps.setString(4, j.getRadiusHtml());
    }

    /** 从 ResultSet 映射一行到 Jewel 实体 */
    private Jewel mapRow(ResultSet rs) throws SQLException {
        Jewel j = new Jewel();
        j.setPageId(rs.getInt("page_id"));
        j.setPageName(rs.getString("page_name"));
        j.setJewelLimit(rs.getString("jewel_limit"));
        j.setRadiusHtml(rs.getString("radius_html"));
        return j;
    }
}
