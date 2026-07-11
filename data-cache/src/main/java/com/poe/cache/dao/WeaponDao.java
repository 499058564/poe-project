package com.poe.cache.dao;

import com.poe.cache.model.Weapon;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * weapons 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键，通过 page_name 与 base_items 表关联。
 * 记录武器的攻速、暴击率、武器范围和各类伤害范围。
 */
public class WeaponDao implements CrudRepository<Weapon, Integer> {

    private final Connection connection;

    public WeaponDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * 插入单条武器记录。
     *
     * @param w 武器实体
     */
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

    /**
     * 批量插入武器记录（事务）。
     * <p>
     * 使用 JDBC batch + 事务保证原子性，失败自动回滚。
     *
     * @param weapons 武器实体列表（非空）
     */
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

    /**
     * 根据主键查询武器。
     *
     * @param pageId Wiki 页面 ID
     * @return 武器实体（可能为空）
     */
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

    /**
     * 查询全部武器记录。
     *
     * @return 按 page_id 升序排列的武器列表
     */
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

    /**
     * 根据主键删除武器。
     *
     * @param pageId Wiki 页面 ID
     */
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

    /**
     * 统计武器记录总数。
     *
     * @return weapons 表行数
     */
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

    /** 设置 PreparedStatement 参数（绑定 Weapon 字段） */
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

    /** 从 ResultSet 映射一行到 Weapon 实体 */
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
