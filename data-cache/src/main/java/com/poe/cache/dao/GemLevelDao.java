package com.poe.cache.dao;

import com.poe.cache.model.GemLevel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * gem_levels 表数据访问对象。
 * <p>
 * 表使用 (page_id, level) 作为复合主键，记录宝石各等级的经验与属性需求。
 */
public class GemLevelDao implements CrudRepository<GemLevel, Integer> {

    private final Connection connection;

    public GemLevelDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * 插入单条宝石等级记录。
     *
     * @param entity 宝石等级实体
     */
    @Override
    public void insert(GemLevel entity) {
        String sql = "INSERT INTO gem_levels (page_id, page_name, experience, level, "
            + "required_dexterity, required_intelligence, required_level, required_strength) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert gem_level: " + entity.getPageId(), e);
        }
    }

    /**
     * 批量插入宝石等级记录（事务）。
     *
     * @param entities 宝石等级实体列表（非空）
     */
    @Override
    public void batchInsert(List<GemLevel> entities) {
        String sql = "INSERT INTO gem_levels (page_id, page_name, experience, level, "
            + "required_dexterity, required_intelligence, required_level, required_strength) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (GemLevel entity : entities) {
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
            throw new RuntimeException("Failed to batch insert gem_levels", e);
        }
    }

    /**
     * 按 page_id 查询第一条宝石等级。
     *
     * @param pageId Wiki 页面 ID
     * @return 宝石等级实体（可能为空）
     */
    @Override
    public Optional<GemLevel> findById(Integer pageId) {
        String sql = "SELECT * FROM gem_levels WHERE page_id = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find gem_level by id: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部宝石等级记录。
     *
     * @return 按 page_id, level 升序排列的宝石等级列表
     */
    @Override
    public List<GemLevel> findAll() {
        List<GemLevel> list = new ArrayList<>();
        String sql = "SELECT * FROM gem_levels ORDER BY page_id, level";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all gem_levels", e);
        }
        return list;
    }

    /**
     * 按 page_id 删除该宝石的所有等级记录。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM gem_levels WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete gem_levels: " + pageId, e);
        }
    }

    /**
     * 统计宝石等级记录总数。
     *
     * @return gem_levels 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM gem_levels";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count gem_levels", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, GemLevel gl) throws SQLException {
        ps.setInt(1, gl.getPageId());
        ps.setString(2, gl.getPageName());
        ps.setInt(3, gl.getExperience());
        ps.setInt(4, gl.getLevel());
        ps.setInt(5, gl.getRequiredDexterity());
        ps.setInt(6, gl.getRequiredIntelligence());
        ps.setInt(7, gl.getRequiredLevel());
        ps.setInt(8, gl.getRequiredStrength());
    }

    private GemLevel mapRow(ResultSet rs) throws SQLException {
        GemLevel gl = new GemLevel();
        gl.setPageId(rs.getInt("page_id"));
        gl.setPageName(rs.getString("page_name"));
        gl.setExperience(rs.getInt("experience"));
        gl.setLevel(rs.getInt("level"));
        gl.setRequiredDexterity(rs.getInt("required_dexterity"));
        gl.setRequiredIntelligence(rs.getInt("required_intelligence"));
        gl.setRequiredLevel(rs.getInt("required_level"));
        gl.setRequiredStrength(rs.getInt("required_strength"));
        return gl;
    }
}
