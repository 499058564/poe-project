package com.poe.cache.dao;

import com.poe.cache.model.Tattoo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TattooDao implements CrudRepository<Tattoo, Integer> {

    private final Connection connection;

    public TattooDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(Tattoo entity) {
        String sql = "INSERT INTO tattoos (page_id, page_name, max_adjacent, min_adjacent, skill_id, " +
            "target, tattoo_limit, tribe) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert tattoos: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<Tattoo> entities) {
        String sql = "INSERT INTO tattoos (page_id, page_name, max_adjacent, min_adjacent, skill_id, " +
            "target, tattoo_limit, tribe) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Tattoo entity : entities) {
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
            throw new RuntimeException("Failed to batch insert tattoos", e);
        }
    }

    @Override
    public Optional<Tattoo> findById(Integer pageId) {
        String sql = "SELECT * FROM tattoos WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tattoos by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Tattoo> findAll() {
        List<Tattoo> list = new ArrayList<>();
        String sql = "SELECT * FROM tattoos ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all tattoos", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM tattoos WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete tattoos: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM tattoos";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count tattoos", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Tattoo v) throws SQLException {
        ps.setInt(1, v.getPageId());
        ps.setString(2, v.getPageName());
        ps.setInt(3, v.getMaxAdjacent());
        ps.setInt(4, v.getMinAdjacent());
        ps.setString(5, v.getSkillId());
        ps.setString(6, v.getTarget());
        ps.setString(7, v.getTattooLimit());
        ps.setInt(8, v.getTribe());
    }

    private Tattoo mapRow(ResultSet rs) throws SQLException {
        Tattoo v = new Tattoo();
        v.setPageId(rs.getInt("page_id"));
        v.setPageName(rs.getString("page_name"));
        v.setMaxAdjacent(rs.getInt("max_adjacent"));
        v.setMinAdjacent(rs.getInt("min_adjacent"));
        v.setSkillId(rs.getString("skill_id"));
        v.setTarget(rs.getString("target"));
        v.setTattooLimit(rs.getString("tattoo_limit"));
        v.setTribe(rs.getInt("tribe"));
        return v;
    }
}
