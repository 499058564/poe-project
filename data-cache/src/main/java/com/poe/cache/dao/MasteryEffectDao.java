package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.MasteryEffect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * mastery_effects 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键，记录天赋专精的各项可选效果。
 */
public class MasteryEffectDao implements CrudRepository<MasteryEffect, Integer> {

    private final DataSource dataSource;

    public MasteryEffectDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 插入单条专精效果记录。
     *
     * @param entity 专精效果实体
     */
    @Override
    public void insert(MasteryEffect entity) {
        String sql = "INSERT INTO mastery_effects (page_id, page_name, effect_id, stat_ids, "
            + "stat_text, stat_text_raw, stat_values) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert mastery_effect: " + entity.getPageId(), e);
        }
    }

    /**
     * 批量插入专精效果记录（事务）。
     *
     * @param entities 专精效果实体列表（非空）
     */
    @Override
    public void batchInsert(List<MasteryEffect> entities) {
        String sql = "INSERT INTO mastery_effects (page_id, page_name, effect_id, stat_ids, "
            + "stat_text, stat_text_raw, stat_values) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (MasteryEffect entity : entities) {
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
            throw new RuntimeException("Failed to batch insert mastery_effects", e);
        }
    }

    /**
     * 根据主键查询专精效果。
     *
     * @param pageId Wiki 页面 ID
     * @return 专精效果实体（可能为空）
     */
    @Override
    public Optional<MasteryEffect> findById(Integer pageId) {
        String sql = "SELECT * FROM mastery_effects WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find mastery_effect by id: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部专精效果记录。
     *
     * @return 按 page_id 升序排列的专精效果列表
     */
    @Override
    public List<MasteryEffect> findAll() {
        List<MasteryEffect> list = new ArrayList<>();
        String sql = "SELECT * FROM mastery_effects ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all mastery_effects", e);
        }
        return list;
    }

    /**
     * 根据主键删除专精效果。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM mastery_effects WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete mastery_effect: " + pageId, e);
        }
    }

    /**
     * 统计专精效果记录总数。
     *
     * @return mastery_effects 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM mastery_effects";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count mastery_effects", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, MasteryEffect me) throws SQLException {
        ps.setInt(1, me.getPageId());
        ps.setString(2, me.getPageName());
        ps.setString(3, me.getEffectId());
        ps.setString(4, me.getStatIds());
        ps.setString(5, me.getStatText());
        ps.setString(6, me.getStatTextRaw());
        ps.setString(7, me.getStatValues());
    }

    private MasteryEffect mapRow(ResultSet rs) throws SQLException {
        MasteryEffect me = new MasteryEffect();
        me.setPageId(rs.getInt("page_id"));
        me.setPageName(rs.getString("page_name"));
        me.setEffectId(rs.getString("effect_id"));
        me.setStatIds(rs.getString("stat_ids"));
        me.setStatText(rs.getString("stat_text"));
        me.setStatTextRaw(rs.getString("stat_text_raw"));
        me.setStatValues(rs.getString("stat_values"));
        return me;
    }
}
