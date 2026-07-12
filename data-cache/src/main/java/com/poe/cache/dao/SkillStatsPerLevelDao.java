package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.SkillStatsPerLevel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * skill_stats_per_level 表数据访问对象。
 * <p>
 * 表使用 (page_id, stat_id, level) 作为复合主键，记录技能各等级属性数值。
 */
public class SkillStatsPerLevelDao implements CrudRepository<SkillStatsPerLevel, Integer> {

    private final DataSource dataSource;

    public SkillStatsPerLevelDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 插入单条技能属性记录。
     *
     * @param entity 技能属性实体
     */
    @Override
    public void insert(SkillStatsPerLevel entity) {
        String sql = "INSERT INTO skill_stats_per_level (page_id, page_name, stat_id, level, value) "
            + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert skill_stats_per_level: " + entity.getPageId(), e);
        }
    }

    /**
     * 批量插入技能属性记录（事务）。
     *
     * @param entities 技能属性实体列表（非空）
     */
    @Override
    public void batchInsert(List<SkillStatsPerLevel> entities) {
        String sql = "INSERT INTO skill_stats_per_level (page_id, page_name, stat_id, level, value) "
            + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (SkillStatsPerLevel entity : entities) {
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
            throw new RuntimeException("Failed to batch insert skill_stats_per_level", e);
        }
    }

    /**
     * 按 page_id 查询第一条技能属性。
     *
     * @param pageId Wiki 页面 ID
     * @return 技能属性实体（可能为空）
     */
    @Override
    public Optional<SkillStatsPerLevel> findById(Integer pageId) {
        String sql = "SELECT * FROM skill_stats_per_level WHERE page_id = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find skill_stats_per_level by id: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部技能属性记录。
     *
     * @return 按 page_id, stat_id, level 升序排列的列表
     */
    @Override
    public List<SkillStatsPerLevel> findAll() {
        List<SkillStatsPerLevel> list = new ArrayList<>();
        String sql = "SELECT * FROM skill_stats_per_level ORDER BY page_id, stat_id, level";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all skill_stats_per_level", e);
        }
        return list;
    }

    /**
     * 按 page_id 删除该技能的所有属性记录。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM skill_stats_per_level WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete skill_stats_per_level: " + pageId, e);
        }
    }

    /**
     * 统计技能属性记录总数。
     *
     * @return skill_stats_per_level 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM skill_stats_per_level";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count skill_stats_per_level", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, SkillStatsPerLevel s) throws SQLException {
        ps.setInt(1, s.getPageId());
        ps.setString(2, s.getPageName());
        ps.setString(3, s.getStatId());
        ps.setInt(4, s.getLevel());
        ps.setInt(5, s.getValue());
    }

    private SkillStatsPerLevel mapRow(ResultSet rs) throws SQLException {
        SkillStatsPerLevel s = new SkillStatsPerLevel();
        s.setPageId(rs.getInt("page_id"));
        s.setPageName(rs.getString("page_name"));
        s.setStatId(rs.getString("stat_id"));
        s.setLevel(rs.getInt("level"));
        s.setValue(rs.getInt("value"));
        return s;
    }
}
