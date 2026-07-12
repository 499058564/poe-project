package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.DelveUpgradeStats;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DelveUpgradeStatsDao implements CrudRepository<DelveUpgradeStats, Integer> {

    private final DataSource dataSource;

    public DelveUpgradeStatsDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(DelveUpgradeStats entity) {
        String sql = "INSERT INTO delve_upgrade_stats (page_id, page_name, cargo_id, level, type, value) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert delve_upgrade_stats: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<DelveUpgradeStats> entities) {
        String sql = "INSERT INTO delve_upgrade_stats (page_id, page_name, cargo_id, level, type, value) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (DelveUpgradeStats entity : entities) {
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
            throw new RuntimeException("Failed to batch insert delve_upgrade_stats", e);
        }
    }

    @Override
    public Optional<DelveUpgradeStats> findById(Integer pageId) {
        String sql = "SELECT * FROM delve_upgrade_stats WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find delve_upgrade_stats by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<DelveUpgradeStats> findAll() {
        List<DelveUpgradeStats> list = new ArrayList<>();
        String sql = "SELECT * FROM delve_upgrade_stats ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all delve_upgrade_stats", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM delve_upgrade_stats WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete delve_upgrade_stats: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM delve_upgrade_stats";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count delve_upgrade_stats", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, DelveUpgradeStats d) throws SQLException {
        ps.setInt(1, d.getPageId());
        ps.setString(2, d.getPageName());
        ps.setString(3, d.getCargoId());
        ps.setInt(4, d.getLevel());
        ps.setString(5, d.getType());
        ps.setDouble(6, d.getValue());
    }

    private DelveUpgradeStats mapRow(ResultSet rs) throws SQLException {
        DelveUpgradeStats d = new DelveUpgradeStats();
        d.setPageId(rs.getInt("page_id"));
        d.setPageName(rs.getString("page_name"));
        d.setCargoId(rs.getString("cargo_id"));
        d.setLevel(rs.getInt("level"));
        d.setType(rs.getString("type"));
        d.setValue(rs.getDouble("value"));
        return d;
    }
}
