package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.SpawnWeight;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpawnWeightDao implements CrudRepository<SpawnWeight, Integer> {

    private final DataSource dataSource;

    public SpawnWeightDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(SpawnWeight entity) {
        String sql = "INSERT INTO spawn_weights (page_id, page_name, ordinal, tag, weight) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert spawn_weights: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<SpawnWeight> entities) {
        String sql = "INSERT INTO spawn_weights (page_id, page_name, ordinal, tag, weight) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (SpawnWeight entity : entities) {
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
            throw new RuntimeException("Failed to batch insert spawn_weights", e);
        }
    }

    @Override
    public Optional<SpawnWeight> findById(Integer pageId) {
        String sql = "SELECT * FROM spawn_weights WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find spawn_weights by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<SpawnWeight> findAll() {
        List<SpawnWeight> list = new ArrayList<>();
        String sql = "SELECT * FROM spawn_weights ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all spawn_weights", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM spawn_weights WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete spawn_weights: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM spawn_weights";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count spawn_weights", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, SpawnWeight v) throws SQLException {
        ps.setInt(1, v.getPageId());
        ps.setString(2, v.getPageName());
        ps.setInt(3, v.getOrdinal());
        ps.setString(4, v.getTag());
        ps.setInt(5, v.getWeight());
    }

    private SpawnWeight mapRow(ResultSet rs) throws SQLException {
        SpawnWeight v = new SpawnWeight();
        v.setPageId(rs.getInt("page_id"));
        v.setPageName(rs.getString("page_name"));
        v.setOrdinal(rs.getInt("ordinal"));
        v.setTag(rs.getString("tag"));
        v.setWeight(rs.getInt("weight"));
        return v;
    }
}
