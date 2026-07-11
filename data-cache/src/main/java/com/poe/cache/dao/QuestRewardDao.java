package com.poe.cache.dao;

import com.poe.cache.model.QuestReward;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuestRewardDao implements CrudRepository<QuestReward, Integer> {

    private final Connection connection;

    public QuestRewardDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(QuestReward entity) {
        String sql = "INSERT INTO quest_rewards (page_id, page_name, act, class_ids, classes, item_level, " +
            "notes, quest, quest_id, rarity, sockets) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert quest_rewards: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<QuestReward> entities) {
        String sql = "INSERT INTO quest_rewards (page_id, page_name, act, class_ids, classes, item_level, " +
            "notes, quest, quest_id, rarity, sockets) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (QuestReward entity : entities) {
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
            throw new RuntimeException("Failed to batch insert quest_rewards", e);
        }
    }

    @Override
    public Optional<QuestReward> findById(Integer pageId) {
        String sql = "SELECT * FROM quest_rewards WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find quest_rewards by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<QuestReward> findAll() {
        List<QuestReward> list = new ArrayList<>();
        String sql = "SELECT * FROM quest_rewards ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all quest_rewards", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM quest_rewards WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete quest_rewards: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM quest_rewards";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count quest_rewards", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, QuestReward v) throws SQLException {
        ps.setInt(1, v.getPageId());
        ps.setString(2, v.getPageName());
        ps.setInt(3, v.getAct());
        ps.setString(4, v.getClassIds());
        ps.setString(5, v.getClasses());
        ps.setInt(6, v.getItemLevel());
        ps.setString(7, v.getNotes());
        ps.setString(8, v.getQuest());
        ps.setString(9, v.getQuestId());
        ps.setString(10, v.getRarity());
        ps.setInt(11, v.getSockets());
    }

    private QuestReward mapRow(ResultSet rs) throws SQLException {
        QuestReward v = new QuestReward();
        v.setPageId(rs.getInt("page_id"));
        v.setPageName(rs.getString("page_name"));
        v.setAct(rs.getInt("act"));
        v.setClassIds(rs.getString("class_ids"));
        v.setClasses(rs.getString("classes"));
        v.setItemLevel(rs.getInt("item_level"));
        v.setNotes(rs.getString("notes"));
        v.setQuest(rs.getString("quest"));
        v.setQuestId(rs.getString("quest_id"));
        v.setRarity(rs.getString("rarity"));
        v.setSockets(rs.getInt("sockets"));
        return v;
    }
}
