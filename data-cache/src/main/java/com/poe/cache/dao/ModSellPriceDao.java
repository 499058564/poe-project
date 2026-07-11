package com.poe.cache.dao;

import com.poe.cache.model.ModSellPrice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * mod_sell_prices 表数据访问对象。
 * <p>
 * 表使用 (page_id, name) 作为复合主键。
 * 记录词缀的出售价格（通货类型与数量）。
 */
public class ModSellPriceDao implements CrudRepository<ModSellPrice, Integer> {

    private final Connection connection;

    public ModSellPriceDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(ModSellPrice entity) {
        String sql = "INSERT INTO mod_sell_prices (page_id, page_name, amount, currency_name) " +
            "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert mod_sell_price: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<ModSellPrice> entities) {
        String sql = "INSERT INTO mod_sell_prices (page_id, page_name, amount, currency_name) " +
            "VALUES (?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (ModSellPrice entity : entities) {
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
            throw new RuntimeException("Failed to batch insert mod_sell_prices", e);
        }
    }

    @Override
    public Optional<ModSellPrice> findById(Integer pageId) {
        String sql = "SELECT * FROM mod_sell_prices WHERE page_id = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find mod_sell_price: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<ModSellPrice> findAll() {
        List<ModSellPrice> list = new ArrayList<>();
        String sql = "SELECT * FROM mod_sell_prices ORDER BY page_id, currency_name";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all mod_sell_prices", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM mod_sell_prices WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete mod_sell_prices: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM mod_sell_prices";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count mod_sell_prices", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, ModSellPrice p) throws SQLException {
        ps.setInt(1, p.getPageId());
        ps.setString(2, p.getPageName());
        ps.setInt(3, p.getAmount());
        ps.setString(4, p.getCurrencyName());
    }

    private ModSellPrice mapRow(ResultSet rs) throws SQLException {
        ModSellPrice p = new ModSellPrice();
        p.setPageId(rs.getInt("page_id"));
        p.setPageName(rs.getString("page_name"));
        p.setAmount(rs.getInt("amount"));
        p.setCurrencyName(rs.getString("currency_name"));
        return p;
    }
}
