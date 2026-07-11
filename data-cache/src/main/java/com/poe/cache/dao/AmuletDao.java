package com.poe.cache.dao;

import com.poe.cache.model.Amulet;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * amulets 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键，通过 page_name 与 base_items 表关联。
 * 额外字段 is_talisman / talisman_tier 用于标识护身符子类型。
 */
public class AmuletDao implements CrudRepository<Amulet, Integer> {

    private final Connection connection;

    public AmuletDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * 插入单条护身符记录。
     *
     * @param a 护身符实体
     */
    @Override
    public void insert(Amulet a) {
        String sql = "INSERT INTO amulets (page_id, page_name, is_talisman, talisman_tier) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, a);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert amulet: " + a.getPageId(), e);
        }
    }

    /**
     * 批量插入护身符记录（事务）。
     * <p>
     * 使用 JDBC batch + 事务保证原子性，失败自动回滚。
     *
     * @param amulets 护身符实体列表（非空）
     */
    @Override
    public void batchInsert(List<Amulet> amulets) {
        String sql = "INSERT INTO amulets (page_id, page_name, is_talisman, talisman_tier) VALUES (?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Amulet a : amulets) {
                    setParams(ps, a);
                    ps.addBatch();
                }
                ps.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to batch insert amulets", e);
        }
    }

    /**
     * 根据主键查询护身符。
     *
     * @param pageId Wiki 页面 ID
     * @return 护身符实体（可能为空）
     */
    @Override
    public Optional<Amulet> findById(Integer pageId) {
        String sql = "SELECT * FROM amulets WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find amulet: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部护身符记录。
     *
     * @return 按 page_id 升序排列的护身符列表
     */
    @Override
    public List<Amulet> findAll() {
        List<Amulet> list = new ArrayList<>();
        String sql = "SELECT * FROM amulets ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all amulets", e);
        }
        return list;
    }

    /**
     * 根据主键删除护身符。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM amulets WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete amulet: " + pageId, e);
        }
    }

    /**
     * 统计护身符记录总数。
     *
     * @return amulets 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM amulets";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count amulets", e);
        }
        return 0;
    }

    /** 设置 PreparedStatement 参数（绑定 Amulet 字段） */
    private void setParams(PreparedStatement ps, Amulet a) throws SQLException {
        ps.setInt(1, a.getPageId());
        ps.setString(2, a.getPageName());
        ps.setBoolean(3, a.isTalisman());
        ps.setInt(4, a.getTalismanTier());
    }

    /** 从 ResultSet 映射一行到 Amulet 实体 */
    private Amulet mapRow(ResultSet rs) throws SQLException {
        Amulet a = new Amulet();
        a.setPageId(rs.getInt("page_id"));
        a.setPageName(rs.getString("page_name"));
        a.setTalisman(rs.getBoolean("is_talisman"));
        a.setTalismanTier(rs.getInt("talisman_tier"));
        return a;
    }
}
