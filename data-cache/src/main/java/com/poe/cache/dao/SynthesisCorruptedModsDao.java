package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.SynthesisCorruptedMods;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SynthesisCorruptedModsDao implements CrudRepository<SynthesisCorruptedMods, Integer> {

    private final DataSource dataSource;

    public SynthesisCorruptedModsDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(SynthesisCorruptedMods entity) {
        String sql = "INSERT INTO synthesis_corrupted_mods (page_id, page_name, item_class_id, mod_ids) "
            + "VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert synthesis_corrupted_mods: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<SynthesisCorruptedMods> entities) {
        String sql = "INSERT INTO synthesis_corrupted_mods (page_id, page_name, item_class_id, mod_ids) "
            + "VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (SynthesisCorruptedMods entity : entities) {
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
            throw new RuntimeException("Failed to batch insert synthesis_corrupted_mods", e);
        }
    }

    @Override
    public Optional<SynthesisCorruptedMods> findById(Integer pageId) {
        String sql = "SELECT * FROM synthesis_corrupted_mods WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find synthesis_corrupted_mods by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<SynthesisCorruptedMods> findAll() {
        List<SynthesisCorruptedMods> list = new ArrayList<>();
        String sql = "SELECT * FROM synthesis_corrupted_mods ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all synthesis_corrupted_mods", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM synthesis_corrupted_mods WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete synthesis_corrupted_mods: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM synthesis_corrupted_mods";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count synthesis_corrupted_mods", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, SynthesisCorruptedMods s) throws SQLException {
        ps.setInt(1, s.getPageId());
        ps.setString(2, s.getPageName());
        ps.setString(3, s.getItemClassId());
        ps.setString(4, s.getModIds());
    }

    private SynthesisCorruptedMods mapRow(ResultSet rs) throws SQLException {
        SynthesisCorruptedMods s = new SynthesisCorruptedMods();
        s.setPageId(rs.getInt("page_id"));
        s.setPageName(rs.getString("page_name"));
        s.setItemClassId(rs.getString("item_class_id"));
        s.setModIds(rs.getString("mod_ids"));
        return s;
    }
}
