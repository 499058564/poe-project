package com.poe.cache.dao;

import com.poe.cache.model.BestiaryRecipes;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BestiaryRecipesDao implements CrudRepository<BestiaryRecipes, Integer> {

    private final Connection connection;

    public BestiaryRecipesDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(BestiaryRecipes entity) {
        String sql = "INSERT INTO bestiary_recipes (page_id, page_name, recipe_id, game_mode, "
            + "header, notes, subheader) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert bestiary_recipes: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<BestiaryRecipes> entities) {
        String sql = "INSERT INTO bestiary_recipes (page_id, page_name, recipe_id, game_mode, "
            + "header, notes, subheader) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (BestiaryRecipes entity : entities) {
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
            throw new RuntimeException("Failed to batch insert bestiary_recipes", e);
        }
    }

    @Override
    public Optional<BestiaryRecipes> findById(Integer pageId) {
        String sql = "SELECT * FROM bestiary_recipes WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find bestiary_recipes by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<BestiaryRecipes> findAll() {
        List<BestiaryRecipes> list = new ArrayList<>();
        String sql = "SELECT * FROM bestiary_recipes ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all bestiary_recipes", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM bestiary_recipes WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete bestiary_recipes: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM bestiary_recipes";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count bestiary_recipes", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, BestiaryRecipes b) throws SQLException {
        ps.setInt(1, b.getPageId());
        ps.setString(2, b.getPageName());
        ps.setString(3, b.getRecipeId());
        ps.setString(4, b.getGameMode());
        ps.setString(5, b.getHeader());
        ps.setString(6, b.getNotes());
        ps.setString(7, b.getSubheader());
    }

    private BestiaryRecipes mapRow(ResultSet rs) throws SQLException {
        BestiaryRecipes b = new BestiaryRecipes();
        b.setPageId(rs.getInt("page_id"));
        b.setPageName(rs.getString("page_name"));
        b.setRecipeId(rs.getString("recipe_id"));
        b.setGameMode(rs.getString("game_mode"));
        b.setHeader(rs.getString("header"));
        b.setNotes(rs.getString("notes"));
        b.setSubheader(rs.getString("subheader"));
        return b;
    }
}
