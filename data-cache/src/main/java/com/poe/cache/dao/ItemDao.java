package com.poe.cache.dao;

import com.poe.cache.model.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * base_items 表数据访问对象。
 */
public class ItemDao implements CrudRepository<Item, Integer> {

    private final Connection connection;

    public ItemDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(Item item) {
        String sql = "INSERT INTO base_items (id, name, name_zh, class, inventory_width, " +
            "inventory_height, requirements, implicits, properties, flavour_text, " +
            "drop_level, wiki_url, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setItemParams(ps, item);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert item: " + item.getId(), e);
        }
    }

    /**
     * 批量插入，在单个事务内执行以提高性能。
     * 任一行插入失败时，整个批次回滚并恢复自动提交模式。
     */
    @Override
    public void batchInsert(List<Item> items) {
        String sql = "INSERT INTO base_items (id, name, name_zh, class, inventory_width, " +
            "inventory_height, requirements, implicits, properties, flavour_text, " +
            "drop_level, wiki_url, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Item item : items) {
                    setItemParams(ps, item);
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
            throw new RuntimeException("Failed to batch insert items", e);
        }
    }

    @Override
    public Optional<Item> findById(Integer id) {
        String sql = "SELECT * FROM base_items WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find item by id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Item> findAll() {
        String sql = "SELECT * FROM base_items ORDER BY id";
        List<Item> items = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all items", e);
        }
        return items;
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM base_items WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete item by id: " + id, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM base_items";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count items", e);
        }
        return 0;
    }

    private void setItemParams(PreparedStatement ps, Item item) throws SQLException {
        ps.setInt(1, item.getId());
        ps.setString(2, item.getName());
        ps.setString(3, item.getNameZh());
        ps.setString(4, item.getItemClass());
        ps.setInt(5, item.getInventoryWidth());
        ps.setInt(6, item.getInventoryHeight());
        ps.setString(7, item.getRequirements());
        ps.setString(8, item.getImplicits());
        ps.setString(9, item.getProperties());
        ps.setString(10, item.getFlavourText());
        ps.setInt(11, item.getDropLevel());
        ps.setString(12, item.getWikiUrl());
        ps.setString(13, item.getVersion());
    }

    private Item mapRow(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setId(rs.getInt("id"));
        item.setName(rs.getString("name"));
        item.setNameZh(rs.getString("name_zh"));
        item.setItemClass(rs.getString("class"));
        item.setInventoryWidth(rs.getInt("inventory_width"));
        item.setInventoryHeight(rs.getInt("inventory_height"));
        item.setRequirements(rs.getString("requirements"));
        item.setImplicits(rs.getString("implicits"));
        item.setProperties(rs.getString("properties"));
        item.setFlavourText(rs.getString("flavour_text"));
        item.setDropLevel(rs.getInt("drop_level"));
        item.setWikiUrl(rs.getString("wiki_url"));
        item.setVersion(rs.getString("version"));
        return item;
    }
}
