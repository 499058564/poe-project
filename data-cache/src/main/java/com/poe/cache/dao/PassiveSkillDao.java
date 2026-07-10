package com.poe.cache.dao;

import com.poe.cache.model.PassiveSkill;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * passive_skills 表数据访问对象。
 */
public class PassiveSkillDao implements CrudRepository<PassiveSkill, Integer> {

    private final Connection connection;

    public PassiveSkillDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(PassiveSkill skill) {
        String sql = "INSERT INTO passive_skills (id, name, name_zh, class, ascendancy, stats, " +
            "is_keystone, is_notable, is_jewel_socket, x, y, connections, version) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (PassiveSkill skill : skills) {
                    setParams(ps, skill);
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
            throw new RuntimeException("Failed to batch insert passive skills", e);
        }
    }

    @Override
    public Optional<PassiveSkill> findById(Integer id) {
        String sql = "SELECT * FROM passive_skills WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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

    @Override
    public List<PassiveSkill> findAll() {
        String sql = "SELECT * FROM passive_skills ORDER BY id";
        List<PassiveSkill> skills = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                skills.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all passive skills", e);
        }
        return skills;
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM passive_skills WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete passive skill by id: " + id, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM passive_skills";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count passive skills", e);
        }
        return 0;
    }

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
