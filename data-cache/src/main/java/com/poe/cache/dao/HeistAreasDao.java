package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.HeistAreas;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HeistAreasDao implements CrudRepository<HeistAreas, Integer> {

    private final DataSource dataSource;

    public HeistAreasDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(HeistAreas entity) {
        String sql = "INSERT INTO heist_areas (page_id, page_name, area_id, area_ids, blueprint_id, "
            + "contract_id, job_ids, reward_text) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert heist_areas: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<HeistAreas> entities) {
        String sql = "INSERT INTO heist_areas (page_id, page_name, area_id, area_ids, blueprint_id, "
            + "contract_id, job_ids, reward_text) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (HeistAreas entity : entities) {
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
            throw new RuntimeException("Failed to batch insert heist_areas", e);
        }
    }

    @Override
    public Optional<HeistAreas> findById(Integer pageId) {
        String sql = "SELECT * FROM heist_areas WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find heist_areas by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<HeistAreas> findAll() {
        List<HeistAreas> list = new ArrayList<>();
        String sql = "SELECT * FROM heist_areas ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all heist_areas", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM heist_areas WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete heist_areas: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM heist_areas";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count heist_areas", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, HeistAreas h) throws SQLException {
        ps.setInt(1, h.getPageId());
        ps.setString(2, h.getPageName());
        ps.setString(3, h.getAreaId());
        ps.setString(4, h.getAreaIds());
        ps.setString(5, h.getBlueprintId());
        ps.setString(6, h.getContractId());
        ps.setString(7, h.getJobIds());
        ps.setString(8, h.getRewardText());
    }

    private HeistAreas mapRow(ResultSet rs) throws SQLException {
        HeistAreas h = new HeistAreas();
        h.setPageId(rs.getInt("page_id"));
        h.setPageName(rs.getString("page_name"));
        h.setAreaId(rs.getString("area_id"));
        h.setAreaIds(rs.getString("area_ids"));
        h.setBlueprintId(rs.getString("blueprint_id"));
        h.setContractId(rs.getString("contract_id"));
        h.setJobIds(rs.getString("job_ids"));
        h.setRewardText(rs.getString("reward_text"));
        return h;
    }
}
