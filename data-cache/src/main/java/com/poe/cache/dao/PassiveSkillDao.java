package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.PassiveSkill;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * passive_skills 表数据访问对象。
 * <p>
 * 表使用 id 作为主键。
 * 记录天赋树的全部被动技能，包括升华、基石、核心天赋和珠宝插槽的坐标与连接关系。
 */
public class PassiveSkillDao implements CrudRepository<PassiveSkill, Integer> {

    private final DataSource dataSource;

    public PassiveSkillDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 插入单条被动技能记录。
     *
     * @param skill 被动技能实体
     */
    @Override
    public void insert(PassiveSkill skill) {
        String sql = "INSERT INTO passive_skills (id, name, name_zh, class, ascendancy, stats, " +
            "is_keystone, is_notable, is_jewel_socket, x, y, connections, version) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, skill);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert passive skill: " + skill.getId(), e);
        }
    }

    /**
     * 批量插入，在单个事务内执行以提高性能。
     * 任一行插入失败时，整个批次回滚并恢复自动提交模式。
     */
    @Override
    public void batchInsert(List<PassiveSkill> skills) {
        String sql = "INSERT INTO passive_skills (id, name, name_zh, class, ascendancy, stats, " +
            "is_keystone, is_notable, is_jewel_socket, x, y, connections, version) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (PassiveSkill skill : skills) {
                    setParams(ps, skill);
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
            throw new RuntimeException("Failed to batch insert passive skills", e);
        }
    }

    /**
     * 根据主键查询被动技能。
     *
     * @param id 被动技能 ID
     * @return 被动技能实体（可能为空）
     */
    @Override
    public Optional<PassiveSkill> findById(Integer id) {
        String sql = "SELECT * FROM passive_skills WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find passive skill by id: " + id, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部被动技能记录。
     *
     * @return 按 id 升序排列的被动技能列表
     */
    @Override
    public List<PassiveSkill> findAll() {
        String sql = "SELECT * FROM passive_skills ORDER BY id";
        List<PassiveSkill> skills = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                skills.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all passive skills", e);
        }
        return skills;
    }

    /**
     * 根据主键删除被动技能。
     *
     * @param id 被动技能 ID
     */
    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM passive_skills WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete passive skill by id: " + id, e);
        }
    }

    /**
     * 统计被动技能记录总数。
     *
     * @return passive_skills 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM passive_skills";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count passive skills", e);
        }
        return 0;
    }

    /** 设置 PreparedStatement 参数（绑定 PassiveSkill 字段） */
    private void setParams(PreparedStatement ps, PassiveSkill skill) throws SQLException {
        ps.setInt(1, skill.getId());
        ps.setString(2, skill.getName());
        ps.setString(3, skill.getNameZh());
        ps.setString(4, skill.getPassiveClass());
        ps.setString(5, skill.getAscendancy());
        ps.setString(6, skill.getStats());
        ps.setInt(7, skill.isKeystone() ? 1 : 0);
        ps.setInt(8, skill.isNotable() ? 1 : 0);
        ps.setInt(9, skill.isJewelSocket() ? 1 : 0);
        ps.setDouble(10, skill.getX());
        ps.setDouble(11, skill.getY());
        ps.setString(12, skill.getConnections());
        ps.setString(13, skill.getVersion());
    }

    /** 从 ResultSet 映射一行到 PassiveSkill 实体 */
    private PassiveSkill mapRow(ResultSet rs) throws SQLException {
        PassiveSkill skill = new PassiveSkill();
        skill.setId(rs.getInt("id"));
        skill.setName(rs.getString("name"));
        skill.setNameZh(rs.getString("name_zh"));
        skill.setPassiveClass(rs.getString("class"));
        skill.setAscendancy(rs.getString("ascendancy"));
        skill.setStats(rs.getString("stats"));
        skill.setKeystone(rs.getInt("is_keystone") != 0);
        skill.setNotable(rs.getInt("is_notable") != 0);
        skill.setJewelSocket(rs.getInt("is_jewel_socket") != 0);
        skill.setX(rs.getDouble("x"));
        skill.setY(rs.getDouble("y"));
        skill.setConnections(rs.getString("connections"));
        skill.setVersion(rs.getString("version"));
        return skill;
    }
}
