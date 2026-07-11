package com.poe.cache.dao;

import com.poe.cache.model.Idol;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IdolDao implements CrudRepository<Idol, Integer> {

    private final javax.sql.DataSource dataSource;

    public IdolDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(Idol entity) {
        String sql = "INSERT INTO idols (page_id, page_name, idol_limit) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert idols: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<Idol> entities) {
        String sql = "INSERT INTO idols (page_id, page_name, idol_limit) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Idol entity : entities) {
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
            throw new RuntimeException("Failed to batch insert idols", e);
        }
    }

    @Override
    public Optional<Idol> findById(Integer pageId) {
        String sql = "SELECT * FROM idols WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find idols by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Idol> findAll() {
        List<Idol> list = new ArrayList<>();
        String sql = "SELECT * FROM idols ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all idols", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM idols WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete idols: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM idols";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count idols", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Idol v) throws SQLException {
        ps.setInt(1, v.getPageId());
        ps.setString(2, v.getPageName());
        ps.setString(3, v.getIdolLimit());
    }

    private Idol mapRow(ResultSet rs) throws SQLException {
        Idol v = new Idol();
        v.setPageId(rs.getInt("page_id"));
        v.setPageName(rs.getString("page_name"));
        v.setIdolLimit(rs.getString("idol_limit"));
        return v;
    }
}
