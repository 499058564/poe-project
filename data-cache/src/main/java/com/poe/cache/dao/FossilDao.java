package com.poe.cache.dao;

import com.poe.cache.model.Fossil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * fossils 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键。
 * 记录挖矿化石的属性配置。
 */
public class FossilDao implements CrudRepository<Fossil, Integer> {

    private final javax.sql.DataSource dataSource;

    public FossilDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(Fossil entity) {
        String sql = "INSERT INTO fossils (page_id, page_name, added_modifier_ids, " +
            "allowed_tags, base_item_id, can_enchant, can_mirror, can_quality, " +
            "can_roll_white_sockets, corrupted_essence_chance, forbidden_tags, " +
            "forced_modifier_ids, is_lucky, sell_price_modifier_ids) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert fossil: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<Fossil> entities) {
        String sql = "INSERT INTO fossils (page_id, page_name, added_modifier_ids, " +
            "allowed_tags, base_item_id, can_enchant, can_mirror, can_quality, " +
            "can_roll_white_sockets, corrupted_essence_chance, forbidden_tags, " +
            "forced_modifier_ids, is_lucky, sell_price_modifier_ids) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Fossil entity : entities) {
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
            throw new RuntimeException("Failed to batch insert fossils", e);
        }
    }

    @Override
    public Optional<Fossil> findById(Integer pageId) {
        String sql = "SELECT * FROM fossils WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find fossil: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Fossil> findAll() {
        List<Fossil> list = new ArrayList<>();
        String sql = "SELECT * FROM fossils ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all fossils", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM fossils WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete fossil: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM fossils";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count fossils", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Fossil f) throws SQLException {
        int idx = 1;
        ps.setInt(idx++, f.getPageId());
        ps.setString(idx++, f.getPageName());
        ps.setString(idx++, f.getAddedModifierIds());
        ps.setString(idx++, f.getAllowedTags());
        ps.setString(idx++, f.getBaseItemId());
        ps.setInt(idx++, f.isCanEnchant() ? 1 : 0);
        ps.setInt(idx++, f.isCanMirror() ? 1 : 0);
        ps.setInt(idx++, f.isCanQuality() ? 1 : 0);
        ps.setInt(idx++, f.isCanRollWhiteSockets() ? 1 : 0);
        ps.setInt(idx++, f.getCorruptedEssenceChance());
        ps.setString(idx++, f.getForbiddenTags());
        ps.setString(idx++, f.getForcedModifierIds());
        ps.setInt(idx++, f.isLucky() ? 1 : 0);
        ps.setString(idx++, f.getSellPriceModifierIds());
    }

    private Fossil mapRow(ResultSet rs) throws SQLException {
        Fossil f = new Fossil();
        f.setPageId(rs.getInt("page_id"));
        f.setPageName(rs.getString("page_name"));
        f.setAddedModifierIds(rs.getString("added_modifier_ids"));
        f.setAllowedTags(rs.getString("allowed_tags"));
        f.setBaseItemId(rs.getString("base_item_id"));
        f.setCanEnchant(rs.getInt("can_enchant") == 1);
        f.setCanMirror(rs.getInt("can_mirror") == 1);
        f.setCanQuality(rs.getInt("can_quality") == 1);
        f.setCanRollWhiteSockets(rs.getInt("can_roll_white_sockets") == 1);
        f.setCorruptedEssenceChance(rs.getInt("corrupted_essence_chance"));
        f.setForbiddenTags(rs.getString("forbidden_tags"));
        f.setForcedModifierIds(rs.getString("forced_modifier_ids"));
        f.setLucky(rs.getInt("is_lucky") == 1);
        f.setSellPriceModifierIds(rs.getString("sell_price_modifier_ids"));
        return f;
    }
}
