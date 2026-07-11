package com.poe.cache.dao;

import com.poe.cache.model.MonsterMapMultiplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * monster_map_multipliers 表数据访问对象。
 * <p>
 * 表使用 level 作为主键（自然键），记录各地图等级怪物/Boss 的倍率。
 */
public class MonsterMapMultiplierDao implements CrudRepository<MonsterMapMultiplier, Integer> {

    private final javax.sql.DataSource dataSource;

    public MonsterMapMultiplierDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 插入单条地图怪物倍率记录。
     *
     * @param entity 地图怪物倍率实体
     */
    @Override
    public void insert(MonsterMapMultiplier entity) {
        String sql = "INSERT INTO monster_map_multipliers (level, boss_damage, boss_item_quantity, "
            + "boss_item_rarity, boss_life, damage, life) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert monster map multiplier: " + entity.getLevel(), e);
        }
    }

    /**
     * 批量插入地图怪物倍率记录（事务）。
     *
     * @param entities 地图怪物倍率实体列表（非空）
     */
    @Override
    public void batchInsert(List<MonsterMapMultiplier> entities) {
        String sql = "INSERT INTO monster_map_multipliers (level, boss_damage, boss_item_quantity, "
            + "boss_item_rarity, boss_life, damage, life) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (MonsterMapMultiplier entity : entities) {
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
            throw new RuntimeException("Failed to batch insert monster map multipliers", e);
        }
    }

    /**
     * 根据等级查询地图怪物倍率。
     *
     * @param level 地图等级
     * @return 地图怪物倍率实体（可能为空）
     */
    @Override
    public Optional<MonsterMapMultiplier> findById(Integer level) {
        String sql = "SELECT * FROM monster_map_multipliers WHERE level = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, level);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find monster map multiplier by level: " + level, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部地图怪物倍率记录。
     *
     * @return 按 level 升序排列的地图怪物倍率列表
     */
    @Override
    public List<MonsterMapMultiplier> findAll() {
        List<MonsterMapMultiplier> list = new ArrayList<>();
        String sql = "SELECT * FROM monster_map_multipliers ORDER BY level";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all monster map multipliers", e);
        }
        return list;
    }

    /**
     * 根据等级删除地图怪物倍率。
     *
     * @param level 地图等级
     */
    @Override
    public void deleteById(Integer level) {
        String sql = "DELETE FROM monster_map_multipliers WHERE level = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, level);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete monster map multiplier: " + level, e);
        }
    }

    /**
     * 统计地图怪物倍率记录总数。
     *
     * @return monster_map_multipliers 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM monster_map_multipliers";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count monster map multipliers", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, MonsterMapMultiplier mmm) throws SQLException {
        ps.setInt(1, mmm.getLevel());
        ps.setInt(2, mmm.getBossDamage());
        ps.setInt(3, mmm.getBossItemQuantity());
        ps.setInt(4, mmm.getBossItemRarity());
        ps.setInt(5, mmm.getBossLife());
        ps.setInt(6, mmm.getDamage());
        ps.setInt(7, mmm.getLife());
    }

    private MonsterMapMultiplier mapRow(ResultSet rs) throws SQLException {
        MonsterMapMultiplier mmm = new MonsterMapMultiplier();
        mmm.setLevel(rs.getInt("level"));
        mmm.setBossDamage(rs.getInt("boss_damage"));
        mmm.setBossItemQuantity(rs.getInt("boss_item_quantity"));
        mmm.setBossItemRarity(rs.getInt("boss_item_rarity"));
        mmm.setBossLife(rs.getInt("boss_life"));
        mmm.setDamage(rs.getInt("damage"));
        mmm.setLife(rs.getInt("life"));
        return mmm;
    }
}
