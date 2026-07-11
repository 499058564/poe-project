package com.poe.cache.dao;

import com.poe.cache.model.DelveResourcesPerLevel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DelveResourcesPerLevelDao implements CrudRepository<DelveResourcesPerLevel, Integer> {

    private final javax.sql.DataSource dataSource;

    public DelveResourcesPerLevelDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(DelveResourcesPerLevel entity) {
        String sql = "INSERT INTO delve_resources_per_level (page_id, page_name, area_level, sulphite) "
            + "VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert delve_resources_per_level: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<DelveResourcesPerLevel> entities) {
        String sql = "INSERT INTO delve_resources_per_level (page_id, page_name, area_level, sulphite) "
            + "VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (DelveResourcesPerLevel entity : entities) {
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
            throw new RuntimeException("Failed to batch insert delve_resources_per_level", e);
        }
    }

    @Override
    public Optional<DelveResourcesPerLevel> findById(Integer pageId) {
        String sql = "SELECT * FROM delve_resources_per_level WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find delve_resources_per_level by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<DelveResourcesPerLevel> findAll() {
        List<DelveResourcesPerLevel> list = new ArrayList<>();
        String sql = "SELECT * FROM delve_resources_per_level ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all delve_resources_per_level", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM delve_resources_per_level WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete delve_resources_per_level: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM delve_resources_per_level";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count delve_resources_per_level", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, DelveResourcesPerLevel d) throws SQLException {
        ps.setInt(1, d.getPageId());
        ps.setString(2, d.getPageName());
        ps.setInt(3, d.getAreaLevel());
        ps.setInt(4, d.getSulphite());
    }

    private DelveResourcesPerLevel mapRow(ResultSet rs) throws SQLException {
        DelveResourcesPerLevel d = new DelveResourcesPerLevel();
        d.setPageId(rs.getInt("page_id"));
        d.setPageName(rs.getString("page_name"));
        d.setAreaLevel(rs.getInt("area_level"));
        d.setSulphite(rs.getInt("sulphite"));
        return d;
    }
}
