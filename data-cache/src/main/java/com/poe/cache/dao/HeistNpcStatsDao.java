package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.HeistNpcStats;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HeistNpcStatsDao implements CrudRepository<HeistNpcStats, Integer> {

    private final DataSource dataSource;

    public HeistNpcStatsDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(HeistNpcStats entity) {
        String sql = "INSERT INTO heist_npc_stats (page_id, page_name, npc_id, stat_id, value) "
            + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert heist_npc_stats: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<HeistNpcStats> entities) {
        String sql = "INSERT INTO heist_npc_stats (page_id, page_name, npc_id, stat_id, value) "
            + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (HeistNpcStats entity : entities) {
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
            throw new RuntimeException("Failed to batch insert heist_npc_stats", e);
        }
    }

    @Override
    public Optional<HeistNpcStats> findById(Integer pageId) {
        String sql = "SELECT * FROM heist_npc_stats WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find heist_npc_stats by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<HeistNpcStats> findAll() {
        List<HeistNpcStats> list = new ArrayList<>();
        String sql = "SELECT * FROM heist_npc_stats ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all heist_npc_stats", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM heist_npc_stats WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete heist_npc_stats: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM heist_npc_stats";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count heist_npc_stats", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, HeistNpcStats h) throws SQLException {
        ps.setInt(1, h.getPageId());
        ps.setString(2, h.getPageName());
        ps.setString(3, h.getNpcId());
        ps.setString(4, h.getStatId());
        ps.setDouble(5, h.getValue());
    }

    private HeistNpcStats mapRow(ResultSet rs) throws SQLException {
        HeistNpcStats h = new HeistNpcStats();
        h.setPageId(rs.getInt("page_id"));
        h.setPageName(rs.getString("page_name"));
        h.setNpcId(rs.getString("npc_id"));
        h.setStatId(rs.getString("stat_id"));
        h.setValue(rs.getDouble("value"));
        return h;
    }
}
