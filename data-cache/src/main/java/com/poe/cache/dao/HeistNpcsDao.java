package com.poe.cache.dao;

import com.poe.cache.model.HeistNpcs;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HeistNpcsDao implements CrudRepository<HeistNpcs, Integer> {

    private final Connection connection;

    public HeistNpcsDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(HeistNpcs entity) {
        String sql = "INSERT INTO heist_npcs (page_id, page_name, npc_id, job_id, name, stat_text) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert heist_npcs: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<HeistNpcs> entities) {
        String sql = "INSERT INTO heist_npcs (page_id, page_name, npc_id, job_id, name, stat_text) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (HeistNpcs entity : entities) {
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
            throw new RuntimeException("Failed to batch insert heist_npcs", e);
        }
    }

    @Override
    public Optional<HeistNpcs> findById(Integer pageId) {
        String sql = "SELECT * FROM heist_npcs WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find heist_npcs by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<HeistNpcs> findAll() {
        List<HeistNpcs> list = new ArrayList<>();
        String sql = "SELECT * FROM heist_npcs ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all heist_npcs", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM heist_npcs WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete heist_npcs: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM heist_npcs";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count heist_npcs", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, HeistNpcs h) throws SQLException {
        ps.setInt(1, h.getPageId());
        ps.setString(2, h.getPageName());
        ps.setString(3, h.getNpcId());
        ps.setString(4, h.getJobId());
        ps.setString(5, h.getName());
        ps.setString(6, h.getStatText());
    }

    private HeistNpcs mapRow(ResultSet rs) throws SQLException {
        HeistNpcs h = new HeistNpcs();
        h.setPageId(rs.getInt("page_id"));
        h.setPageName(rs.getString("page_name"));
        h.setNpcId(rs.getString("npc_id"));
        h.setJobId(rs.getString("job_id"));
        h.setName(rs.getString("name"));
        h.setStatText(rs.getString("stat_text"));
        return h;
    }
}
