package com.poe.cache.dao;

import com.poe.cache.model.Pantheon;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PantheonDao implements CrudRepository<Pantheon, Integer> {

    private final javax.sql.DataSource dataSource;

    public PantheonDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(Pantheon entity) {
        String sql = "INSERT INTO pantheon (page_id, page_name, god_name, is_major_god) "
            + "VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert pantheon: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<Pantheon> entities) {
        String sql = "INSERT INTO pantheon (page_id, page_name, god_name, is_major_god) "
            + "VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Pantheon entity : entities) {
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
            throw new RuntimeException("Failed to batch insert pantheon", e);
        }
    }

    @Override
    public Optional<Pantheon> findById(Integer pageId) {
        String sql = "SELECT * FROM pantheon WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find pantheon by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Pantheon> findAll() {
        List<Pantheon> list = new ArrayList<>();
        String sql = "SELECT * FROM pantheon ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all pantheon", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM pantheon WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete pantheon: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM pantheon";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count pantheon", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Pantheon p) throws SQLException {
        ps.setInt(1, p.getPageId());
        ps.setString(2, p.getPageName());
        ps.setString(3, p.getGodName());
        ps.setInt(4, p.isMajorGod() ? 1 : 0);
    }

    private Pantheon mapRow(ResultSet rs) throws SQLException {
        Pantheon p = new Pantheon();
        p.setPageId(rs.getInt("page_id"));
        p.setPageName(rs.getString("page_name"));
        p.setGodName(rs.getString("god_name"));
        p.setMajorGod(rs.getInt("is_major_god") == 1);
        return p;
    }
}
