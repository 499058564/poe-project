package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.Essence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * essences 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键。
 * 记录精华物品的分类、等级和类型信息。
 */
public class EssenceDao implements CrudRepository<Essence, Integer> {

    private final DataSource dataSource;

    public EssenceDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(Essence entity) {
        String sql = "INSERT INTO essences (page_id, page_name, category, level, " +
            "level_restriction, type) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert essence: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<Essence> entities) {
        String sql = "INSERT INTO essences (page_id, page_name, category, level, " +
            "level_restriction, type) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Essence entity : entities) {
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
            throw new RuntimeException("Failed to batch insert essences", e);
        }
    }

    @Override
    public Optional<Essence> findById(Integer pageId) {
        String sql = "SELECT * FROM essences WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find essence: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Essence> findAll() {
        List<Essence> list = new ArrayList<>();
        String sql = "SELECT * FROM essences ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all essences", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM essences WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete essence: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM essences";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count essences", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Essence e) throws SQLException {
        ps.setInt(1, e.getPageId());
        ps.setString(2, e.getPageName());
        ps.setString(3, e.getCategory());
        ps.setInt(4, e.getLevel());
        ps.setInt(5, e.getLevelRestriction());
        ps.setInt(6, e.getType());
    }

    private Essence mapRow(ResultSet rs) throws SQLException {
        Essence e = new Essence();
        e.setPageId(rs.getInt("page_id"));
        e.setPageName(rs.getString("page_name"));
        e.setCategory(rs.getString("category"));
        e.setLevel(rs.getInt("level"));
        e.setLevelRestriction(rs.getInt("level_restriction"));
        e.setType(rs.getInt("type"));
        return e;
    }
}
