package com.poe.cache.dao;

import com.poe.cache.model.SkillLevel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * skill_levels 表数据访问对象。
 * <p>
 * 表使用 (page_id, level) 作为复合主键，记录每个技能等级的详细数值。
 */
public class SkillLevelDao implements CrudRepository<SkillLevel, Integer> {

    private final Connection connection;

    public SkillLevelDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * 插入单条技能等级记录。
     *
     * @param entity 技能等级实体
     */
    @Override
    public void insert(SkillLevel entity) {
        String sql = "INSERT INTO skill_levels (page_id, page_name, attack_speed_multiplier, "
            + "attack_time, cooldown, cost_amounts, cost_multiplier, cost_types, "
            + "critical_strike_chance, damage_effectiveness, damage_multiplier, "
            + "dexterity_requirement, duration, experience, intelligence_requirement, "
            + "level, level_requirement, life_reservation_flat, life_reservation_percent, "
            + "mana_reservation_flat, mana_reservation_percent, skill_level, stat_text, "
            + "stored_uses, strength_requirement, vaal_soul_gain_prevention_time, "
            + "vaal_souls_requirement, vaal_stored_uses) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert skill_level: " + entity.getPageId(), e);
        }
    }

    /**
     * 批量插入技能等级记录（事务）。
     *
     * @param entities 技能等级实体列表（非空）
     */
    @Override
    public void batchInsert(List<SkillLevel> entities) {
        String sql = "INSERT INTO skill_levels (page_id, page_name, attack_speed_multiplier, "
            + "attack_time, cooldown, cost_amounts, cost_multiplier, cost_types, "
            + "critical_strike_chance, damage_effectiveness, damage_multiplier, "
            + "dexterity_requirement, duration, experience, intelligence_requirement, "
            + "level, level_requirement, life_reservation_flat, life_reservation_percent, "
            + "mana_reservation_flat, mana_reservation_percent, skill_level, stat_text, "
            + "stored_uses, strength_requirement, vaal_soul_gain_prevention_time, "
            + "vaal_souls_requirement, vaal_stored_uses) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (SkillLevel entity : entities) {
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
            throw new RuntimeException("Failed to batch insert skill_levels", e);
        }
    }

    /**
     * 按 page_id 查询第一条技能等级。
     *
     * @param pageId Wiki 页面 ID
     * @return 技能等级实体（可能为空）
     */
    @Override
    public Optional<SkillLevel> findById(Integer pageId) {
        String sql = "SELECT * FROM skill_levels WHERE page_id = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find skill_level by id: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部技能等级记录。
     *
     * @return 按 page_id, level 升序排列的技能等级列表
     */
    @Override
    public List<SkillLevel> findAll() {
        List<SkillLevel> list = new ArrayList<>();
        String sql = "SELECT * FROM skill_levels ORDER BY page_id, level";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all skill_levels", e);
        }
        return list;
    }

    /**
     * 按 page_id 删除该技能的所有等级。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM skill_levels WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete skill_levels: " + pageId, e);
        }
    }

    /**
     * 统计技能等级记录总数。
     *
     * @return skill_levels 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM skill_levels";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count skill_levels", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, SkillLevel sl) throws SQLException {
        ps.setInt(1, sl.getPageId());
        ps.setString(2, sl.getPageName());
        ps.setInt(3, sl.getAttackSpeedMultiplier());
        ps.setDouble(4, sl.getAttackTime());
        ps.setDouble(5, sl.getCooldown());
        ps.setString(6, sl.getCostAmounts());
        ps.setDouble(7, sl.getCostMultiplier());
        ps.setString(8, sl.getCostTypes());
        ps.setDouble(9, sl.getCriticalStrikeChance());
        ps.setDouble(10, sl.getDamageEffectiveness());
        ps.setDouble(11, sl.getDamageMultiplier());
        ps.setInt(12, sl.getDexterityRequirement());
        ps.setDouble(13, sl.getDuration());
        ps.setInt(14, sl.getExperience());
        ps.setInt(15, sl.getIntelligenceRequirement());
        ps.setInt(16, sl.getLevel());
        ps.setInt(17, sl.getLevelRequirement());
        ps.setInt(18, sl.getLifeReservationFlat());
        ps.setInt(19, sl.getLifeReservationPercent());
        ps.setInt(20, sl.getManaReservationFlat());
        ps.setInt(21, sl.getManaReservationPercent());
        ps.setInt(22, sl.getSkillLevel());
        ps.setString(23, sl.getStatText());
        ps.setInt(24, sl.getStoredUses());
        ps.setInt(25, sl.getStrengthRequirement());
        ps.setDouble(26, sl.getVaalSoulGainPreventionTime());
        ps.setInt(27, sl.getVaalSoulsRequirement());
        ps.setInt(28, sl.getVaalStoredUses());
    }

    private SkillLevel mapRow(ResultSet rs) throws SQLException {
        SkillLevel sl = new SkillLevel();
        sl.setPageId(rs.getInt("page_id"));
        sl.setPageName(rs.getString("page_name"));
        sl.setAttackSpeedMultiplier(rs.getInt("attack_speed_multiplier"));
        sl.setAttackTime(rs.getDouble("attack_time"));
        sl.setCooldown(rs.getDouble("cooldown"));
        sl.setCostAmounts(rs.getString("cost_amounts"));
        sl.setCostMultiplier(rs.getDouble("cost_multiplier"));
        sl.setCostTypes(rs.getString("cost_types"));
        sl.setCriticalStrikeChance(rs.getDouble("critical_strike_chance"));
        sl.setDamageEffectiveness(rs.getDouble("damage_effectiveness"));
        sl.setDamageMultiplier(rs.getDouble("damage_multiplier"));
        sl.setDexterityRequirement(rs.getInt("dexterity_requirement"));
        sl.setDuration(rs.getDouble("duration"));
        sl.setExperience(rs.getInt("experience"));
        sl.setIntelligenceRequirement(rs.getInt("intelligence_requirement"));
        sl.setLevel(rs.getInt("level"));
        sl.setLevelRequirement(rs.getInt("level_requirement"));
        sl.setLifeReservationFlat(rs.getInt("life_reservation_flat"));
        sl.setLifeReservationPercent(rs.getInt("life_reservation_percent"));
        sl.setManaReservationFlat(rs.getInt("mana_reservation_flat"));
        sl.setManaReservationPercent(rs.getInt("mana_reservation_percent"));
        sl.setSkillLevel(rs.getInt("skill_level"));
        sl.setStatText(rs.getString("stat_text"));
        sl.setStoredUses(rs.getInt("stored_uses"));
        sl.setStrengthRequirement(rs.getInt("strength_requirement"));
        sl.setVaalSoulGainPreventionTime(rs.getDouble("vaal_soul_gain_prevention_time"));
        sl.setVaalSoulsRequirement(rs.getInt("vaal_souls_requirement"));
        sl.setVaalStoredUses(rs.getInt("vaal_stored_uses"));
        return sl;
    }
}
