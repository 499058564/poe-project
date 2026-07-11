package com.poe.cache.dao;

import com.poe.cache.model.HarvestSeeds;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HarvestSeedsDao implements CrudRepository<HarvestSeeds, Integer> {

    private final Connection connection;

    public HarvestSeedsDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(HarvestSeeds entity) {
        String sql = "INSERT INTO harvest_seeds (page_id, page_name, consumed_primal_lifeforce_percentage, "
            + "consumed_vivid_lifeforce_percentage, consumed_wild_lifeforce_percentage, effect, "
            + "granted_craft_option_ids, growth_cycles, required_nearby_seed_amount, "
            + "required_nearby_seed_tier, tier, type, type_id) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert harvest_seeds: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<HarvestSeeds> entities) {
        String sql = "INSERT INTO harvest_seeds (page_id, page_name, consumed_primal_lifeforce_percentage, "
            + "consumed_vivid_lifeforce_percentage, consumed_wild_lifeforce_percentage, effect, "
            + "granted_craft_option_ids, growth_cycles, required_nearby_seed_amount, "
            + "required_nearby_seed_tier, tier, type, type_id) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (HarvestSeeds entity : entities) {
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
            throw new RuntimeException("Failed to batch insert harvest_seeds", e);
        }
    }

    @Override
    public Optional<HarvestSeeds> findById(Integer pageId) {
        String sql = "SELECT * FROM harvest_seeds WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find harvest_seeds by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<HarvestSeeds> findAll() {
        List<HarvestSeeds> list = new ArrayList<>();
        String sql = "SELECT * FROM harvest_seeds ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all harvest_seeds", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM harvest_seeds WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete harvest_seeds: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM harvest_seeds";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count harvest_seeds", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, HarvestSeeds h) throws SQLException {
        ps.setInt(1, h.getPageId());
        ps.setString(2, h.getPageName());
        ps.setInt(3, h.getConsumedPrimalLifeforcePercentage());
        ps.setInt(4, h.getConsumedVividLifeforcePercentage());
        ps.setInt(5, h.getConsumedWildLifeforcePercentage());
        ps.setString(6, h.getEffect());
        ps.setString(7, h.getGrantedCraftOptionIds());
        ps.setInt(8, h.getGrowthCycles());
        ps.setInt(9, h.getRequiredNearbySeedAmount());
        ps.setInt(10, h.getRequiredNearbySeedTier());
        ps.setInt(11, h.getTier());
        ps.setString(12, h.getType());
        ps.setInt(13, h.getTypeId());
    }

    private HarvestSeeds mapRow(ResultSet rs) throws SQLException {
        HarvestSeeds h = new HarvestSeeds();
        h.setPageId(rs.getInt("page_id"));
        h.setPageName(rs.getString("page_name"));
        h.setConsumedPrimalLifeforcePercentage(rs.getInt("consumed_primal_lifeforce_percentage"));
        h.setConsumedVividLifeforcePercentage(rs.getInt("consumed_vivid_lifeforce_percentage"));
        h.setConsumedWildLifeforcePercentage(rs.getInt("consumed_wild_lifeforce_percentage"));
        h.setEffect(rs.getString("effect"));
        h.setGrantedCraftOptionIds(rs.getString("granted_craft_option_ids"));
        h.setGrowthCycles(rs.getInt("growth_cycles"));
        h.setRequiredNearbySeedAmount(rs.getInt("required_nearby_seed_amount"));
        h.setRequiredNearbySeedTier(rs.getInt("required_nearby_seed_tier"));
        h.setTier(rs.getInt("tier"));
        h.setType(rs.getString("type"));
        h.setTypeId(rs.getInt("type_id"));
        return h;
    }
}
