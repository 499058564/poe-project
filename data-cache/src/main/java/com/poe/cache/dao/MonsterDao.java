package com.poe.cache.dao;

import com.poe.cache.model.Monster;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * monsters 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键，记录怪物属性、标签与技能信息。
 */
public class MonsterDao implements CrudRepository<Monster, Integer> {

    private final javax.sql.DataSource dataSource;

    public MonsterDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 插入单条怪物记录。
     *
     * @param entity 怪物实体
     */
    @Override
    public void insert(Monster entity) {
        String sql = "INSERT INTO monsters (page_id, page_name, attack_speed, critical_strike_chance, "
            + "damage_multiplier, endgame_mod_ids, experience_multiplier, health_multiplier, "
            + "is_boss, maximum_attack_distance, metadata_id, minimum_attack_distance, "
            + "mod_ids, model_size_multiplier, monster_type_id, name, part1_mod_ids, "
            + "part2_mod_ids, rarity, rarity_id, size, skill_ids, tags) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert monster: " + entity.getPageId(), e);
        }
    }

    /**
     * 批量插入怪物记录（事务）。
     *
     * @param entities 怪物实体列表（非空）
     */
    @Override
    public void batchInsert(List<Monster> entities) {
        String sql = "INSERT INTO monsters (page_id, page_name, attack_speed, critical_strike_chance, "
            + "damage_multiplier, endgame_mod_ids, experience_multiplier, health_multiplier, "
            + "is_boss, maximum_attack_distance, metadata_id, minimum_attack_distance, "
            + "mod_ids, model_size_multiplier, monster_type_id, name, part1_mod_ids, "
            + "part2_mod_ids, rarity, rarity_id, size, skill_ids, tags) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Monster entity : entities) {
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
            throw new RuntimeException("Failed to batch insert monsters", e);
        }
    }

    /**
     * 根据主键查询怪物。
     *
     * @param pageId Wiki 页面 ID
     * @return 怪物实体（可能为空）
     */
    @Override
    public Optional<Monster> findById(Integer pageId) {
        String sql = "SELECT * FROM monsters WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find monster by id: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部怪物记录。
     *
     * @return 按 page_id 升序排列的怪物列表
     */
    @Override
    public List<Monster> findAll() {
        List<Monster> list = new ArrayList<>();
        String sql = "SELECT * FROM monsters ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all monsters", e);
        }
        return list;
    }

    /**
     * 根据主键删除怪物。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM monsters WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete monster: " + pageId, e);
        }
    }

    /**
     * 统计怪物记录总数。
     *
     * @return monsters 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM monsters";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count monsters", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Monster m) throws SQLException {
        ps.setInt(1, m.getPageId());
        ps.setString(2, m.getPageName());
        ps.setInt(3, m.getAttackSpeed());
        ps.setInt(4, m.getCriticalStrikeChance());
        ps.setDouble(5, m.getDamageMultiplier());
        ps.setString(6, m.getEndgameModIds());
        ps.setDouble(7, m.getExperienceMultiplier());
        ps.setDouble(8, m.getHealthMultiplier());
        ps.setInt(9, m.isBoss() ? 1 : 0);
        ps.setInt(10, m.getMaximumAttackDistance());
        ps.setString(11, m.getMetadataId());
        ps.setInt(12, m.getMinimumAttackDistance());
        ps.setString(13, m.getModIds());
        ps.setDouble(14, m.getModelSizeMultiplier());
        ps.setString(15, m.getMonsterTypeId());
        ps.setString(16, m.getName());
        ps.setString(17, m.getPart1ModIds());
        ps.setString(18, m.getPart2ModIds());
        ps.setString(19, m.getRarity());
        ps.setString(20, m.getRarityId());
        ps.setInt(21, m.getSize());
        ps.setString(22, m.getSkillIds());
        ps.setString(23, m.getTags());
    }

    private Monster mapRow(ResultSet rs) throws SQLException {
        Monster m = new Monster();
        m.setPageId(rs.getInt("page_id"));
        m.setPageName(rs.getString("page_name"));
        m.setAttackSpeed(rs.getInt("attack_speed"));
        m.setCriticalStrikeChance(rs.getInt("critical_strike_chance"));
        m.setDamageMultiplier(rs.getDouble("damage_multiplier"));
        m.setEndgameModIds(rs.getString("endgame_mod_ids"));
        m.setExperienceMultiplier(rs.getDouble("experience_multiplier"));
        m.setHealthMultiplier(rs.getDouble("health_multiplier"));
        m.setBoss(rs.getInt("is_boss") == 1);
        m.setMaximumAttackDistance(rs.getInt("maximum_attack_distance"));
        m.setMetadataId(rs.getString("metadata_id"));
        m.setMinimumAttackDistance(rs.getInt("minimum_attack_distance"));
        m.setModIds(rs.getString("mod_ids"));
        m.setModelSizeMultiplier(rs.getDouble("model_size_multiplier"));
        m.setMonsterTypeId(rs.getString("monster_type_id"));
        m.setName(rs.getString("name"));
        m.setPart1ModIds(rs.getString("part1_mod_ids"));
        m.setPart2ModIds(rs.getString("part2_mod_ids"));
        m.setRarity(rs.getString("rarity"));
        m.setRarityId(rs.getString("rarity_id"));
        m.setSize(rs.getInt("size"));
        m.setSkillIds(rs.getString("skill_ids"));
        m.setTags(rs.getString("tags"));
        return m;
    }
}
