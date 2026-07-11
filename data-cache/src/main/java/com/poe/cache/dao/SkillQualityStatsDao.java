package com.poe.cache.dao;

import com.poe.cache.model.SkillQualityStats;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * skill_quality_stats 表数据访问对象。
 * <p>
 * 表使用 (page_id, stat_id, set_id) 作为复合主键，记录技能品质各属性的数值。
 */
public class SkillQualityStatsDao implements CrudRepository<SkillQualityStats, Integer> {

    private final Connection connection;

    public SkillQualityStatsDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * 插入单条技能品质属性记录。
     *
     * @param entity 技能品质属性实体
     */
    @Override
    public void insert(SkillQualityStats entity) {
        String sql = "INSERT INTO skill_quality_stats (page_id, page_name, stat_id, set_id, value) "
            + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert skill_quality_stats: " + entity.getPageId(), e);
        }
    }

    /**
     * 批量插入技能品质属性记录（事务）。
     *
     * @param entities 技能品质属性实体列表（非空）
     */
    @Override
    public void batchInsert(List<SkillQualityStats> entities) {
        String sql = "INSERT INTO skill_quality_stats (page_id, page_name, stat_id, set_id, value) "
            + "VALUES (?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (SkillQualityStats entity : entities) {
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
            throw new RuntimeException("Failed to batch insert skill_quality_stats", e);
        }
    }

    /**
     * 按 page_id 查询第一条技能品质属性。
     *
     * @param pageId Wiki 页面 ID
     * @return 技能品质属性实体（可能为空）
     */
    @Override
    public Optional<SkillQualityStats> findById(Integer pageId) {
        String sql = "SELECT * FROM skill_quality_stats WHERE page_id = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find skill_quality_stats by id: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部技能品质属性记录。
     *
     * @return 按 page_id, stat_id, set_id 升序排列的列表
     */
    @Override
    public List<SkillQualityStats> findAll() {
        List<SkillQualityStats> list = new ArrayList<>();
        String sql = "SELECT * FROM skill_quality_stats ORDER BY page_id, stat_id, set_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all skill_quality_stats", e);
        }
        return list;
    }

    /**
     * 按 page_id 删除该技能的所有品质属性。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM skill_quality_stats WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete skill_quality_stats: " + pageId, e);
        }
    }

    /**
     * 统计技能品质属性记录总数。
     *
     * @return skill_quality_stats 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM skill_quality_stats";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count skill_quality_stats", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, SkillQualityStats s) throws SQLException {
        ps.setInt(1, s.getPageId());
        ps.setString(2, s.getPageName());
        ps.setString(3, s.getStatId());
        ps.setInt(4, s.getSetId());
        ps.setInt(5, s.getValue());
    }

    private SkillQualityStats mapRow(ResultSet rs) throws SQLException {
        SkillQualityStats s = new SkillQualityStats();
        s.setPageId(rs.getInt("page_id"));
        s.setPageName(rs.getString("page_name"));
        s.setStatId(rs.getString("stat_id"));
        s.setSetId(rs.getInt("set_id"));
        s.setValue(rs.getInt("value"));
        return s;
    }
}
