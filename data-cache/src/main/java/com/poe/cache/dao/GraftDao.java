package com.poe.cache.dao;

import com.poe.cache.model.Graft;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GraftDao implements CrudRepository<Graft, Integer> {

    private final javax.sql.DataSource dataSource;

    public GraftDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(Graft entity) {
        String sql = "INSERT INTO grafts (page_id, page_name, skill_id) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert grafts: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<Graft> entities) {
        String sql = "INSERT INTO grafts (page_id, page_name, skill_id) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Graft entity : entities) {
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
            throw new RuntimeException("Failed to batch insert grafts", e);
        }
    }

    @Override
    public Optional<Graft> findById(Integer pageId) {
        String sql = "SELECT * FROM grafts WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find grafts by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Graft> findAll() {
        List<Graft> list = new ArrayList<>();
        String sql = "SELECT * FROM grafts ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all grafts", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM grafts WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete grafts: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM grafts";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count grafts", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Graft v) throws SQLException {
        ps.setInt(1, v.getPageId());
        ps.setString(2, v.getPageName());
        ps.setString(3, v.getSkillId());
    }

    private Graft mapRow(ResultSet rs) throws SQLException {
        Graft v = new Graft();
        v.setPageId(rs.getInt("page_id"));
        v.setPageName(rs.getString("page_name"));
        v.setSkillId(rs.getString("skill_id"));
        return v;
    }
}
