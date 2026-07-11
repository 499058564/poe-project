package com.poe.cache.dao;

import com.poe.cache.model.Armour;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * armours 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键，通过 page_name 与 base_items 表关联。
 * 记录护甲、闪避、能量护盾、结界及移动速度属性。
 */
public class ArmourDao implements CrudRepository<Armour, Integer> {

    private final javax.sql.DataSource dataSource;

    public ArmourDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 插入单条护甲记录。
     *
     * @param a 护甲实体
     */
    @Override
    public void insert(Armour a) {
        String sql = "INSERT INTO armours (page_id, page_name, armour_min, armour_max, "
            + "evasion_min, evasion_max, energy_shield_min, energy_shield_max, "
            + "ward_min, ward_max, movement_speed) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, a);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert armour: " + a.getPageId(), e);
        }
    }

    /**
     * 批量插入护甲记录（事务）。
     * <p>
     * 使用 JDBC batch + 事务保证原子性，失败自动回滚。
     *
     * @param armours 护甲实体列表（非空）
     */
    @Override
    public void batchInsert(List<Armour> armours) {
        String sql = "INSERT INTO armours (page_id, page_name, armour_min, armour_max, "
            + "evasion_min, evasion_max, energy_shield_min, energy_shield_max, "
            + "ward_min, ward_max, movement_speed) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Armour a : armours) {
                    setParams(ps, a);
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
            throw new RuntimeException("Failed to batch insert armours", e);
        }
    }

    /**
     * 根据主键查询护甲。
     *
     * @param pageId Wiki 页面 ID
     * @return 护甲实体（可能为空）
     */
    @Override
    public Optional<Armour> findById(Integer pageId) {
        String sql = "SELECT * FROM armours WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find armour: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部护甲记录。
     *
     * @return 按 page_id 升序排列的护甲列表
     */
    @Override
    public List<Armour> findAll() {
        List<Armour> list = new ArrayList<>();
        String sql = "SELECT * FROM armours ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all armours", e);
        }
        return list;
    }

    /**
     * 根据主键删除护甲。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM armours WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete armour: " + pageId, e);
        }
    }

    /**
     * 统计护甲记录总数。
     *
     * @return armours 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM armours";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count armours", e);
        }
        return 0;
    }

    /** 设置 PreparedStatement 参数（绑定 Armour 字段） */
    private void setParams(PreparedStatement ps, Armour a) throws SQLException {
        ps.setInt(1, a.getPageId());
        ps.setString(2, a.getPageName());
        ps.setInt(3, a.getArmourMin());
        ps.setInt(4, a.getArmourMax());
        ps.setInt(5, a.getEvasionMin());
        ps.setInt(6, a.getEvasionMax());
        ps.setInt(7, a.getEnergyShieldMin());
        ps.setInt(8, a.getEnergyShieldMax());
        ps.setInt(9, a.getWardMin());
        ps.setInt(10, a.getWardMax());
        ps.setInt(11, a.getMovementSpeed());
    }

    /** 从 ResultSet 映射一行到 Armour 实体 */
    private Armour mapRow(ResultSet rs) throws SQLException {
        Armour a = new Armour();
        a.setPageId(rs.getInt("page_id"));
        a.setPageName(rs.getString("page_name"));
        a.setArmourMin(rs.getInt("armour_min"));
        a.setArmourMax(rs.getInt("armour_max"));
        a.setEvasionMin(rs.getInt("evasion_min"));
        a.setEvasionMax(rs.getInt("evasion_max"));
        a.setEnergyShieldMin(rs.getInt("energy_shield_min"));
        a.setEnergyShieldMax(rs.getInt("energy_shield_max"));
        a.setWardMin(rs.getInt("ward_min"));
        a.setWardMax(rs.getInt("ward_max"));
        a.setMovementSpeed(rs.getInt("movement_speed"));
        return a;
    }
}
