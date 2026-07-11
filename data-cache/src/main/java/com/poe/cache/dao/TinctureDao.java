package com.poe.cache.dao;

import com.poe.cache.model.Tincture;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TinctureDao implements CrudRepository<Tincture, Integer> {

    private final javax.sql.DataSource dataSource;

    public TinctureDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(Tincture entity) {
        String sql = "INSERT INTO tinctures (page_id, page_name, cooldown, cooldown_html, cooldown_range_average, " +
            "cooldown_range_colour, cooldown_range_maximum, cooldown_range_minimum, cooldown_range_text, " +
            "debuff_interval, debuff_interval_html, debuff_interval_range_average, debuff_interval_range_colour, " +
            "debuff_interval_range_maximum, debuff_interval_range_minimum, debuff_interval_range_text) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert tinctures: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<Tincture> entities) {
        String sql = "INSERT INTO tinctures (page_id, page_name, cooldown, cooldown_html, cooldown_range_average, " +
            "cooldown_range_colour, cooldown_range_maximum, cooldown_range_minimum, cooldown_range_text, " +
            "debuff_interval, debuff_interval_html, debuff_interval_range_average, debuff_interval_range_colour, " +
            "debuff_interval_range_maximum, debuff_interval_range_minimum, debuff_interval_range_text) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Tincture entity : entities) {
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
            throw new RuntimeException("Failed to batch insert tinctures", e);
        }
    }

    @Override
    public Optional<Tincture> findById(Integer pageId) {
        String sql = "SELECT * FROM tinctures WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tinctures by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Tincture> findAll() {
        List<Tincture> list = new ArrayList<>();
        String sql = "SELECT * FROM tinctures ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all tinctures", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM tinctures WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete tinctures: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM tinctures";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count tinctures", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Tincture v) throws SQLException {
        ps.setInt(1, v.getPageId());
        ps.setString(2, v.getPageName());
        ps.setDouble(3, v.getCooldown());
        ps.setString(4, v.getCooldownHtml());
        ps.setDouble(5, v.getCooldownRangeAverage());
        ps.setString(6, v.getCooldownRangeColour());
        ps.setDouble(7, v.getCooldownRangeMaximum());
        ps.setDouble(8, v.getCooldownRangeMinimum());
        ps.setString(9, v.getCooldownRangeText());
        ps.setDouble(10, v.getDebuffInterval());
        ps.setString(11, v.getDebuffIntervalHtml());
        ps.setDouble(12, v.getDebuffIntervalRangeAverage());
        ps.setString(13, v.getDebuffIntervalRangeColour());
        ps.setDouble(14, v.getDebuffIntervalRangeMaximum());
        ps.setDouble(15, v.getDebuffIntervalRangeMinimum());
        ps.setString(16, v.getDebuffIntervalRangeText());
    }

    private Tincture mapRow(ResultSet rs) throws SQLException {
        Tincture v = new Tincture();
        v.setPageId(rs.getInt("page_id"));
        v.setPageName(rs.getString("page_name"));
        v.setCooldown(rs.getDouble("cooldown"));
        v.setCooldownHtml(rs.getString("cooldown_html"));
        v.setCooldownRangeAverage(rs.getDouble("cooldown_range_average"));
        v.setCooldownRangeColour(rs.getString("cooldown_range_colour"));
        v.setCooldownRangeMaximum(rs.getDouble("cooldown_range_maximum"));
        v.setCooldownRangeMinimum(rs.getDouble("cooldown_range_minimum"));
        v.setCooldownRangeText(rs.getString("cooldown_range_text"));
        v.setDebuffInterval(rs.getDouble("debuff_interval"));
        v.setDebuffIntervalHtml(rs.getString("debuff_interval_html"));
        v.setDebuffIntervalRangeAverage(rs.getDouble("debuff_interval_range_average"));
        v.setDebuffIntervalRangeColour(rs.getString("debuff_interval_range_colour"));
        v.setDebuffIntervalRangeMaximum(rs.getDouble("debuff_interval_range_maximum"));
        v.setDebuffIntervalRangeMinimum(rs.getDouble("debuff_interval_range_minimum"));
        v.setDebuffIntervalRangeText(rs.getString("debuff_interval_range_text"));
        return v;
    }
}
