package com.poe.cache.dao;

import com.poe.cache.model.CharacterClass;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * character_classes 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键，记录 PoE 七大基础职业的属性倾向。
 */
public class CharacterClassDao implements CrudRepository<CharacterClass, Integer> {

    private final javax.sql.DataSource dataSource;

    public CharacterClassDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 插入单条职业记录。
     *
     * @param entity 职业实体
     */
    @Override
    public void insert(CharacterClass entity) {
        String sql = "INSERT INTO character_classes (page_id, page_name, dexterity, flavour_text, "
            + "class_id, intelligence, name, str_id, strength) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert character_class: " + entity.getPageId(), e);
        }
    }

    /**
     * 批量插入职业记录（事务）。
     *
     * @param entities 职业实体列表（非空）
     */
    @Override
    public void batchInsert(List<CharacterClass> entities) {
        String sql = "INSERT INTO character_classes (page_id, page_name, dexterity, flavour_text, "
            + "class_id, intelligence, name, str_id, strength) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (CharacterClass entity : entities) {
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
            throw new RuntimeException("Failed to batch insert character_classes", e);
        }
    }

    /**
     * 根据主键查询职业。
     *
     * @param pageId Wiki 页面 ID
     * @return 职业实体（可能为空）
     */
    @Override
    public Optional<CharacterClass> findById(Integer pageId) {
        String sql = "SELECT * FROM character_classes WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find character_class by id: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部职业记录。
     *
     * @return 按 page_id 升序排列的职业列表
     */
    @Override
    public List<CharacterClass> findAll() {
        List<CharacterClass> list = new ArrayList<>();
        String sql = "SELECT * FROM character_classes ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all character_classes", e);
        }
        return list;
    }

    /**
     * 根据主键删除职业。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM character_classes WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete character_class: " + pageId, e);
        }
    }

    /**
     * 统计职业记录总数。
     *
     * @return character_classes 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM character_classes";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count character_classes", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, CharacterClass cc) throws SQLException {
        ps.setInt(1, cc.getPageId());
        ps.setString(2, cc.getPageName());
        ps.setString(3, cc.getDexterity());
        ps.setString(4, cc.getFlavourText());
        ps.setInt(5, cc.getClassId());
        ps.setString(6, cc.getIntelligence());
        ps.setString(7, cc.getName());
        ps.setString(8, cc.getStrId());
        ps.setString(9, cc.getStrength());
    }

    private CharacterClass mapRow(ResultSet rs) throws SQLException {
        CharacterClass cc = new CharacterClass();
        cc.setPageId(rs.getInt("page_id"));
        cc.setPageName(rs.getString("page_name"));
        cc.setDexterity(rs.getString("dexterity"));
        cc.setFlavourText(rs.getString("flavour_text"));
        cc.setClassId(rs.getInt("class_id"));
        cc.setIntelligence(rs.getString("intelligence"));
        cc.setName(rs.getString("name"));
        cc.setStrId(rs.getString("str_id"));
        cc.setStrength(rs.getString("strength"));
        return cc;
    }
}
