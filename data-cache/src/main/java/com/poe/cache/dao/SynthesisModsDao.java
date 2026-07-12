package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.SynthesisMods;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SynthesisModsDao implements CrudRepository<SynthesisMods, Integer> {

    private final DataSource dataSource;

    public SynthesisModsDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(SynthesisMods entity) {
        String sql = "INSERT INTO synthesis_mods (page_id, page_name, item_class_ids, mod_ids, "
            + "stat_id, stat_text, stat_value) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert synthesis_mods: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<SynthesisMods> entities) {
        String sql = "INSERT INTO synthesis_mods (page_id, page_name, item_class_ids, mod_ids, "
            + "stat_id, stat_text, stat_value) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (SynthesisMods entity : entities) {
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
            throw new RuntimeException("Failed to batch insert synthesis_mods", e);
        }
    }

    @Override
    public Optional<SynthesisMods> findById(Integer pageId) {
        String sql = "SELECT * FROM synthesis_mods WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find synthesis_mods by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<SynthesisMods> findAll() {
        List<SynthesisMods> list = new ArrayList<>();
        String sql = "SELECT * FROM synthesis_mods ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all synthesis_mods", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM synthesis_mods WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete synthesis_mods: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM synthesis_mods";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count synthesis_mods", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, SynthesisMods s) throws SQLException {
        ps.setInt(1, s.getPageId());
        ps.setString(2, s.getPageName());
        ps.setString(3, s.getItemClassIds());
        ps.setString(4, s.getModIds());
        ps.setString(5, s.getStatId());
        ps.setString(6, s.getStatText());
        ps.setDouble(7, s.getStatValue());
    }

    private SynthesisMods mapRow(ResultSet rs) throws SQLException {
        SynthesisMods s = new SynthesisMods();
        s.setPageId(rs.getInt("page_id"));
        s.setPageName(rs.getString("page_name"));
        s.setItemClassIds(rs.getString("item_class_ids"));
        s.setModIds(rs.getString("mod_ids"));
        s.setStatId(rs.getString("stat_id"));
        s.setStatText(rs.getString("stat_text"));
        s.setStatValue(rs.getDouble("stat_value"));
        return s;
    }
}
