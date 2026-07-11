package com.poe.cache.dao;

import com.poe.cache.model.Jewel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * jewels 表数据访问对象。
 */
public class JewelDao implements CrudRepository<Jewel, Integer> {

    private final Connection connection;

    public JewelDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(Jewel j) {
        String sql = "INSERT INTO jewels (page_id, page_name, jewel_limit, radius_html) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, j);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert jewel: " + j.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<Jewel> jewels) {
        String sql = "INSERT INTO jewels (page_id, page_name, jewel_limit, radius_html) VALUES (?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Jewel j : jewels) {
                    setParams(ps, j);
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
            throw new RuntimeException("Failed to batch insert jewels", e);
        }
    }

    @Override
    public Optional<Jewel> findById(Integer pageId) {
        String sql = "SELECT * FROM jewels WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find jewel: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Jewel> findAll() {
        List<Jewel> list = new ArrayList<>();
        String sql = "SELECT * FROM jewels ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all jewels", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM jewels WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete jewel: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM jewels";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count jewels", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Jewel j) throws SQLException {
        ps.setInt(1, j.getPageId());
        ps.setString(2, j.getPageName());
        ps.setString(3, j.getJewelLimit());
        ps.setString(4, j.getRadiusHtml());
    }

    private Jewel mapRow(ResultSet rs) throws SQLException {
        Jewel j = new Jewel();
        j.setPageId(rs.getInt("page_id"));
        j.setPageName(rs.getString("page_name"));
        j.setJewelLimit(rs.getString("jewel_limit"));
        j.setRadiusHtml(rs.getString("radius_html"));
        return j;
    }
}
