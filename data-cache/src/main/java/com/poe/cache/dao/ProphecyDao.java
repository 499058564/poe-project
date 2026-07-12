package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.Prophecy;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProphecyDao implements CrudRepository<Prophecy, Integer> {

    private final DataSource dataSource;

    public ProphecyDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(Prophecy entity) {
        String sql = "INSERT INTO prophecies (page_id, page_name, objective, prediction_text, prophecy_id, " +
            "reward, seal_cost) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert prophecies: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<Prophecy> entities) {
        String sql = "INSERT INTO prophecies (page_id, page_name, objective, prediction_text, prophecy_id, " +
            "reward, seal_cost) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Prophecy entity : entities) {
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
            throw new RuntimeException("Failed to batch insert prophecies", e);
        }
    }

    @Override
    public Optional<Prophecy> findById(Integer pageId) {
        String sql = "SELECT * FROM prophecies WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find prophecies by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Prophecy> findAll() {
        List<Prophecy> list = new ArrayList<>();
        String sql = "SELECT * FROM prophecies ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all prophecies", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM prophecies WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete prophecies: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM prophecies";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count prophecies", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Prophecy v) throws SQLException {
        ps.setInt(1, v.getPageId());
        ps.setString(2, v.getPageName());
        ps.setString(3, v.getObjective());
        ps.setString(4, v.getPredictionText());
        ps.setString(5, v.getProphecyId());
        ps.setString(6, v.getReward());
        ps.setInt(7, v.getSealCost());
    }

    private Prophecy mapRow(ResultSet rs) throws SQLException {
        Prophecy v = new Prophecy();
        v.setPageId(rs.getInt("page_id"));
        v.setPageName(rs.getString("page_name"));
        v.setObjective(rs.getString("objective"));
        v.setPredictionText(rs.getString("prediction_text"));
        v.setProphecyId(rs.getString("prophecy_id"));
        v.setReward(rs.getString("reward"));
        v.setSealCost(rs.getInt("seal_cost"));
        return v;
    }
}
