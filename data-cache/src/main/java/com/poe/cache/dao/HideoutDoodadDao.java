package com.poe.cache.dao;

import com.poe.cache.model.HideoutDoodad;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HideoutDoodadDao implements CrudRepository<HideoutDoodad, Integer> {

    private final Connection connection;

    public HideoutDoodadDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(HideoutDoodad entity) {
        String sql = "INSERT INTO hideout_doodads (page_id, page_name, is_master_doodad, variation_count) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert hideout_doodads: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<HideoutDoodad> entities) {
        String sql = "INSERT INTO hideout_doodads (page_id, page_name, is_master_doodad, variation_count) VALUES (?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (HideoutDoodad entity : entities) {
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
            throw new RuntimeException("Failed to batch insert hideout_doodads", e);
        }
    }

    @Override
    public Optional<HideoutDoodad> findById(Integer pageId) {
        String sql = "SELECT * FROM hideout_doodads WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find hideout_doodads by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<HideoutDoodad> findAll() {
        List<HideoutDoodad> list = new ArrayList<>();
        String sql = "SELECT * FROM hideout_doodads ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all hideout_doodads", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM hideout_doodads WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete hideout_doodads: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM hideout_doodads";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count hideout_doodads", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, HideoutDoodad v) throws SQLException {
        ps.setInt(1, v.getPageId());
        ps.setString(2, v.getPageName());
        ps.setInt(3, v.getIsMasterDoodad());
        ps.setInt(4, v.getVariationCount());
    }

    private HideoutDoodad mapRow(ResultSet rs) throws SQLException {
        HideoutDoodad v = new HideoutDoodad();
        v.setPageId(rs.getInt("page_id"));
        v.setPageName(rs.getString("page_name"));
        v.setIsMasterDoodad(rs.getInt("is_master_doodad"));
        v.setVariationCount(rs.getInt("variation_count"));
        return v;
    }
}
