package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.SkillQuality;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * skill_quality 表数据访问对象。
 * <p>
 * 表使用 (page_id, set_id) 作为复合主键，记录技能品质方案的属性文本和权重。
 */
public class SkillQualityDao implements CrudRepository<SkillQuality, Integer> {

    private final DataSource dataSource;

    public SkillQualityDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 插入单条技能品质记录。
     *
     * @param entity 技能品质实体
     */
    @Override
    public void insert(SkillQuality entity) {
        String sql = "INSERT INTO skill_quality (page_id, page_name, set_id, stat_text, weight) "
            + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert skill_quality: " + entity.getPageId(), e);
        }
    }

    /**
     * 批量插入技能品质记录（事务）。
     *
     * @param entities 技能品质实体列表（非空）
     */
    @Override
    public void batchInsert(List<SkillQuality> entities) {
        String sql = "INSERT INTO skill_quality (page_id, page_name, set_id, stat_text, weight) "
            + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (SkillQuality entity : entities) {
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
            throw new RuntimeException("Failed to batch insert skill_quality", e);
        }
    }

    /**
     * 按 page_id 查询第一条技能品质。
     *
     * @param pageId Wiki 页面 ID
     * @return 技能品质实体（可能为空）
     */
    @Override
    public Optional<SkillQuality> findById(Integer pageId) {
        String sql = "SELECT * FROM skill_quality WHERE page_id = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find skill_quality by id: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部技能品质记录。
     *
     * @return 按 page_id, set_id 升序排列的列表
     */
    @Override
    public List<SkillQuality> findAll() {
        List<SkillQuality> list = new ArrayList<>();
        String sql = "SELECT * FROM skill_quality ORDER BY page_id, set_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all skill_quality", e);
        }
        return list;
    }

    /**
     * 按 page_id 删除该技能的所有品质记录。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM skill_quality WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete skill_quality: " + pageId, e);
        }
    }

    /**
     * 统计技能品质记录总数。
     *
     * @return skill_quality 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM skill_quality";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count skill_quality", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, SkillQuality sq) throws SQLException {
        ps.setInt(1, sq.getPageId());
        ps.setString(2, sq.getPageName());
        ps.setInt(3, sq.getSetId());
        ps.setString(4, sq.getStatText());
        ps.setInt(5, sq.getWeight());
    }

    private SkillQuality mapRow(ResultSet rs) throws SQLException {
        SkillQuality sq = new SkillQuality();
        sq.setPageId(rs.getInt("page_id"));
        sq.setPageName(rs.getString("page_name"));
        sq.setSetId(rs.getInt("set_id"));
        sq.setStatText(rs.getString("stat_text"));
        sq.setWeight(rs.getInt("weight"));
        return sq;
    }
}
