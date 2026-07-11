package com.poe.cache.dao;

import com.poe.cache.model.ItemSellPrice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * item_sell_prices 表数据访问对象。
 * <p>
 * 表使用 (page_id, currency_name) 作为复合主键。
 * 记录物品卖给商人可获得的通货数量。
 */
public class ItemSellPriceDao implements CrudRepository<ItemSellPrice, Integer> {

    private final javax.sql.DataSource dataSource;

    public ItemSellPriceDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(ItemSellPrice entity) {
        String sql = "INSERT INTO item_sell_prices (page_id, page_name, amount, currency_name) " +
            "VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert item_sell_price: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<ItemSellPrice> entities) {
        String sql = "INSERT INTO item_sell_prices (page_id, page_name, amount, currency_name) " +
            "VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (ItemSellPrice entity : entities) {
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
            throw new RuntimeException("Failed to batch insert item_sell_prices", e);
        }
    }

    @Override
    public Optional<ItemSellPrice> findById(Integer pageId) {
        String sql = "SELECT * FROM item_sell_prices WHERE page_id = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find item_sell_price: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<ItemSellPrice> findAll() {
        List<ItemSellPrice> list = new ArrayList<>();
        String sql = "SELECT * FROM item_sell_prices ORDER BY page_id, currency_name";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all item_sell_prices", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM item_sell_prices WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete item_sell_prices: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM item_sell_prices";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count item_sell_prices", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, ItemSellPrice p) throws SQLException {
        ps.setInt(1, p.getPageId());
        ps.setString(2, p.getPageName());
        ps.setInt(3, p.getAmount());
        ps.setString(4, p.getCurrencyName());
    }

    private ItemSellPrice mapRow(ResultSet rs) throws SQLException {
        ItemSellPrice p = new ItemSellPrice();
        p.setPageId(rs.getInt("page_id"));
        p.setPageName(rs.getString("page_name"));
        p.setAmount(rs.getInt("amount"));
        p.setCurrencyName(rs.getString("currency_name"));
        return p;
    }
}
