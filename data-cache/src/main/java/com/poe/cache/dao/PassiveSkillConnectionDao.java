package com.poe.cache.dao;

import com.poe.cache.model.PassiveSkillConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * passive_skill_connections 表数据访问对象。
 * <p>
 * 表使用 (page_id, tree_id) 作为复合主键，记录天赋树节点间的连接关系。
 */
public class PassiveSkillConnectionDao implements CrudRepository<PassiveSkillConnection, Integer> {

    private final Connection connection;

    public PassiveSkillConnectionDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * 插入单条天赋连接记录。
     *
     * @param entity 天赋连接实体
     */
    @Override
    public void insert(PassiveSkillConnection entity) {
        String sql = "INSERT INTO passive_skill_connections (page_id, page_name, node_ids, tree_id) "
            + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert passive_skill_connection: " + entity.getPageId(), e);
        }
    }

    /**
     * 批量插入天赋连接记录（事务）。
     *
     * @param entities 天赋连接实体列表（非空）
     */
    @Override
    public void batchInsert(List<PassiveSkillConnection> entities) {
        String sql = "INSERT INTO passive_skill_connections (page_id, page_name, node_ids, tree_id) "
            + "VALUES (?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (PassiveSkillConnection entity : entities) {
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
            throw new RuntimeException("Failed to batch insert passive_skill_connections", e);
        }
    }

    /**
     * 按 page_id 查询第一条天赋连接。
     *
     * @param pageId Wiki 页面 ID
     * @return 天赋连接实体（可能为空）
     */
    @Override
    public Optional<PassiveSkillConnection> findById(Integer pageId) {
        String sql = "SELECT * FROM passive_skill_connections WHERE page_id = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find passive_skill_connection by id: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部天赋连接记录。
     *
     * @return 按 page_id, tree_id 升序排列的列表
     */
    @Override
    public List<PassiveSkillConnection> findAll() {
        List<PassiveSkillConnection> list = new ArrayList<>();
        String sql = "SELECT * FROM passive_skill_connections ORDER BY page_id, tree_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all passive_skill_connections", e);
        }
        return list;
    }

    /**
     * 按 page_id 删除该天赋的所有连接。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM passive_skill_connections WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete passive_skill_connections: " + pageId, e);
        }
    }

    /**
     * 统计天赋连接记录总数。
     *
     * @return passive_skill_connections 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM passive_skill_connections";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count passive_skill_connections", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, PassiveSkillConnection psc) throws SQLException {
        ps.setInt(1, psc.getPageId());
        ps.setString(2, psc.getPageName());
        ps.setString(3, psc.getNodeIds());
        ps.setString(4, psc.getTreeId());
    }

    private PassiveSkillConnection mapRow(ResultSet rs) throws SQLException {
        PassiveSkillConnection psc = new PassiveSkillConnection();
        psc.setPageId(rs.getInt("page_id"));
        psc.setPageName(rs.getString("page_name"));
        psc.setNodeIds(rs.getString("node_ids"));
        psc.setTreeId(rs.getString("tree_id"));
        return psc;
    }
}
