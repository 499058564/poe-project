package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.Skill;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * skills 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键，记录主动技能和辅助技能的基本信息。
 */
public class SkillDao implements CrudRepository<Skill, Integer> {

    private final DataSource dataSource;

    public SkillDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 插入单条技能记录。
     *
     * @param entity 技能实体
     */
    @Override
    public void insert(Skill entity) {
        String sql = "INSERT INTO skills (page_id, page_name, active_skill_name, cast_time, "
            + "description, is_support, item_class_id_restriction, item_class_restriction, "
            + "max_level, skill_id, stat_text) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert skill: " + entity.getPageId(), e);
        }
    }

    /**
     * 批量插入技能记录（事务）。
     *
     * @param entities 技能实体列表（非空）
     */
    @Override
    public void batchInsert(List<Skill> entities) {
        String sql = "INSERT INTO skills (page_id, page_name, active_skill_name, cast_time, "
            + "description, is_support, item_class_id_restriction, item_class_restriction, "
            + "max_level, skill_id, stat_text) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Skill entity : entities) {
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
            throw new RuntimeException("Failed to batch insert skills", e);
        }
    }

    /**
     * 根据主键查询技能。
     *
     * @param pageId Wiki 页面 ID
     * @return 技能实体（可能为空）
     */
    @Override
    public Optional<Skill> findById(Integer pageId) {
        String sql = "SELECT * FROM skills WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find skill by id: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部技能记录。
     *
     * @return 按 page_id 升序排列的技能列表
     */
    @Override
    public List<Skill> findAll() {
        List<Skill> list = new ArrayList<>();
        String sql = "SELECT * FROM skills ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all skills", e);
        }
        return list;
    }

    /**
     * 根据主键删除技能。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM skills WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete skill: " + pageId, e);
        }
    }

    /**
     * 统计技能记录总数。
     *
     * @return skills 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM skills";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count skills", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Skill s) throws SQLException {
        ps.setInt(1, s.getPageId());
        ps.setString(2, s.getPageName());
        ps.setString(3, s.getActiveSkillName());
        ps.setDouble(4, s.getCastTime());
        ps.setString(5, s.getDescription());
        ps.setInt(6, s.isSupport() ? 1 : 0);
        ps.setString(7, s.getItemClassIdRestriction());
        ps.setString(8, s.getItemClassRestriction());
        ps.setInt(9, s.getMaxLevel());
        ps.setString(10, s.getSkillId());
        ps.setString(11, s.getStatText());
    }

    private Skill mapRow(ResultSet rs) throws SQLException {
        Skill s = new Skill();
        s.setPageId(rs.getInt("page_id"));
        s.setPageName(rs.getString("page_name"));
        s.setActiveSkillName(rs.getString("active_skill_name"));
        s.setCastTime(rs.getDouble("cast_time"));
        s.setDescription(rs.getString("description"));
        s.setSupport(rs.getInt("is_support") == 1);
        s.setItemClassIdRestriction(rs.getString("item_class_id_restriction"));
        s.setItemClassRestriction(rs.getString("item_class_restriction"));
        s.setMaxLevel(rs.getInt("max_level"));
        s.setSkillId(rs.getString("skill_id"));
        s.setStatText(rs.getString("stat_text"));
        return s;
    }
}
