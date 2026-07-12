package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.GenericStat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenericStatDao implements CrudRepository<GenericStat, Integer> {

    private final DataSource dataSource;

    public GenericStatDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(GenericStat entity) {
        String sql = "INSERT INTO generic_stats (page_id, page_name, id, name, stat_text, value) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert generic_stats: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<GenericStat> entities) {
        String sql = "INSERT INTO generic_stats (page_id, page_name, id, name, stat_text, value) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (GenericStat entity : entities) {
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
            throw new RuntimeException("Failed to batch insert generic_stats", e);
        }
    }

    @Override
    public Optional<GenericStat> findById(Integer pageId) {
        String sql = "SELECT * FROM generic_stats WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find generic_stats by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<GenericStat> findAll() {
        List<GenericStat> list = new ArrayList<>();
        String sql = "SELECT * FROM generic_stats ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all generic_stats", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM generic_stats WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete generic_stats: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM generic_stats";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count generic_stats", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, GenericStat v) throws SQLException {
        ps.setInt(1, v.getPageId());
        ps.setString(2, v.getPageName());
        ps.setString(3, v.getId());
        ps.setString(4, v.getName());
        ps.setString(5, v.getStatText());
        ps.setInt(6, v.getValue());
    }

    private GenericStat mapRow(ResultSet rs) throws SQLException {
        GenericStat v = new GenericStat();
        v.setPageId(rs.getInt("page_id"));
        v.setPageName(rs.getString("page_name"));
        v.setId(rs.getString("id"));
        v.setName(rs.getString("name"));
        v.setStatText(rs.getString("stat_text"));
        v.setValue(rs.getInt("value"));
        return v;
    }
}
