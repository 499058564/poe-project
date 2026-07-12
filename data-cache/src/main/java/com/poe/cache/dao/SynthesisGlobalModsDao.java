package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.SynthesisGlobalMods;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SynthesisGlobalModsDao implements CrudRepository<SynthesisGlobalMods, Integer> {

    private final DataSource dataSource;

    public SynthesisGlobalModsDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(SynthesisGlobalMods entity) {
        String sql = "INSERT INTO synthesis_global_mods (page_id, page_name, max_level, min_level, "
            + "mod_id, weight) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert synthesis_global_mods: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<SynthesisGlobalMods> entities) {
        String sql = "INSERT INTO synthesis_global_mods (page_id, page_name, max_level, min_level, "
            + "mod_id, weight) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (SynthesisGlobalMods entity : entities) {
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
            throw new RuntimeException("Failed to batch insert synthesis_global_mods", e);
        }
    }

    @Override
    public Optional<SynthesisGlobalMods> findById(Integer pageId) {
        String sql = "SELECT * FROM synthesis_global_mods WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find synthesis_global_mods by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<SynthesisGlobalMods> findAll() {
        List<SynthesisGlobalMods> list = new ArrayList<>();
        String sql = "SELECT * FROM synthesis_global_mods ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all synthesis_global_mods", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM synthesis_global_mods WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete synthesis_global_mods: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM synthesis_global_mods";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count synthesis_global_mods", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, SynthesisGlobalMods s) throws SQLException {
        ps.setInt(1, s.getPageId());
        ps.setString(2, s.getPageName());
        ps.setInt(3, s.getMaxLevel());
        ps.setInt(4, s.getMinLevel());
        ps.setString(5, s.getModId());
        ps.setInt(6, s.getWeight());
    }

    private SynthesisGlobalMods mapRow(ResultSet rs) throws SQLException {
        SynthesisGlobalMods s = new SynthesisGlobalMods();
        s.setPageId(rs.getInt("page_id"));
        s.setPageName(rs.getString("page_name"));
        s.setMaxLevel(rs.getInt("max_level"));
        s.setMinLevel(rs.getInt("min_level"));
        s.setModId(rs.getString("mod_id"));
        s.setWeight(rs.getInt("weight"));
        return s;
    }
}
