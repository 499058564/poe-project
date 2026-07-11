package com.poe.cache.dao;

import com.poe.cache.model.BlightCraftingRecipes;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BlightCraftingRecipesDao implements CrudRepository<BlightCraftingRecipes, Integer> {

    private final javax.sql.DataSource dataSource;

    public BlightCraftingRecipesDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(BlightCraftingRecipes entity) {
        String sql = "INSERT INTO blight_crafting_recipes (page_id, page_name, recipe_id, modifier_id, "
            + "passive_id, type) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert blight_crafting_recipes: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<BlightCraftingRecipes> entities) {
        String sql = "INSERT INTO blight_crafting_recipes (page_id, page_name, recipe_id, modifier_id, "
            + "passive_id, type) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (BlightCraftingRecipes entity : entities) {
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
            throw new RuntimeException("Failed to batch insert blight_crafting_recipes", e);
        }
    }

    @Override
    public Optional<BlightCraftingRecipes> findById(Integer pageId) {
        String sql = "SELECT * FROM blight_crafting_recipes WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find blight_crafting_recipes by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<BlightCraftingRecipes> findAll() {
        List<BlightCraftingRecipes> list = new ArrayList<>();
        String sql = "SELECT * FROM blight_crafting_recipes ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all blight_crafting_recipes", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM blight_crafting_recipes WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete blight_crafting_recipes: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM blight_crafting_recipes";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count blight_crafting_recipes", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, BlightCraftingRecipes b) throws SQLException {
        ps.setInt(1, b.getPageId());
        ps.setString(2, b.getPageName());
        ps.setString(3, b.getRecipeId());
        ps.setString(4, b.getModifierId());
        ps.setString(5, b.getPassiveId());
        ps.setString(6, b.getType());
    }

    private BlightCraftingRecipes mapRow(ResultSet rs) throws SQLException {
        BlightCraftingRecipes b = new BlightCraftingRecipes();
        b.setPageId(rs.getInt("page_id"));
        b.setPageName(rs.getString("page_name"));
        b.setRecipeId(rs.getString("recipe_id"));
        b.setModifierId(rs.getString("modifier_id"));
        b.setPassiveId(rs.getString("passive_id"));
        b.setType(rs.getString("type"));
        return b;
    }
}
