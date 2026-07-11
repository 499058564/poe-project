package com.poe.cache.dao;

import com.poe.cache.model.ItemMod;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * item_mods 表数据访问对象。
 * <p>
 * 表使用 (page_id, mod_id) 作为复合主键。
 * 记录物品与词缀的关联关系，区分显式/隐式/地图碎片奖励等类型。
 */
public class ItemModDao implements CrudRepository<ItemMod, Integer> {

    private final javax.sql.DataSource dataSource;

    public ItemModDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(ItemMod entity) {
        String sql = "INSERT INTO item_mods (page_id, page_name, mod_id, is_explicit, " +
            "is_implicit, is_map_fragment_bonus, is_random, text) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert item_mod: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<ItemMod> entities) {
        String sql = "INSERT INTO item_mods (page_id, page_name, mod_id, is_explicit, " +
            "is_implicit, is_map_fragment_bonus, is_random, text) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (ItemMod entity : entities) {
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
            throw new RuntimeException("Failed to batch insert item_mods", e);
        }
    }

    @Override
    public Optional<ItemMod> findById(Integer pageId) {
        String sql = "SELECT * FROM item_mods WHERE page_id = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find item_mod: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<ItemMod> findAll() {
        List<ItemMod> list = new ArrayList<>();
        String sql = "SELECT * FROM item_mods ORDER BY page_id, mod_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all item_mods", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM item_mods WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete item_mods: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM item_mods";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count item_mods", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, ItemMod m) throws SQLException {
        ps.setInt(1, m.getPageId());
        ps.setString(2, m.getPageName());
        ps.setString(3, m.getModId());
        ps.setInt(4, m.isExplicit() ? 1 : 0);
        ps.setInt(5, m.isImplicit() ? 1 : 0);
        ps.setInt(6, m.isMapFragmentBonus() ? 1 : 0);
        ps.setInt(7, m.isRandom() ? 1 : 0);
        ps.setString(8, m.getText());
    }

    private ItemMod mapRow(ResultSet rs) throws SQLException {
        ItemMod m = new ItemMod();
        m.setPageId(rs.getInt("page_id"));
        m.setPageName(rs.getString("page_name"));
        m.setModId(rs.getString("mod_id"));
        m.setExplicit(rs.getInt("is_explicit") == 1);
        m.setImplicit(rs.getInt("is_implicit") == 1);
        m.setMapFragmentBonus(rs.getInt("is_map_fragment_bonus") == 1);
        m.setRandom(rs.getInt("is_random") == 1);
        m.setText(rs.getString("text"));
        return m;
    }
}
