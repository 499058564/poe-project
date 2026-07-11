package com.poe.cache.dao;

import com.poe.cache.model.CosmeticItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CosmeticItemDao implements CrudRepository<CosmeticItem, Integer> {

    private final javax.sql.DataSource dataSource;

    public CosmeticItemDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(CosmeticItem entity) {
        String sql = "INSERT INTO cosmetic_items (page_id, page_name, cosmetic_type, target, theme) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert cosmetic_items: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<CosmeticItem> entities) {
        String sql = "INSERT INTO cosmetic_items (page_id, page_name, cosmetic_type, target, theme) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (CosmeticItem entity : entities) {
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
            throw new RuntimeException("Failed to batch insert cosmetic_items", e);
        }
    }

    @Override
    public Optional<CosmeticItem> findById(Integer pageId) {
        String sql = "SELECT * FROM cosmetic_items WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find cosmetic_items by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<CosmeticItem> findAll() {
        List<CosmeticItem> list = new ArrayList<>();
        String sql = "SELECT * FROM cosmetic_items ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all cosmetic_items", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM cosmetic_items WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete cosmetic_items: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM cosmetic_items";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count cosmetic_items", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, CosmeticItem v) throws SQLException {
        ps.setInt(1, v.getPageId());
        ps.setString(2, v.getPageName());
        ps.setString(3, v.getCosmeticType());
        ps.setString(4, v.getTarget());
        ps.setString(5, v.getTheme());
    }

    private CosmeticItem mapRow(ResultSet rs) throws SQLException {
        CosmeticItem v = new CosmeticItem();
        v.setPageId(rs.getInt("page_id"));
        v.setPageName(rs.getString("page_name"));
        v.setCosmeticType(rs.getString("cosmetic_type"));
        v.setTarget(rs.getString("target"));
        v.setTheme(rs.getString("theme"));
        return v;
    }
}
