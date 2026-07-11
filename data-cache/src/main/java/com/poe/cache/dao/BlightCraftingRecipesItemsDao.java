package com.poe.cache.dao;

import com.poe.cache.model.BlightCraftingRecipesItems;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BlightCraftingRecipesItemsDao implements CrudRepository<BlightCraftingRecipesItems, Integer> {

    private final javax.sql.DataSource dataSource;

    public BlightCraftingRecipesItemsDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(BlightCraftingRecipesItems entity) {
        String sql = "INSERT INTO blight_crafting_recipes_items (page_id, page_name, item_id, ordinal, recipe_id) "
            + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert blight_crafting_recipes_items: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<BlightCraftingRecipesItems> entities) {
        String sql = "INSERT INTO blight_crafting_recipes_items (page_id, page_name, item_id, ordinal, recipe_id) "
            + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (BlightCraftingRecipesItems entity : entities) {
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
            throw new RuntimeException("Failed to batch insert blight_crafting_recipes_items", e);
        }
    }

    @Override
    public Optional<BlightCraftingRecipesItems> findById(Integer pageId) {
        String sql = "SELECT * FROM blight_crafting_recipes_items WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find blight_crafting_recipes_items by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<BlightCraftingRecipesItems> findAll() {
        List<BlightCraftingRecipesItems> list = new ArrayList<>();
        String sql = "SELECT * FROM blight_crafting_recipes_items ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all blight_crafting_recipes_items", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM blight_crafting_recipes_items WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete blight_crafting_recipes_items: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM blight_crafting_recipes_items";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count blight_crafting_recipes_items", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, BlightCraftingRecipesItems b) throws SQLException {
        ps.setInt(1, b.getPageId());
        ps.setString(2, b.getPageName());
        ps.setString(3, b.getItemId());
        ps.setInt(4, b.getOrdinal());
        ps.setString(5, b.getRecipeId());
    }

    private BlightCraftingRecipesItems mapRow(ResultSet rs) throws SQLException {
        BlightCraftingRecipesItems b = new BlightCraftingRecipesItems();
        b.setPageId(rs.getInt("page_id"));
        b.setPageName(rs.getString("page_name"));
        b.setItemId(rs.getString("item_id"));
        b.setOrdinal(rs.getInt("ordinal"));
        b.setRecipeId(rs.getString("recipe_id"));
        return b;
    }
}
