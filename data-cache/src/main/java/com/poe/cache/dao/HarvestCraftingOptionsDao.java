package com.poe.cache.dao;

import com.poe.cache.model.HarvestCraftingOptions;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HarvestCraftingOptionsDao implements CrudRepository<HarvestCraftingOptions, Integer> {

    private final javax.sql.DataSource dataSource;

    public HarvestCraftingOptionsDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(HarvestCraftingOptions entity) {
        String sql = "INSERT INTO harvest_crafting_options (page_id, page_name, option_id, cost_primal, "
            + "cost_rancour, cost_sacred, cost_vivid, cost_wild, effect, effect_html, ordinal) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert harvest_crafting_options: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<HarvestCraftingOptions> entities) {
        String sql = "INSERT INTO harvest_crafting_options (page_id, page_name, option_id, cost_primal, "
            + "cost_rancour, cost_sacred, cost_vivid, cost_wild, effect, effect_html, ordinal) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (HarvestCraftingOptions entity : entities) {
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
            throw new RuntimeException("Failed to batch insert harvest_crafting_options", e);
        }
    }

    @Override
    public Optional<HarvestCraftingOptions> findById(Integer pageId) {
        String sql = "SELECT * FROM harvest_crafting_options WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find harvest_crafting_options by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<HarvestCraftingOptions> findAll() {
        List<HarvestCraftingOptions> list = new ArrayList<>();
        String sql = "SELECT * FROM harvest_crafting_options ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all harvest_crafting_options", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM harvest_crafting_options WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete harvest_crafting_options: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM harvest_crafting_options";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count harvest_crafting_options", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, HarvestCraftingOptions h) throws SQLException {
        ps.setInt(1, h.getPageId());
        ps.setString(2, h.getPageName());
        ps.setString(3, h.getOptionId());
        ps.setInt(4, h.getCostPrimal());
        ps.setInt(5, h.getCostRancour());
        ps.setInt(6, h.getCostSacred());
        ps.setInt(7, h.getCostVivid());
        ps.setInt(8, h.getCostWild());
        ps.setString(9, h.getEffect());
        ps.setString(10, h.getEffectHtml());
        ps.setInt(11, h.getOrdinal());
    }

    private HarvestCraftingOptions mapRow(ResultSet rs) throws SQLException {
        HarvestCraftingOptions h = new HarvestCraftingOptions();
        h.setPageId(rs.getInt("page_id"));
        h.setPageName(rs.getString("page_name"));
        h.setOptionId(rs.getString("option_id"));
        h.setCostPrimal(rs.getInt("cost_primal"));
        h.setCostRancour(rs.getInt("cost_rancour"));
        h.setCostSacred(rs.getInt("cost_sacred"));
        h.setCostVivid(rs.getInt("cost_vivid"));
        h.setCostWild(rs.getInt("cost_wild"));
        h.setEffect(rs.getString("effect"));
        h.setEffectHtml(rs.getString("effect_html"));
        h.setOrdinal(rs.getInt("ordinal"));
        return h;
    }
}
