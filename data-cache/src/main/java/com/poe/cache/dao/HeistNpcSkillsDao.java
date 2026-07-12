package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.HeistNpcSkills;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HeistNpcSkillsDao implements CrudRepository<HeistNpcSkills, Integer> {

    private final DataSource dataSource;

    public HeistNpcSkillsDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(HeistNpcSkills entity) {
        String sql = "INSERT INTO heist_npc_skills (page_id, page_name, job_id, level, npc_id) "
            + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert heist_npc_skills: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<HeistNpcSkills> entities) {
        String sql = "INSERT INTO heist_npc_skills (page_id, page_name, job_id, level, npc_id) "
            + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (HeistNpcSkills entity : entities) {
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
            throw new RuntimeException("Failed to batch insert heist_npc_skills", e);
        }
    }

    @Override
    public Optional<HeistNpcSkills> findById(Integer pageId) {
        String sql = "SELECT * FROM heist_npc_skills WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find heist_npc_skills by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<HeistNpcSkills> findAll() {
        List<HeistNpcSkills> list = new ArrayList<>();
        String sql = "SELECT * FROM heist_npc_skills ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all heist_npc_skills", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM heist_npc_skills WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete heist_npc_skills: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM heist_npc_skills";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count heist_npc_skills", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, HeistNpcSkills h) throws SQLException {
        ps.setInt(1, h.getPageId());
        ps.setString(2, h.getPageName());
        ps.setString(3, h.getJobId());
        ps.setString(4, h.getLevel());
        ps.setString(5, h.getNpcId());
    }

    private HeistNpcSkills mapRow(ResultSet rs) throws SQLException {
        HeistNpcSkills h = new HeistNpcSkills();
        h.setPageId(rs.getInt("page_id"));
        h.setPageName(rs.getString("page_name"));
        h.setJobId(rs.getString("job_id"));
        h.setLevel(rs.getString("level"));
        h.setNpcId(rs.getString("npc_id"));
        return h;
    }
}
