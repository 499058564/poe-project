package com.poe.cache.dao;

import com.poe.cache.model.LegacyVariant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LegacyVariantDao implements CrudRepository<LegacyVariant, Integer> {

    private final Connection connection;

    public LegacyVariantDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(LegacyVariant entity) {
        String sql = "INSERT INTO legacy_variants (page_id, page_name, removal_version, implicit_stat_text, " +
            "explicit_stat_text, stat_text, base_item, required_level) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert legacy_variants: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<LegacyVariant> entities) {
        String sql = "INSERT INTO legacy_variants (page_id, page_name, removal_version, implicit_stat_text, " +
            "explicit_stat_text, stat_text, base_item, required_level) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (LegacyVariant entity : entities) {
                    setParams(ps, entity);
                    ps.addBatch();
                }
                ps.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to batch insert legacy_variants", e);
        }
    }

    @Override
    public Optional<LegacyVariant> findById(Integer pageId) {
        String sql = "SELECT * FROM legacy_variants WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find legacy_variants by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<LegacyVariant> findAll() {
        List<LegacyVariant> list = new ArrayList<>();
        String sql = "SELECT * FROM legacy_variants ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all legacy_variants", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM legacy_variants WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete legacy_variants: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM legacy_variants";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count legacy_variants", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, LegacyVariant v) throws SQLException {
        ps.setInt(1, v.getPageId());
        ps.setString(2, v.getPageName());
        ps.setString(3, v.getRemovalVersion());
        ps.setString(4, v.getImplicitStatText());
        ps.setString(5, v.getExplicitStatText());
        ps.setString(6, v.getStatText());
        ps.setString(7, v.getBaseItem());
        ps.setInt(8, v.getRequiredLevel());
    }

    private LegacyVariant mapRow(ResultSet rs) throws SQLException {
        LegacyVariant v = new LegacyVariant();
        v.setPageId(rs.getInt("page_id"));
        v.setPageName(rs.getString("page_name"));
        v.setRemovalVersion(rs.getString("removal_version"));
        v.setImplicitStatText(rs.getString("implicit_stat_text"));
        v.setExplicitStatText(rs.getString("explicit_stat_text"));
        v.setStatText(rs.getString("stat_text"));
        v.setBaseItem(rs.getString("base_item"));
        v.setRequiredLevel(rs.getInt("required_level"));
        return v;
    }
}
