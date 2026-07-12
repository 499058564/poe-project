package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.HeistJobs;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HeistJobsDao implements CrudRepository<HeistJobs, Integer> {

    private final DataSource dataSource;

    public HeistJobsDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(HeistJobs entity) {
        String sql = "INSERT INTO heist_jobs (page_id, page_name, job_id, name) "
            + "VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert heist_jobs: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<HeistJobs> entities) {
        String sql = "INSERT INTO heist_jobs (page_id, page_name, job_id, name) "
            + "VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (HeistJobs entity : entities) {
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
            throw new RuntimeException("Failed to batch insert heist_jobs", e);
        }
    }

    @Override
    public Optional<HeistJobs> findById(Integer pageId) {
        String sql = "SELECT * FROM heist_jobs WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find heist_jobs by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<HeistJobs> findAll() {
        List<HeistJobs> list = new ArrayList<>();
        String sql = "SELECT * FROM heist_jobs ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all heist_jobs", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM heist_jobs WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete heist_jobs: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM heist_jobs";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count heist_jobs", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, HeistJobs h) throws SQLException {
        ps.setInt(1, h.getPageId());
        ps.setString(2, h.getPageName());
        ps.setString(3, h.getJobId());
        ps.setString(4, h.getName());
    }

    private HeistJobs mapRow(ResultSet rs) throws SQLException {
        HeistJobs h = new HeistJobs();
        h.setPageId(rs.getInt("page_id"));
        h.setPageName(rs.getString("page_name"));
        h.setJobId(rs.getString("job_id"));
        h.setName(rs.getString("name"));
        return h;
    }
}
