package com.poe.cache.dao;

import com.poe.cache.model.Armour;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * armours 表数据访问对象。
 */
public class ArmourDao implements CrudRepository<Armour, Integer> {

    private final Connection connection;

    public ArmourDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(Armour a) {
        String sql = "INSERT INTO armours (page_id, page_name, armour_min, armour_max, "
            + "evasion_min, evasion_max, energy_shield_min, energy_shield_max, "
            + "ward_min, ward_max, movement_speed) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, a);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert armour: " + a.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<Armour> armours) {
        String sql = "INSERT INTO armours (page_id, page_name, armour_min, armour_max, "
            + "evasion_min, evasion_max, energy_shield_min, energy_shield_max, "
            + "ward_min, ward_max, movement_speed) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Armour a : armours) {
                    setParams(ps, a);
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
            throw new RuntimeException("Failed to batch insert armours", e);
        }
    }

    @Override
    public Optional<Armour> findById(Integer pageId) {
        String sql = "SELECT * FROM armours WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find armour: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Armour> findAll() {
        List<Armour> list = new ArrayList<>();
        String sql = "SELECT * FROM armours ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all armours", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM armours WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete armour: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM armours";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count armours", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Armour a) throws SQLException {
        ps.setInt(1, a.getPageId());
        ps.setString(2, a.getPageName());
        ps.setInt(3, a.getArmourMin());
        ps.setInt(4, a.getArmourMax());
        ps.setInt(5, a.getEvasionMin());
        ps.setInt(6, a.getEvasionMax());
        ps.setInt(7, a.getEnergyShieldMin());
        ps.setInt(8, a.getEnergyShieldMax());
        ps.setInt(9, a.getWardMin());
        ps.setInt(10, a.getWardMax());
        ps.setInt(11, a.getMovementSpeed());
    }

    private Armour mapRow(ResultSet rs) throws SQLException {
        Armour a = new Armour();
        a.setPageId(rs.getInt("page_id"));
        a.setPageName(rs.getString("page_name"));
        a.setArmourMin(rs.getInt("armour_min"));
        a.setArmourMax(rs.getInt("armour_max"));
        a.setEvasionMin(rs.getInt("evasion_min"));
        a.setEvasionMax(rs.getInt("evasion_max"));
        a.setEnergyShieldMin(rs.getInt("energy_shield_min"));
        a.setEnergyShieldMax(rs.getInt("energy_shield_max"));
        a.setWardMin(rs.getInt("ward_min"));
        a.setWardMax(rs.getInt("ward_max"));
        a.setMovementSpeed(rs.getInt("movement_speed"));
        return a;
    }
}
