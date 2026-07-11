package com.poe.cache.dao;

import com.poe.cache.model.Sentinel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SentinelDao implements CrudRepository<Sentinel, Integer> {

    private final Connection connection;

    public SentinelDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(Sentinel entity) {
        String sql = "INSERT INTO sentinels (page_id, page_name, charge, charge_html, charge_range_average, " +
            "charge_range_colour, charge_range_maximum, charge_range_minimum, charge_range_text, " +
            "duration, duration_html, duration_range_average, duration_range_colour, duration_range_maximum, " +
            "duration_range_minimum, duration_range_text, empowerment, empowerment_html, empowerment_range_average, " +
            "empowerment_range_colour, empowerment_range_maximum, empowerment_range_minimum, empowerment_range_text, " +
            "empowers, empowers_html, empowers_range_average, empowers_range_colour, empowers_range_maximum, " +
            "empowers_range_minimum, empowers_range_text, monster, monster_level) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert sentinels: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<Sentinel> entities) {
        String sql = "INSERT INTO sentinels (page_id, page_name, charge, charge_html, charge_range_average, " +
            "charge_range_colour, charge_range_maximum, charge_range_minimum, charge_range_text, " +
            "duration, duration_html, duration_range_average, duration_range_colour, duration_range_maximum, " +
            "duration_range_minimum, duration_range_text, empowerment, empowerment_html, empowerment_range_average, " +
            "empowerment_range_colour, empowerment_range_maximum, empowerment_range_minimum, empowerment_range_text, " +
            "empowers, empowers_html, empowers_range_average, empowers_range_colour, empowers_range_maximum, " +
            "empowers_range_minimum, empowers_range_text, monster, monster_level) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Sentinel entity : entities) {
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
            throw new RuntimeException("Failed to batch insert sentinels", e);
        }
    }

    @Override
    public Optional<Sentinel> findById(Integer pageId) {
        String sql = "SELECT * FROM sentinels WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find sentinels by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Sentinel> findAll() {
        List<Sentinel> list = new ArrayList<>();
        String sql = "SELECT * FROM sentinels ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all sentinels", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM sentinels WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete sentinels: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM sentinels";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count sentinels", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Sentinel v) throws SQLException {
        ps.setInt(1, v.getPageId());
        ps.setString(2, v.getPageName());
        ps.setInt(3, v.getCharge());
        ps.setString(4, v.getChargeHtml());
        ps.setInt(5, v.getChargeRangeAverage());
        ps.setString(6, v.getChargeRangeColour());
        ps.setInt(7, v.getChargeRangeMaximum());
        ps.setInt(8, v.getChargeRangeMinimum());
        ps.setString(9, v.getChargeRangeText());
        ps.setInt(10, v.getDuration());
        ps.setString(11, v.getDurationHtml());
        ps.setInt(12, v.getDurationRangeAverage());
        ps.setString(13, v.getDurationRangeColour());
        ps.setInt(14, v.getDurationRangeMaximum());
        ps.setInt(15, v.getDurationRangeMinimum());
        ps.setString(16, v.getDurationRangeText());
        ps.setInt(17, v.getEmpowerment());
        ps.setString(18, v.getEmpowermentHtml());
        ps.setInt(19, v.getEmpowermentRangeAverage());
        ps.setString(20, v.getEmpowermentRangeColour());
        ps.setInt(21, v.getEmpowermentRangeMaximum());
        ps.setInt(22, v.getEmpowermentRangeMinimum());
        ps.setString(23, v.getEmpowermentRangeText());
        ps.setInt(24, v.getEmpowers());
        ps.setString(25, v.getEmpowersHtml());
        ps.setInt(26, v.getEmpowersRangeAverage());
        ps.setString(27, v.getEmpowersRangeColour());
        ps.setInt(28, v.getEmpowersRangeMaximum());
        ps.setInt(29, v.getEmpowersRangeMinimum());
        ps.setString(30, v.getEmpowersRangeText());
        ps.setString(31, v.getMonster());
        ps.setInt(32, v.getMonsterLevel());
    }

    private Sentinel mapRow(ResultSet rs) throws SQLException {
        Sentinel v = new Sentinel();
        v.setPageId(rs.getInt("page_id"));
        v.setPageName(rs.getString("page_name"));
        v.setCharge(rs.getInt("charge"));
        v.setChargeHtml(rs.getString("charge_html"));
        v.setChargeRangeAverage(rs.getInt("charge_range_average"));
        v.setChargeRangeColour(rs.getString("charge_range_colour"));
        v.setChargeRangeMaximum(rs.getInt("charge_range_maximum"));
        v.setChargeRangeMinimum(rs.getInt("charge_range_minimum"));
        v.setChargeRangeText(rs.getString("charge_range_text"));
        v.setDuration(rs.getInt("duration"));
        v.setDurationHtml(rs.getString("duration_html"));
        v.setDurationRangeAverage(rs.getInt("duration_range_average"));
        v.setDurationRangeColour(rs.getString("duration_range_colour"));
        v.setDurationRangeMaximum(rs.getInt("duration_range_maximum"));
        v.setDurationRangeMinimum(rs.getInt("duration_range_minimum"));
        v.setDurationRangeText(rs.getString("duration_range_text"));
        v.setEmpowerment(rs.getInt("empowerment"));
        v.setEmpowermentHtml(rs.getString("empowerment_html"));
        v.setEmpowermentRangeAverage(rs.getInt("empowerment_range_average"));
        v.setEmpowermentRangeColour(rs.getString("empowerment_range_colour"));
        v.setEmpowermentRangeMaximum(rs.getInt("empowerment_range_maximum"));
        v.setEmpowermentRangeMinimum(rs.getInt("empowerment_range_minimum"));
        v.setEmpowermentRangeText(rs.getString("empowerment_range_text"));
        v.setEmpowers(rs.getInt("empowers"));
        v.setEmpowersHtml(rs.getString("empowers_html"));
        v.setEmpowersRangeAverage(rs.getInt("empowers_range_average"));
        v.setEmpowersRangeColour(rs.getString("empowers_range_colour"));
        v.setEmpowersRangeMaximum(rs.getInt("empowers_range_maximum"));
        v.setEmpowersRangeMinimum(rs.getInt("empowers_range_minimum"));
        v.setEmpowersRangeText(rs.getString("empowers_range_text"));
        v.setMonster(rs.getString("monster"));
        v.setMonsterLevel(rs.getInt("monster_level"));
        return v;
    }
}
