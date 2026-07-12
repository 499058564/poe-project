package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.BlightItems;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BlightItemsDao implements CrudRepository<BlightItems, Integer> {

    private final DataSource dataSource;

    public BlightItemsDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(BlightItems entity) {
        String sql = "INSERT INTO blight_items (page_id, page_name, tier) "
            + "VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert blight_items: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<BlightItems> entities) {
        String sql = "INSERT INTO blight_items (page_id, page_name, tier) "
            + "VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (BlightItems entity : entities) {
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
            throw new RuntimeException("Failed to batch insert blight_items", e);
        }
    }

    @Override
    public Optional<BlightItems> findById(Integer pageId) {
        String sql = "SELECT * FROM blight_items WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find blight_items by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<BlightItems> findAll() {
        List<BlightItems> list = new ArrayList<>();
        String sql = "SELECT * FROM blight_items ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all blight_items", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM blight_items WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete blight_items: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM blight_items";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count blight_items", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, BlightItems b) throws SQLException {
        ps.setInt(1, b.getPageId());
        ps.setString(2, b.getPageName());
        ps.setString(3, b.getTier());
    }

    private BlightItems mapRow(ResultSet rs) throws SQLException {
        BlightItems b = new BlightItems();
        b.setPageId(rs.getInt("page_id"));
        b.setPageName(rs.getString("page_name"));
        b.setTier(rs.getString("tier"));
        return b;
    }
}
