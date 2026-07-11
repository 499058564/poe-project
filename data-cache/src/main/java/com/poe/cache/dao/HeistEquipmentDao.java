package com.poe.cache.dao;

import com.poe.cache.model.HeistEquipment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HeistEquipmentDao implements CrudRepository<HeistEquipment, Integer> {

    private final javax.sql.DataSource dataSource;

    public HeistEquipmentDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(HeistEquipment entity) {
        String sql = "INSERT INTO heist_equipment (page_id, page_name, required_job_id, required_job_level) "
            + "VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert heist_equipment: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<HeistEquipment> entities) {
        String sql = "INSERT INTO heist_equipment (page_id, page_name, required_job_id, required_job_level) "
            + "VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (HeistEquipment entity : entities) {
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
            throw new RuntimeException("Failed to batch insert heist_equipment", e);
        }
    }

    @Override
    public Optional<HeistEquipment> findById(Integer pageId) {
        String sql = "SELECT * FROM heist_equipment WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find heist_equipment by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<HeistEquipment> findAll() {
        List<HeistEquipment> list = new ArrayList<>();
        String sql = "SELECT * FROM heist_equipment ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all heist_equipment", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM heist_equipment WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete heist_equipment: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM heist_equipment";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count heist_equipment", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, HeistEquipment h) throws SQLException {
        ps.setInt(1, h.getPageId());
        ps.setString(2, h.getPageName());
        ps.setString(3, h.getRequiredJobId());
        ps.setInt(4, h.getRequiredJobLevel());
    }

    private HeistEquipment mapRow(ResultSet rs) throws SQLException {
        HeistEquipment h = new HeistEquipment();
        h.setPageId(rs.getInt("page_id"));
        h.setPageName(rs.getString("page_name"));
        h.setRequiredJobId(rs.getString("required_job_id"));
        h.setRequiredJobLevel(rs.getInt("required_job_level"));
        return h;
    }
}
