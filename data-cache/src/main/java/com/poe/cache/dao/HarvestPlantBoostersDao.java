package com.poe.cache.dao;

import com.poe.cache.model.HarvestPlantBoosters;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HarvestPlantBoostersDao implements CrudRepository<HarvestPlantBoosters, Integer> {

    private final Connection connection;

    public HarvestPlantBoostersDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(HarvestPlantBoosters entity) {
        String sql = "INSERT INTO harvest_plant_boosters (page_id, page_name, additional_crafting_options, "
            + "extra_chances, lifeforce, radius) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert harvest_plant_boosters: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<HarvestPlantBoosters> entities) {
        String sql = "INSERT INTO harvest_plant_boosters (page_id, page_name, additional_crafting_options, "
            + "extra_chances, lifeforce, radius) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (HarvestPlantBoosters entity : entities) {
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
            throw new RuntimeException("Failed to batch insert harvest_plant_boosters", e);
        }
    }

    @Override
    public Optional<HarvestPlantBoosters> findById(Integer pageId) {
        String sql = "SELECT * FROM harvest_plant_boosters WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find harvest_plant_boosters by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<HarvestPlantBoosters> findAll() {
        List<HarvestPlantBoosters> list = new ArrayList<>();
        String sql = "SELECT * FROM harvest_plant_boosters ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all harvest_plant_boosters", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM harvest_plant_boosters WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete harvest_plant_boosters: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM harvest_plant_boosters";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count harvest_plant_boosters", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, HarvestPlantBoosters h) throws SQLException {
        ps.setInt(1, h.getPageId());
        ps.setString(2, h.getPageName());
        ps.setString(3, h.getAdditionalCraftingOptions());
        ps.setString(4, h.getExtraChances());
        ps.setString(5, h.getLifeforce());
        ps.setInt(6, h.getRadius());
    }

    private HarvestPlantBoosters mapRow(ResultSet rs) throws SQLException {
        HarvestPlantBoosters h = new HarvestPlantBoosters();
        h.setPageId(rs.getInt("page_id"));
        h.setPageName(rs.getString("page_name"));
        h.setAdditionalCraftingOptions(rs.getString("additional_crafting_options"));
        h.setExtraChances(rs.getString("extra_chances"));
        h.setLifeforce(rs.getString("lifeforce"));
        h.setRadius(rs.getInt("radius"));
        return h;
    }
}
