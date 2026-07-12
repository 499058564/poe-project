package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.Guide;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuideDao implements CrudRepository<Guide, Integer> {

    private final DataSource dataSource;

    public GuideDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(Guide entity) {
        String sql = "INSERT INTO guides (page_id, page_name, date, subject, version) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert guides: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<Guide> entities) {
        String sql = "INSERT INTO guides (page_id, page_name, date, subject, version) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Guide entity : entities) {
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
            throw new RuntimeException("Failed to batch insert guides", e);
        }
    }

    @Override
    public Optional<Guide> findById(Integer pageId) {
        String sql = "SELECT * FROM guides WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find guides by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Guide> findAll() {
        List<Guide> list = new ArrayList<>();
        String sql = "SELECT * FROM guides ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all guides", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM guides WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete guides: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM guides";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count guides", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Guide v) throws SQLException {
        ps.setInt(1, v.getPageId());
        ps.setString(2, v.getPageName());
        ps.setString(3, v.getDate());
        ps.setString(4, v.getSubject());
        ps.setString(5, v.getVersion());
    }

    private Guide mapRow(ResultSet rs) throws SQLException {
        Guide v = new Guide();
        v.setPageId(rs.getInt("page_id"));
        v.setPageName(rs.getString("page_name"));
        v.setDate(rs.getString("date"));
        v.setSubject(rs.getString("subject"));
        v.setVersion(rs.getString("version"));
        return v;
    }
}
