package com.poe.cache.dao;

import com.poe.cache.model.PantheonSouls;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PantheonSoulsDao implements CrudRepository<PantheonSouls, Integer> {

    private final javax.sql.DataSource dataSource;

    public PantheonSoulsDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(PantheonSouls entity) {
        String sql = "INSERT INTO pantheon_souls (page_id, page_name, soul_id, item_id, name, ordinal, "
            + "stat_text, target_area_id, target_monster_id) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert pantheon_souls: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<PantheonSouls> entities) {
        String sql = "INSERT INTO pantheon_souls (page_id, page_name, soul_id, item_id, name, ordinal, "
            + "stat_text, target_area_id, target_monster_id) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (PantheonSouls entity : entities) {
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
            throw new RuntimeException("Failed to batch insert pantheon_souls", e);
        }
    }

    @Override
    public Optional<PantheonSouls> findById(Integer pageId) {
        String sql = "SELECT * FROM pantheon_souls WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find pantheon_souls by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<PantheonSouls> findAll() {
        List<PantheonSouls> list = new ArrayList<>();
        String sql = "SELECT * FROM pantheon_souls ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all pantheon_souls", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM pantheon_souls WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete pantheon_souls: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM pantheon_souls";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count pantheon_souls", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, PantheonSouls p) throws SQLException {
        ps.setInt(1, p.getPageId());
        ps.setString(2, p.getPageName());
        ps.setString(3, p.getSoulId());
        ps.setString(4, p.getItemId());
        ps.setString(5, p.getName());
        ps.setInt(6, p.getOrdinal());
        ps.setString(7, p.getStatText());
        ps.setString(8, p.getTargetAreaId());
        ps.setString(9, p.getTargetMonsterId());
    }

    private PantheonSouls mapRow(ResultSet rs) throws SQLException {
        PantheonSouls p = new PantheonSouls();
        p.setPageId(rs.getInt("page_id"));
        p.setPageName(rs.getString("page_name"));
        p.setSoulId(rs.getString("soul_id"));
        p.setItemId(rs.getString("item_id"));
        p.setName(rs.getString("name"));
        p.setOrdinal(rs.getInt("ordinal"));
        p.setStatText(rs.getString("stat_text"));
        p.setTargetAreaId(rs.getString("target_area_id"));
        p.setTargetMonsterId(rs.getString("target_monster_id"));
        return p;
    }
}
