package com.poe.cache.dao;

import com.poe.cache.model.MonsterType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * monster_types 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键，记录怪物类型的倍率与抗性关联。
 */
public class MonsterTypeDao implements CrudRepository<MonsterType, Integer> {

    private final javax.sql.DataSource dataSource;

    public MonsterTypeDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 插入单条怪物类型记录。
     *
     * @param entity 怪物类型实体
     */
    @Override
    public void insert(MonsterType entity) {
        String sql = "INSERT INTO monster_types (page_id, page_name, armour_multiplier, "
            + "damage_spread, energy_shield_multiplier, evasion_multiplier, id, "
            + "monster_resistance_id, tags) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert monster type: " + entity.getPageId(), e);
        }
    }

    /**
     * 批量插入怪物类型记录（事务）。
     *
     * @param entities 怪物类型实体列表（非空）
     */
    @Override
    public void batchInsert(List<MonsterType> entities) {
        String sql = "INSERT INTO monster_types (page_id, page_name, armour_multiplier, "
            + "damage_spread, energy_shield_multiplier, evasion_multiplier, id, "
            + "monster_resistance_id, tags) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (MonsterType entity : entities) {
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
            throw new RuntimeException("Failed to batch insert monster types", e);
        }
    }

    /**
     * 根据主键查询怪物类型。
     *
     * @param pageId Wiki 页面 ID
     * @return 怪物类型实体（可能为空）
     */
    @Override
    public Optional<MonsterType> findById(Integer pageId) {
        String sql = "SELECT * FROM monster_types WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find monster type by id: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部怪物类型记录。
     *
     * @return 按 page_id 升序排列的怪物类型列表
     */
    @Override
    public List<MonsterType> findAll() {
        List<MonsterType> list = new ArrayList<>();
        String sql = "SELECT * FROM monster_types ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all monster types", e);
        }
        return list;
    }

    /**
     * 根据主键删除怪物类型。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM monster_types WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete monster type: " + pageId, e);
        }
    }

    /**
     * 统计怪物类型记录总数。
     *
     * @return monster_types 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM monster_types";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count monster types", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, MonsterType mt) throws SQLException {
        ps.setInt(1, mt.getPageId());
        ps.setString(2, mt.getPageName());
        ps.setInt(3, mt.getArmourMultiplier());
        ps.setDouble(4, mt.getDamageSpread());
        ps.setInt(5, mt.getEnergyShieldMultiplier());
        ps.setInt(6, mt.getEvasionMultiplier());
        ps.setString(7, mt.getId());
        ps.setString(8, mt.getMonsterResistanceId());
        ps.setString(9, mt.getTags());
    }

    private MonsterType mapRow(ResultSet rs) throws SQLException {
        MonsterType mt = new MonsterType();
        mt.setPageId(rs.getInt("page_id"));
        mt.setPageName(rs.getString("page_name"));
        mt.setArmourMultiplier(rs.getInt("armour_multiplier"));
        mt.setDamageSpread(rs.getDouble("damage_spread"));
        mt.setEnergyShieldMultiplier(rs.getInt("energy_shield_multiplier"));
        mt.setEvasionMultiplier(rs.getInt("evasion_multiplier"));
        mt.setId(rs.getString("id"));
        mt.setMonsterResistanceId(rs.getString("monster_resistance_id"));
        mt.setTags(rs.getString("tags"));
        return mt;
    }
}
