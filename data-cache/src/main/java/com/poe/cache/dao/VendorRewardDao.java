package com.poe.cache.dao;

import com.poe.cache.model.VendorReward;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * vendor_rewards 表数据访问对象。
 * <p>
 * 表使用 (page_id, quest_id, npc) 作为复合主键。
 * 记录任务完成后可从 NPC 获得的物品奖励信息。
 */
public class VendorRewardDao implements CrudRepository<VendorReward, Integer> {

    private final Connection connection;

    public VendorRewardDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(VendorReward entity) {
        String sql = "INSERT INTO vendor_rewards (page_id, page_name, act, " +
            "class_ids, classes, npc, quest, quest_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert vendor_reward: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<VendorReward> entities) {
        String sql = "INSERT INTO vendor_rewards (page_id, page_name, act, " +
            "class_ids, classes, npc, quest, quest_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (VendorReward entity : entities) {
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
            throw new RuntimeException("Failed to batch insert vendor_rewards", e);
        }
    }

    @Override
    public Optional<VendorReward> findById(Integer pageId) {
        String sql = "SELECT * FROM vendor_rewards WHERE page_id = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find vendor_reward: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<VendorReward> findAll() {
        List<VendorReward> list = new ArrayList<>();
        String sql = "SELECT * FROM vendor_rewards ORDER BY page_id, quest_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all vendor_rewards", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM vendor_rewards WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete vendor_rewards: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM vendor_rewards";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count vendor_rewards", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, VendorReward r) throws SQLException {
        ps.setInt(1, r.getPageId());
        ps.setString(2, r.getPageName());
        ps.setInt(3, r.getAct());
        ps.setString(4, r.getClassIds());
        ps.setString(5, r.getClasses());
        ps.setString(6, r.getNpc());
        ps.setString(7, r.getQuest());
        ps.setString(8, r.getQuestId());
    }

    private VendorReward mapRow(ResultSet rs) throws SQLException {
        VendorReward r = new VendorReward();
        r.setPageId(rs.getInt("page_id"));
        r.setPageName(rs.getString("page_name"));
        r.setAct(rs.getInt("act"));
        r.setClassIds(rs.getString("class_ids"));
        r.setClasses(rs.getString("classes"));
        r.setNpc(rs.getString("npc"));
        r.setQuest(rs.getString("quest"));
        r.setQuestId(rs.getString("quest_id"));
        return r;
    }
}
