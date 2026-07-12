package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.ItemPurchaseCost;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * item_purchase_costs 表数据访问对象。
 * <p>
 * 表使用 (page_id, currency_name, rarity) 作为复合主键。
 * 记录从商人处购买物品所需的通货成本，按稀有度区分。
 */
public class ItemPurchaseCostDao implements CrudRepository<ItemPurchaseCost, Integer> {

    private final DataSource dataSource;

    public ItemPurchaseCostDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(ItemPurchaseCost entity) {
        String sql = "INSERT INTO item_purchase_costs (page_id, page_name, amount, " +
            "currency_name, rarity) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert item_purchase_cost: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<ItemPurchaseCost> entities) {
        String sql = "INSERT INTO item_purchase_costs (page_id, page_name, amount, " +
            "currency_name, rarity) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (ItemPurchaseCost entity : entities) {
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
            throw new RuntimeException("Failed to batch insert item_purchase_costs", e);
        }
    }

    @Override
    public Optional<ItemPurchaseCost> findById(Integer pageId) {
        String sql = "SELECT * FROM item_purchase_costs WHERE page_id = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find item_purchase_cost: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<ItemPurchaseCost> findAll() {
        List<ItemPurchaseCost> list = new ArrayList<>();
        String sql = "SELECT * FROM item_purchase_costs ORDER BY page_id, currency_name, rarity";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all item_purchase_costs", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM item_purchase_costs WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete item_purchase_costs: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM item_purchase_costs";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count item_purchase_costs", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, ItemPurchaseCost c) throws SQLException {
        ps.setInt(1, c.getPageId());
        ps.setString(2, c.getPageName());
        ps.setInt(3, c.getAmount());
        ps.setString(4, c.getCurrencyName());
        ps.setString(5, c.getRarity());
    }

    private ItemPurchaseCost mapRow(ResultSet rs) throws SQLException {
        ItemPurchaseCost c = new ItemPurchaseCost();
        c.setPageId(rs.getInt("page_id"));
        c.setPageName(rs.getString("page_name"));
        c.setAmount(rs.getInt("amount"));
        c.setCurrencyName(rs.getString("currency_name"));
        c.setRarity(rs.getString("rarity"));
        return c;
    }
}
