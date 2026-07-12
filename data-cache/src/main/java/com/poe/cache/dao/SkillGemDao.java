package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.SkillGem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * skill_gems 表数据访问对象。
 * <p>
 * 表使用 id 作为主键。
 * 记录技能宝石的完整信息，包括类型、标签、属性需求、变体和版本信息。
 */
public class SkillGemDao implements CrudRepository<SkillGem, Integer> {

    private final DataSource dataSource;

    public SkillGemDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 插入单条技能宝石记录。
     *
     * @param gem 技能宝石实体
     */
    @Override
    public void insert(SkillGem gem) {
        String sql = "INSERT INTO skill_gems (id, name, name_zh, gem_type, gem_tags, "
            + "primary_attribute, description, quality_stats, level_stats, "
            + "required_level, is_vaal_skill_gem, support_gem_letter, support_gem_letter_html, "
            + "requires_intelligence, requires_dexterity, requires_strength, "
            + "awakened_variant_id, regular_variant_id, vaal_variant_id, "
            + "secondary_skill_id, ruthless_skill_id, ruthless_secondary_skill_id, version) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, gem);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert skill gem: " + gem.getId(), e);
        }
    }

    /**
     * 批量插入技能宝石记录（事务）。
     * <p>
     * 使用 JDBC batch + 事务保证原子性，失败自动回滚。
     *
     * @param gems 技能宝石实体列表（非空）
     */
    @Override
    public void batchInsert(List<SkillGem> gems) {
        String sql = "INSERT INTO skill_gems (id, name, name_zh, gem_type, gem_tags, "
            + "primary_attribute, description, quality_stats, level_stats, "
            + "required_level, is_vaal_skill_gem, support_gem_letter, support_gem_letter_html, "
            + "requires_intelligence, requires_dexterity, requires_strength, "
            + "awakened_variant_id, regular_variant_id, vaal_variant_id, "
            + "secondary_skill_id, ruthless_skill_id, ruthless_secondary_skill_id, version) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (SkillGem gem : gems) {
                    setParams(ps, gem);
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
            throw new RuntimeException("Failed to batch insert skill gems", e);
        }
    }

    /**
     * 根据主键查询技能宝石。
     *
     * @param id 技能宝石 ID
     * @return 技能宝石实体（可能为空）
     */
    @Override
    public Optional<SkillGem> findById(Integer id) {
        String sql = "SELECT * FROM skill_gems WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find skill gem by id: " + id, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部技能宝石记录。
     *
     * @return 按 id 升序排列的技能宝石列表
     */
    @Override
    public List<SkillGem> findAll() {
        String sql = "SELECT * FROM skill_gems ORDER BY id";
        List<SkillGem> gems = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                gems.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all skill gems", e);
        }
        return gems;
    }

    /**
     * 根据主键删除技能宝石。
     *
     * @param id 技能宝石 ID
     */
    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM skill_gems WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete skill gem by id: " + id, e);
        }
    }

    /**
     * 统计技能宝石记录总数。
     *
     * @return skill_gems 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM skill_gems";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count skill gems", e);
        }
        return 0;
    }

    /** 设置 PreparedStatement 参数（绑定 SkillGem 字段） */
    private void setParams(PreparedStatement ps, SkillGem gem) throws SQLException {
        int i = 1;
        ps.setInt(i++, gem.getId());
        ps.setString(i++, gem.getName());
        ps.setString(i++, gem.getNameZh());
        ps.setString(i++, gem.getGemType());
        ps.setString(i++, gem.getGemTags());
        ps.setString(i++, gem.getPrimaryAttribute());
        ps.setString(i++, gem.getDescription());
        ps.setString(i++, gem.getQualityStats());
        ps.setString(i++, gem.getLevelStats());
        ps.setInt(i++, gem.getRequiredLevel());
        ps.setInt(i++, gem.isVaalSkillGem() ? 1 : 0);
        ps.setString(i++, gem.getSupportGemLetter());
        ps.setString(i++, gem.getSupportGemLetterHtml());
        ps.setInt(i++, gem.isRequiresIntelligence() ? 1 : 0);
        ps.setInt(i++, gem.isRequiresDexterity() ? 1 : 0);
        ps.setInt(i++, gem.isRequiresStrength() ? 1 : 0);
        ps.setString(i++, gem.getAwakenedVariantId());
        ps.setString(i++, gem.getRegularVariantId());
        ps.setString(i++, gem.getVaalVariantId());
        ps.setString(i++, gem.getSecondarySkillId());
        ps.setString(i++, gem.getRuthlessSkillId());
        ps.setString(i++, gem.getRuthlessSecondarySkillId());
        ps.setString(i++, gem.getVersion());
    }

    /** 从 ResultSet 映射一行到 SkillGem 实体 */
    private SkillGem mapRow(ResultSet rs) throws SQLException {
        SkillGem gem = new SkillGem();
        gem.setId(rs.getInt("id"));
        gem.setName(rs.getString("name"));
        gem.setNameZh(rs.getString("name_zh"));
        gem.setGemType(rs.getString("gem_type"));
        gem.setGemTags(rs.getString("gem_tags"));
        gem.setPrimaryAttribute(rs.getString("primary_attribute"));
        gem.setDescription(rs.getString("description"));
        gem.setQualityStats(rs.getString("quality_stats"));
        gem.setLevelStats(rs.getString("level_stats"));
        gem.setRequiredLevel(rs.getInt("required_level"));
        gem.setVaalSkillGem(rs.getInt("is_vaal_skill_gem") == 1);
        gem.setSupportGemLetter(rs.getString("support_gem_letter"));
        gem.setSupportGemLetterHtml(rs.getString("support_gem_letter_html"));
        gem.setRequiresIntelligence(rs.getInt("requires_intelligence") == 1);
        gem.setRequiresDexterity(rs.getInt("requires_dexterity") == 1);
        gem.setRequiresStrength(rs.getInt("requires_strength") == 1);
        gem.setAwakenedVariantId(rs.getString("awakened_variant_id"));
        gem.setRegularVariantId(rs.getString("regular_variant_id"));
        gem.setVaalVariantId(rs.getString("vaal_variant_id"));
        gem.setSecondarySkillId(rs.getString("secondary_skill_id"));
        gem.setRuthlessSkillId(rs.getString("ruthless_skill_id"));
        gem.setRuthlessSecondarySkillId(rs.getString("ruthless_secondary_skill_id"));
        gem.setVersion(rs.getString("version"));
        return gem;
    }
}
