package com.poe.cache.dao;

import com.poe.cache.model.SkillGem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * skill_gems 表数据访问对象。
 */
public class SkillGemDao implements CrudRepository<SkillGem, Integer> {

    private final Connection connection;

    public SkillGemDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(SkillGem gem) {
        String sql = "INSERT INTO skill_gems (id, name, name_zh, gem_type, gem_tags, "
            + "primary_attribute, description, quality_stats, level_stats, "
            + "required_level, is_vaal_skill_gem, support_gem_letter, support_gem_letter_html, "
            + "requires_intelligence, requires_dexterity, requires_strength, "
            + "awakened_variant_id, regular_variant_id, vaal_variant_id, "
            + "secondary_skill_id, ruthless_skill_id, ruthless_secondary_skill_id, version) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, gem);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert skill gem: " + gem.getId(), e);
        }
    }

    @Override
    public void batchInsert(List<SkillGem> gems) {
        String sql = "INSERT INTO skill_gems (id, name, name_zh, gem_type, gem_tags, "
            + "primary_attribute, description, quality_stats, level_stats, "
            + "required_level, is_vaal_skill_gem, support_gem_letter, support_gem_letter_html, "
            + "requires_intelligence, requires_dexterity, requires_strength, "
            + "awakened_variant_id, regular_variant_id, vaal_variant_id, "
            + "secondary_skill_id, ruthless_skill_id, ruthless_secondary_skill_id, version) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (SkillGem gem : gems) {
                    setParams(ps, gem);
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
            throw new RuntimeException("Failed to batch insert skill gems", e);
        }
    }

    @Override
    public Optional<SkillGem> findById(Integer id) {
        String sql = "SELECT * FROM skill_gems WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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

    @Override
    public List<SkillGem> findAll() {
        String sql = "SELECT * FROM skill_gems ORDER BY id";
        List<SkillGem> gems = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                gems.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all skill gems", e);
        }
        return gems;
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM skill_gems WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete skill gem by id: " + id, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM skill_gems";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count skill gems", e);
        }
        return 0;
    }

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
