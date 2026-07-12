package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.SynthesisAreas;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SynthesisAreasDao implements CrudRepository<SynthesisAreas, Integer> {

    private final DataSource dataSource;

    public SynthesisAreasDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(SynthesisAreas entity) {
        String sql = "INSERT INTO synthesis_areas (page_id, page_name, area_id, max_level, min_level, "
            + "name, size, weight) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert synthesis_areas: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<SynthesisAreas> entities) {
        String sql = "INSERT INTO synthesis_areas (page_id, page_name, area_id, max_level, min_level, "
            + "name, size, weight) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (SynthesisAreas entity : entities) {
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
            throw new RuntimeException("Failed to batch insert synthesis_areas", e);
        }
    }

    @Override
    public Optional<SynthesisAreas> findById(Integer pageId) {
        String sql = "SELECT * FROM synthesis_areas WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find synthesis_areas by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<SynthesisAreas> findAll() {
        List<SynthesisAreas> list = new ArrayList<>();
        String sql = "SELECT * FROM synthesis_areas ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all synthesis_areas", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM synthesis_areas WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete synthesis_areas: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM synthesis_areas";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count synthesis_areas", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, SynthesisAreas s) throws SQLException {
        ps.setInt(1, s.getPageId());
        ps.setString(2, s.getPageName());
        ps.setString(3, s.getAreaId());
        ps.setInt(4, s.getMaxLevel());
        ps.setInt(5, s.getMinLevel());
        ps.setString(6, s.getName());
        ps.setInt(7, s.getSize());
        ps.setInt(8, s.getWeight());
    }

    private SynthesisAreas mapRow(ResultSet rs) throws SQLException {
        SynthesisAreas s = new SynthesisAreas();
        s.setPageId(rs.getInt("page_id"));
        s.setPageName(rs.getString("page_name"));
        s.setAreaId(rs.getString("area_id"));
        s.setMaxLevel(rs.getInt("max_level"));
        s.setMinLevel(rs.getInt("min_level"));
        s.setName(rs.getString("name"));
        s.setSize(rs.getInt("size"));
        s.setWeight(rs.getInt("weight"));
        return s;
    }
}
