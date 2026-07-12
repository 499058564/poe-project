package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.DelveUpgrades;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DelveUpgradesDao implements CrudRepository<DelveUpgrades, Integer> {

    private final DataSource dataSource;

    public DelveUpgradesDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(DelveUpgrades entity) {
        String sql = "INSERT INTO delve_upgrades (page_id, page_name, cost, level, type) "
            + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert delve_upgrades: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<DelveUpgrades> entities) {
        String sql = "INSERT INTO delve_upgrades (page_id, page_name, cost, level, type) "
            + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (DelveUpgrades entity : entities) {
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
            throw new RuntimeException("Failed to batch insert delve_upgrades", e);
        }
    }

    @Override
    public Optional<DelveUpgrades> findById(Integer pageId) {
        String sql = "SELECT * FROM delve_upgrades WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find delve_upgrades by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<DelveUpgrades> findAll() {
        List<DelveUpgrades> list = new ArrayList<>();
        String sql = "SELECT * FROM delve_upgrades ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all delve_upgrades", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM delve_upgrades WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete delve_upgrades: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM delve_upgrades";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count delve_upgrades", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, DelveUpgrades d) throws SQLException {
        ps.setInt(1, d.getPageId());
        ps.setString(2, d.getPageName());
        ps.setInt(3, d.getCost());
        ps.setInt(4, d.getLevel());
        ps.setString(5, d.getType());
    }

    private DelveUpgrades mapRow(ResultSet rs) throws SQLException {
        DelveUpgrades d = new DelveUpgrades();
        d.setPageId(rs.getInt("page_id"));
        d.setPageName(rs.getString("page_name"));
        d.setCost(rs.getInt("cost"));
        d.setLevel(rs.getInt("level"));
        d.setType(rs.getString("type"));
        return d;
    }
}
