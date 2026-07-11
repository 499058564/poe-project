package com.poe.cache.dao;

import com.poe.cache.model.MonsterLifeScaling;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * monster_life_scaling 表数据访问对象。
 * <p>
 * 表使用 level 作为主键（自然键），记录各等级怪物的生命倍率。
 */
public class MonsterLifeScalingDao implements CrudRepository<MonsterLifeScaling, Integer> {

    private final Connection connection;

    public MonsterLifeScalingDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * 插入单条怪物生命倍率记录。
     *
     * @param entity 怪物生命倍率实体
     */
    @Override
    public void insert(MonsterLifeScaling entity) {
        String sql = "INSERT INTO monster_life_scaling (level, magic, rare) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert monster life scaling: " + entity.getLevel(), e);
        }
    }

    /**
     * 批量插入怪物生命倍率记录（事务）。
     *
     * @param entities 怪物生命倍率实体列表（非空）
     */
    @Override
    public void batchInsert(List<MonsterLifeScaling> entities) {
        String sql = "INSERT INTO monster_life_scaling (level, magic, rare) VALUES (?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (MonsterLifeScaling entity : entities) {
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
            throw new RuntimeException("Failed to batch insert monster life scalings", e);
        }
    }

    /**
     * 根据等级查询怪物生命倍率。
     *
     * @param level 怪物等级
     * @return 怪物生命倍率实体（可能为空）
     */
    @Override
    public Optional<MonsterLifeScaling> findById(Integer level) {
        String sql = "SELECT * FROM monster_life_scaling WHERE level = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, level);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find monster life scaling by level: " + level, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部怪物生命倍率记录。
     *
     * @return 按 level 升序排列的怪物生命倍率列表
     */
    @Override
    public List<MonsterLifeScaling> findAll() {
        List<MonsterLifeScaling> list = new ArrayList<>();
        String sql = "SELECT * FROM monster_life_scaling ORDER BY level";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all monster life scalings", e);
        }
        return list;
    }

    /**
     * 根据等级删除怪物生命倍率。
     *
     * @param level 怪物等级
     */
    @Override
    public void deleteById(Integer level) {
        String sql = "DELETE FROM monster_life_scaling WHERE level = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, level);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete monster life scaling: " + level, e);
        }
    }

    /**
     * 统计怪物生命倍率记录总数。
     *
     * @return monster_life_scaling 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM monster_life_scaling";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count monster life scalings", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, MonsterLifeScaling mls) throws SQLException {
        ps.setInt(1, mls.getLevel());
        ps.setInt(2, mls.getMagic());
        ps.setInt(3, mls.getRare());
    }

    private MonsterLifeScaling mapRow(ResultSet rs) throws SQLException {
        MonsterLifeScaling mls = new MonsterLifeScaling();
        mls.setLevel(rs.getInt("level"));
        mls.setMagic(rs.getInt("magic"));
        mls.setRare(rs.getInt("rare"));
        return mls;
    }
}
