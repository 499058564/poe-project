package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.PantheonStats;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PantheonStatsDao implements CrudRepository<PantheonStats, Integer> {

    private final DataSource dataSource;

    public PantheonStatsDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(PantheonStats entity) {
        String sql = "INSERT INTO pantheon_stats (page_id, page_name, stat_id, ordinal, pantheon_id, "
            + "pantheon_ordinal, value) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert pantheon_stats: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<PantheonStats> entities) {
        String sql = "INSERT INTO pantheon_stats (page_id, page_name, stat_id, ordinal, pantheon_id, "
            + "pantheon_ordinal, value) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (PantheonStats entity : entities) {
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
            throw new RuntimeException("Failed to batch insert pantheon_stats", e);
        }
    }

    @Override
    public Optional<PantheonStats> findById(Integer pageId) {
        String sql = "SELECT * FROM pantheon_stats WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find pantheon_stats by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<PantheonStats> findAll() {
        List<PantheonStats> list = new ArrayList<>();
        String sql = "SELECT * FROM pantheon_stats ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all pantheon_stats", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM pantheon_stats WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete pantheon_stats: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM pantheon_stats";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count pantheon_stats", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, PantheonStats p) throws SQLException {
        ps.setInt(1, p.getPageId());
        ps.setString(2, p.getPageName());
        ps.setString(3, p.getStatId());
        ps.setInt(4, p.getOrdinal());
        ps.setString(5, p.getPantheonId());
        ps.setInt(6, p.getPantheonOrdinal());
        ps.setString(7, p.getValue());
    }

    private PantheonStats mapRow(ResultSet rs) throws SQLException {
        PantheonStats p = new PantheonStats();
        p.setPageId(rs.getInt("page_id"));
        p.setPageName(rs.getString("page_name"));
        p.setStatId(rs.getString("stat_id"));
        p.setOrdinal(rs.getInt("ordinal"));
        p.setPantheonId(rs.getString("pantheon_id"));
        p.setPantheonOrdinal(rs.getInt("pantheon_ordinal"));
        p.setValue(rs.getString("value"));
        return p;
    }
}
