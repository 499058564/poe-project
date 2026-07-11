package com.poe.cache.dao;

import com.poe.cache.model.MasteryGroup;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * mastery_groups 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键，记录天赋专精的分组信息。
 */
public class MasteryGroupDao implements CrudRepository<MasteryGroup, Integer> {

    private final Connection connection;

    public MasteryGroupDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * 插入单条专精组记录。
     *
     * @param entity 专精组实体
     */
    @Override
    public void insert(MasteryGroup entity) {
        String sql = "INSERT INTO mastery_groups (page_id, page_name, icon, group_id, name) "
            + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert mastery_group: " + entity.getPageId(), e);
        }
    }

    /**
     * 批量插入专精组记录（事务）。
     *
     * @param entities 专精组实体列表（非空）
     */
    @Override
    public void batchInsert(List<MasteryGroup> entities) {
        String sql = "INSERT INTO mastery_groups (page_id, page_name, icon, group_id, name) "
            + "VALUES (?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (MasteryGroup entity : entities) {
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
            throw new RuntimeException("Failed to batch insert mastery_groups", e);
        }
    }

    /**
     * 根据主键查询专精组。
     *
     * @param pageId Wiki 页面 ID
     * @return 专精组实体（可能为空）
     */
    @Override
    public Optional<MasteryGroup> findById(Integer pageId) {
        String sql = "SELECT * FROM mastery_groups WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find mastery_group by id: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部专精组记录。
     *
     * @return 按 page_id 升序排列的专精组列表
     */
    @Override
    public List<MasteryGroup> findAll() {
        List<MasteryGroup> list = new ArrayList<>();
        String sql = "SELECT * FROM mastery_groups ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all mastery_groups", e);
        }
        return list;
    }

    /**
     * 根据主键删除专精组。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM mastery_groups WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete mastery_group: " + pageId, e);
        }
    }

    /**
     * 统计专精组记录总数。
     *
     * @return mastery_groups 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM mastery_groups";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count mastery_groups", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, MasteryGroup mg) throws SQLException {
        ps.setInt(1, mg.getPageId());
        ps.setString(2, mg.getPageName());
        ps.setString(3, mg.getIcon());
        ps.setString(4, mg.getGroupId());
        ps.setString(5, mg.getName());
    }

    private MasteryGroup mapRow(ResultSet rs) throws SQLException {
        MasteryGroup mg = new MasteryGroup();
        mg.setPageId(rs.getInt("page_id"));
        mg.setPageName(rs.getString("page_name"));
        mg.setIcon(rs.getString("icon"));
        mg.setGroupId(rs.getString("group_id"));
        mg.setName(rs.getString("name"));
        return mg;
    }
}
