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
        String sql = "INSERT INTO skill_gems (id, name, name_zh, gem_type, gem_tags, " +
            "primary_attribute, description, quality_stats, level_stats, " +
            "required_level, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, gem);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert skill gem: " + gem.getId(), e);
        }
    }

    /**
     * 批量插入，在单个事务内执行以提高性能。
     * 任一行插入失败时，整个批次回滚并恢复自动提交模式。
     */
    @Override
    public void batchInsert(List<SkillGem> gems) {
        String sql = "INSERT INTO skill_gems (id, name, name_zh, gem_type, gem_tags, " +
            "primary_attribute, description, quality_stats, level_stats, " +
            "required_level, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
        ps.setInt(1, gem.getId());
        ps.setString(2, gem.getName());
        ps.setString(3, gem.getNameZh());
        ps.setString(4, gem.getGemType());
        ps.setString(5, gem.getGemTags());
        ps.setString(6, gem.getPrimaryAttribute());
        ps.setString(7, gem.getDescription());
        ps.setString(8, gem.getQualityStats());
        ps.setString(9, gem.getLevelStats());
        ps.setInt(10, gem.getRequiredLevel());
        ps.setString(11, gem.getVersion());
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
        gem.setVersion(rs.getString("version"));
        return gem;
    }
}
