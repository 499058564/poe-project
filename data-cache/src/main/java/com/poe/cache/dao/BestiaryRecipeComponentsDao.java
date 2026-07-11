package com.poe.cache.dao;

import com.poe.cache.model.BestiaryRecipeComponents;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BestiaryRecipeComponentsDao implements CrudRepository<BestiaryRecipeComponents, Integer> {

    private final Connection connection;

    public BestiaryRecipeComponentsDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(BestiaryRecipeComponents entity) {
        String sql = "INSERT INTO bestiary_recipe_components (page_id, page_name, amount, component_id, recipe_id) "
            + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert bestiary_recipe_components: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<BestiaryRecipeComponents> entities) {
        String sql = "INSERT INTO bestiary_recipe_components (page_id, page_name, amount, component_id, recipe_id) "
            + "VALUES (?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (BestiaryRecipeComponents entity : entities) {
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
            throw new RuntimeException("Failed to batch insert bestiary_recipe_components", e);
        }
    }

    @Override
    public Optional<BestiaryRecipeComponents> findById(Integer pageId) {
        String sql = "SELECT * FROM bestiary_recipe_components WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find bestiary_recipe_components by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<BestiaryRecipeComponents> findAll() {
        List<BestiaryRecipeComponents> list = new ArrayList<>();
        String sql = "SELECT * FROM bestiary_recipe_components ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all bestiary_recipe_components", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM bestiary_recipe_components WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete bestiary_recipe_components: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM bestiary_recipe_components";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count bestiary_recipe_components", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, BestiaryRecipeComponents b) throws SQLException {
        ps.setInt(1, b.getPageId());
        ps.setString(2, b.getPageName());
        ps.setInt(3, b.getAmount());
        ps.setString(4, b.getComponentId());
        ps.setString(5, b.getRecipeId());
    }

    private BestiaryRecipeComponents mapRow(ResultSet rs) throws SQLException {
        BestiaryRecipeComponents b = new BestiaryRecipeComponents();
        b.setPageId(rs.getInt("page_id"));
        b.setPageName(rs.getString("page_name"));
        b.setAmount(rs.getInt("amount"));
        b.setComponentId(rs.getString("component_id"));
        b.setRecipeId(rs.getString("recipe_id"));
        return b;
    }
}
