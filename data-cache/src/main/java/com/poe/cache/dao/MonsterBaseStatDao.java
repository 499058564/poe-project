package com.poe.cache.dao;

import com.poe.cache.model.MonsterBaseStat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * monster_base_stats 表数据访问对象。
 * <p>
 * 表使用 level 作为主键（自然键），记录各等级怪物的基础属性。
 */
public class MonsterBaseStatDao implements CrudRepository<MonsterBaseStat, Integer> {

    private final Connection connection;

    public MonsterBaseStatDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * 插入单条怪物基础属性记录。
     *
     * @param entity 怪物基础属性实体
     */
    @Override
    public void insert(MonsterBaseStat entity) {
        String sql = "INSERT INTO monster_base_stats (level, accuracy, armour, damage, "
            + "evasion, experience, life, summon_life) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert monster base stat: " + entity.getLevel(), e);
        }
    }

    /**
     * 批量插入怪物基础属性记录（事务）。
     *
     * @param entities 怪物基础属性实体列表（非空）
     */
    @Override
    public void batchInsert(List<MonsterBaseStat> entities) {
        String sql = "INSERT INTO monster_base_stats (level, accuracy, armour, damage, "
            + "evasion, experience, life, summon_life) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (MonsterBaseStat entity : entities) {
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
            throw new RuntimeException("Failed to batch insert monster base stats", e);
        }
    }

    /**
     * 根据等级查询怪物基础属性。
     *
     * @param level 怪物等级
     * @return 怪物基础属性实体（可能为空）
     */
    @Override
    public Optional<MonsterBaseStat> findById(Integer level) {
        String sql = "SELECT * FROM monster_base_stats WHERE level = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, level);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find monster base stat by level: " + level, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部怪物基础属性记录。
     *
     * @return 按 level 升序排列的怪物基础属性列表
     */
    @Override
    public List<MonsterBaseStat> findAll() {
        List<MonsterBaseStat> list = new ArrayList<>();
        String sql = "SELECT * FROM monster_base_stats ORDER BY level";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all monster base stats", e);
        }
        return list;
    }

    /**
     * 根据等级删除怪物基础属性。
     *
     * @param level 怪物等级
     */
    @Override
    public void deleteById(Integer level) {
        String sql = "DELETE FROM monster_base_stats WHERE level = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, level);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete monster base stat: " + level, e);
        }
    }

    /**
     * 统计怪物基础属性记录总数。
     *
     * @return monster_base_stats 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM monster_base_stats";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count monster base stats", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, MonsterBaseStat mbs) throws SQLException {
        ps.setInt(1, mbs.getLevel());
        ps.setInt(2, mbs.getAccuracy());
        ps.setInt(3, mbs.getArmour());
        ps.setDouble(4, mbs.getDamage());
        ps.setInt(5, mbs.getEvasion());
        ps.setInt(6, mbs.getExperience());
        ps.setInt(7, mbs.getLife());
        ps.setInt(8, mbs.getSummonLife());
    }

    private MonsterBaseStat mapRow(ResultSet rs) throws SQLException {
        MonsterBaseStat mbs = new MonsterBaseStat();
        mbs.setLevel(rs.getInt("level"));
        mbs.setAccuracy(rs.getInt("accuracy"));
        mbs.setArmour(rs.getInt("armour"));
        mbs.setDamage(rs.getDouble("damage"));
        mbs.setEvasion(rs.getInt("evasion"));
        mbs.setExperience(rs.getInt("experience"));
        mbs.setLife(rs.getInt("life"));
        mbs.setSummonLife(rs.getInt("summon_life"));
        return mbs;
    }
}
