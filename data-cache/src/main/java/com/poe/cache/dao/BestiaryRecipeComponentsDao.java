package com.poe.cache.dao;

import com.poe.cache.model.BestiaryRecipeComponents;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BestiaryRecipeComponentsDao implements CrudRepository<BestiaryRecipeComponents, Integer> {

    private final javax.sql.DataSource dataSource;

    public BestiaryRecipeComponentsDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(BestiaryRecipeComponents entity) {
        String sql = "INSERT INTO bestiary_recipe_components (page_id, page_name, amount, component_id, recipe_id) "
            + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (BestiaryRecipeComponents entity : entities) {
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
            throw new RuntimeException("Failed to batch insert bestiary_recipe_components", e);
        }
    }

    @Override
    public Optional<BestiaryRecipeComponents> findById(Integer pageId) {
        String sql = "SELECT * FROM bestiary_recipe_components WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
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
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete bestiary_recipe_components: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM bestiary_recipe_components";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
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
