package com.poe.cache.dao;

import com.poe.cache.model.AscendancyClass;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ascendancy_classes 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键，记录七大基础职业各自的升华进阶分支。
 */
public class AscendancyClassDao implements CrudRepository<AscendancyClass, Integer> {

    private final Connection connection;

    public AscendancyClassDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * 插入单条升华职业记录。
     *
     * @param entity 升华职业实体
     */
    @Override
    public void insert(AscendancyClass entity) {
        String sql = "INSERT INTO ascendancy_classes (page_id, page_name, character_class, "
            + "character_id, flavour_text, ascendancy_id, name) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert ascendancy_class: " + entity.getPageId(), e);
        }
    }

    /**
     * 批量插入升华职业记录（事务）。
     *
     * @param entities 升华职业实体列表（非空）
     */
    @Override
    public void batchInsert(List<AscendancyClass> entities) {
        String sql = "INSERT INTO ascendancy_classes (page_id, page_name, character_class, "
            + "character_id, flavour_text, ascendancy_id, name) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (AscendancyClass entity : entities) {
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
            throw new RuntimeException("Failed to batch insert ascendancy_classes", e);
        }
    }

    /**
     * 根据主键查询升华职业。
     *
     * @param pageId Wiki 页面 ID
     * @return 升华职业实体（可能为空）
     */
    @Override
    public Optional<AscendancyClass> findById(Integer pageId) {
        String sql = "SELECT * FROM ascendancy_classes WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find ascendancy_class by id: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部升华职业记录。
     *
     * @return 按 page_id 升序排列的升华职业列表
     */
    @Override
    public List<AscendancyClass> findAll() {
        List<AscendancyClass> list = new ArrayList<>();
        String sql = "SELECT * FROM ascendancy_classes ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all ascendancy_classes", e);
        }
        return list;
    }

    /**
     * 根据主键删除升华职业。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM ascendancy_classes WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete ascendancy_class: " + pageId, e);
        }
    }

    /**
     * 统计升华职业记录总数。
     *
     * @return ascendancy_classes 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM ascendancy_classes";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count ascendancy_classes", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, AscendancyClass ac) throws SQLException {
        ps.setInt(1, ac.getPageId());
        ps.setString(2, ac.getPageName());
        ps.setString(3, ac.getCharacterClass());
        ps.setInt(4, ac.getCharacterId());
        ps.setString(5, ac.getFlavourText());
        ps.setInt(6, ac.getAscendancyId());
        ps.setString(7, ac.getName());
    }

    private AscendancyClass mapRow(ResultSet rs) throws SQLException {
        AscendancyClass ac = new AscendancyClass();
        ac.setPageId(rs.getInt("page_id"));
        ac.setPageName(rs.getString("page_name"));
        ac.setCharacterClass(rs.getString("character_class"));
        ac.setCharacterId(rs.getInt("character_id"));
        ac.setFlavourText(rs.getString("flavour_text"));
        ac.setAscendancyId(rs.getInt("ascendancy_id"));
        ac.setName(rs.getString("name"));
        return ac;
    }
}
