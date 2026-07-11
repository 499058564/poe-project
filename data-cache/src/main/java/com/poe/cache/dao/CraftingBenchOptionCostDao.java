package com.poe.cache.dao;

import com.poe.cache.model.CraftingBenchOptionCost;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * crafting_bench_options_costs 表数据访问对象。
 * <p>
 * 表使用 (option_id, currency_name) 作为复合主键。
 * 记录工艺台词缀制作所需的通货消耗明细。
 */
public class CraftingBenchOptionCostDao implements CrudRepository<CraftingBenchOptionCost, Integer> {

    private final javax.sql.DataSource dataSource;

    public CraftingBenchOptionCostDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(CraftingBenchOptionCost entity) {
        String sql = "INSERT INTO crafting_bench_options_costs (page_id, page_name, option_id, " +
            "amount, currency_name) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert crafting_bench_option_cost: " + entity.getOptionId(), e);
        }
    }

    @Override
    public void batchInsert(List<CraftingBenchOptionCost> entities) {
        String sql = "INSERT INTO crafting_bench_options_costs (page_id, page_name, option_id, " +
            "amount, currency_name) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (CraftingBenchOptionCost entity : entities) {
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
            throw new RuntimeException("Failed to batch insert crafting_bench_options_costs", e);
        }
    }

    @Override
    public Optional<CraftingBenchOptionCost> findById(Integer pageId) {
        String sql = "SELECT * FROM crafting_bench_options_costs WHERE page_id = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find crafting_bench_option_cost: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<CraftingBenchOptionCost> findAll() {
        List<CraftingBenchOptionCost> list = new ArrayList<>();
        String sql = "SELECT * FROM crafting_bench_options_costs ORDER BY option_id, currency_name";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all crafting_bench_options_costs", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM crafting_bench_options_costs WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete crafting_bench_options_costs: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM crafting_bench_options_costs";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count crafting_bench_options_costs", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, CraftingBenchOptionCost c) throws SQLException {
        ps.setInt(1, c.getPageId());
        ps.setString(2, c.getPageName());
        ps.setInt(3, c.getOptionId());
        ps.setInt(4, c.getAmount());
        ps.setString(5, c.getCurrencyName());
    }

    private CraftingBenchOptionCost mapRow(ResultSet rs) throws SQLException {
        CraftingBenchOptionCost c = new CraftingBenchOptionCost();
        c.setPageId(rs.getInt("page_id"));
        c.setPageName(rs.getString("page_name"));
        c.setOptionId(rs.getInt("option_id"));
        c.setAmount(rs.getInt("amount"));
        c.setCurrencyName(rs.getString("currency_name"));
        return c;
    }
}
