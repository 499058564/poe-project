package com.poe.cache.dao;

import com.poe.cache.model.ItemBuff;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * item_buffs 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键（每个物品至多一条 Buff）。
 * 记录药剂等物品使用后提供的临时 Buff 效果。
 */
public class ItemBuffDao implements CrudRepository<ItemBuff, Integer> {

    private final javax.sql.DataSource dataSource;

    public ItemBuffDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(ItemBuff entity) {
        String sql = "INSERT INTO item_buffs (page_id, page_name, buff_values, icon, buff_id, stat_text) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert item_buff: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<ItemBuff> entities) {
        String sql = "INSERT INTO item_buffs (page_id, page_name, buff_values, icon, buff_id, stat_text) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (ItemBuff entity : entities) {
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
            throw new RuntimeException("Failed to batch insert item_buffs", e);
        }
    }

    @Override
    public Optional<ItemBuff> findById(Integer pageId) {
        String sql = "SELECT * FROM item_buffs WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find item_buff: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<ItemBuff> findAll() {
        List<ItemBuff> list = new ArrayList<>();
        String sql = "SELECT * FROM item_buffs ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all item_buffs", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM item_buffs WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete item_buff: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM item_buffs";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count item_buffs", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, ItemBuff b) throws SQLException {
        ps.setInt(1, b.getPageId());
        ps.setString(2, b.getPageName());
        ps.setString(3, b.getBuffValues());
        ps.setString(4, b.getIcon());
        ps.setString(5, b.getBuffId());
        ps.setString(6, b.getStatText());
    }

    private ItemBuff mapRow(ResultSet rs) throws SQLException {
        ItemBuff b = new ItemBuff();
        b.setPageId(rs.getInt("page_id"));
        b.setPageName(rs.getString("page_name"));
        b.setBuffValues(rs.getString("buff_values"));
        b.setIcon(rs.getString("icon"));
        b.setBuffId(rs.getString("buff_id"));
        b.setStatText(rs.getString("stat_text"));
        return b;
    }
}
