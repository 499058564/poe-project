package com.poe.cache.dao;

import com.poe.cache.model.Weapon;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * weapons 表数据访问对象。
 */
public class WeaponDao implements CrudRepository<Weapon, Integer> {

    private final Connection connection;

    public WeaponDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(Weapon w) {
        String sql = "INSERT INTO weapons (page_id, page_name, attack_speed, critical_strike_chance, "
            + "weapon_range, physical_damage_min, physical_damage_max, "
            + "fire_damage_min, fire_damage_max, cold_damage_min, cold_damage_max, "
            + "lightning_damage_min, lightning_damage_max, chaos_damage_min, chaos_damage_max) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, w);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert weapon: " + w.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<Weapon> weapons) {
        String sql = "INSERT INTO weapons (page_id, page_name, attack_speed, critical_strike_chance, "
            + "weapon_range, physical_damage_min, physical_damage_max, "
            + "fire_damage_min, fire_damage_max, cold_damage_min, cold_damage_max, "
            + "lightning_damage_min, lightning_damage_max, chaos_damage_min, chaos_damage_max) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Weapon w : weapons) {
                    setParams(ps, w);
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
            throw new RuntimeException("Failed to batch insert weapons", e);
        }
    }

    @Override
    public Optional<Weapon> findById(Integer pageId) {
        String sql = "SELECT * FROM weapons WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find weapon by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Weapon> findAll() {
        List<Weapon> list = new ArrayList<>();
        String sql = "SELECT * FROM weapons ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all weapons", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM weapons WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete weapon: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM weapons";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count weapons", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Weapon w) throws SQLException {
        ps.setInt(1, w.getPageId());
        ps.setString(2, w.getPageName());
        ps.setDouble(3, w.getAttackSpeed());
        ps.setDouble(4, w.getCriticalStrikeChance());
        ps.setDouble(5, w.getWeaponRange());
        ps.setInt(6, w.getPhysicalDamageMin());
        ps.setInt(7, w.getPhysicalDamageMax());
        ps.setInt(8, w.getFireDamageMin());
        ps.setInt(9, w.getFireDamageMax());
        ps.setInt(10, w.getColdDamageMin());
        ps.setInt(11, w.getColdDamageMax());
        ps.setInt(12, w.getLightningDamageMin());
        ps.setInt(13, w.getLightningDamageMax());
        ps.setInt(14, w.getChaosDamageMin());
        ps.setInt(15, w.getChaosDamageMax());
    }

    private Weapon mapRow(ResultSet rs) throws SQLException {
        Weapon w = new Weapon();
        w.setPageId(rs.getInt("page_id"));
        w.setPageName(rs.getString("page_name"));
        w.setAttackSpeed(rs.getDouble("attack_speed"));
        w.setCriticalStrikeChance(rs.getDouble("critical_strike_chance"));
        w.setWeaponRange(rs.getDouble("weapon_range"));
        w.setPhysicalDamageMin(rs.getInt("physical_damage_min"));
        w.setPhysicalDamageMax(rs.getInt("physical_damage_max"));
        w.setFireDamageMin(rs.getInt("fire_damage_min"));
        w.setFireDamageMax(rs.getInt("fire_damage_max"));
        w.setColdDamageMin(rs.getInt("cold_damage_min"));
        w.setColdDamageMax(rs.getInt("cold_damage_max"));
        w.setLightningDamageMin(rs.getInt("lightning_damage_min"));
        w.setLightningDamageMax(rs.getInt("lightning_damage_max"));
        w.setChaosDamageMin(rs.getInt("chaos_damage_min"));
        w.setChaosDamageMax(rs.getInt("chaos_damage_max"));
        return w;
    }
}
