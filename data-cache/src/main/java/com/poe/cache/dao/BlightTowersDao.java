package com.poe.cache.dao;

import com.poe.cache.model.BlightTowers;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BlightTowersDao implements CrudRepository<BlightTowers, Integer> {

    private final Connection connection;

    public BlightTowersDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(BlightTowers entity) {
        String sql = "INSERT INTO blight_towers (page_id, page_name, cost, description, icon, "
            + "tower_id, name, radius, tier) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert blight_towers: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<BlightTowers> entities) {
        String sql = "INSERT INTO blight_towers (page_id, page_name, cost, description, icon, "
            + "tower_id, name, radius, tier) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (BlightTowers entity : entities) {
                    setParams(ps, entity);
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
            throw new RuntimeException("Failed to batch insert blight_towers", e);
        }
    }

    @Override
    public Optional<BlightTowers> findById(Integer pageId) {
        String sql = "SELECT * FROM blight_towers WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find blight_towers by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<BlightTowers> findAll() {
        List<BlightTowers> list = new ArrayList<>();
        String sql = "SELECT * FROM blight_towers ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all blight_towers", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM blight_towers WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete blight_towers: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM blight_towers";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count blight_towers", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, BlightTowers b) throws SQLException {
        ps.setInt(1, b.getPageId());
        ps.setString(2, b.getPageName());
        ps.setInt(3, b.getCost());
        ps.setString(4, b.getDescription());
        ps.setString(5, b.getIcon());
        ps.setString(6, b.getTowerId());
        ps.setString(7, b.getName());
        ps.setInt(8, b.getRadius());
        ps.setString(9, b.getTier());
    }

    private BlightTowers mapRow(ResultSet rs) throws SQLException {
        BlightTowers b = new BlightTowers();
        b.setPageId(rs.getInt("page_id"));
        b.setPageName(rs.getString("page_name"));
        b.setCost(rs.getInt("cost"));
        b.setDescription(rs.getString("description"));
        b.setIcon(rs.getString("icon"));
        b.setTowerId(rs.getString("tower_id"));
        b.setName(rs.getString("name"));
        b.setRadius(rs.getInt("radius"));
        b.setTier(rs.getString("tier"));
        return b;
    }
}
