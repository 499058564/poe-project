package com.poe.cache.dao;

import com.poe.cache.model.Amulet;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * amulets 表数据访问对象。
 */
public class AmuletDao implements CrudRepository<Amulet, Integer> {

    private final Connection connection;

    public AmuletDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(Amulet a) {
        String sql = "INSERT INTO amulets (page_id, page_name, is_talisman, talisman_tier) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, a);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert amulet: " + a.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<Amulet> amulets) {
        String sql = "INSERT INTO amulets (page_id, page_name, is_talisman, talisman_tier) VALUES (?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Amulet a : amulets) {
                    setParams(ps, a);
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
            throw new RuntimeException("Failed to batch insert amulets", e);
        }
    }

    @Override
    public Optional<Amulet> findById(Integer pageId) {
        String sql = "SELECT * FROM amulets WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find amulet: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Amulet> findAll() {
        List<Amulet> list = new ArrayList<>();
        String sql = "SELECT * FROM amulets ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all amulets", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM amulets WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete amulet: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM amulets";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count amulets", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Amulet a) throws SQLException {
        ps.setInt(1, a.getPageId());
        ps.setString(2, a.getPageName());
        ps.setBoolean(3, a.isTalisman());
        ps.setInt(4, a.getTalismanTier());
    }

    private Amulet mapRow(ResultSet rs) throws SQLException {
        Amulet a = new Amulet();
        a.setPageId(rs.getInt("page_id"));
        a.setPageName(rs.getString("page_name"));
        a.setTalisman(rs.getBoolean("is_talisman"));
        a.setTalismanTier(rs.getInt("talisman_tier"));
        return a;
    }
}
