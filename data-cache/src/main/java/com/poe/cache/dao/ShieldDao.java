package com.poe.cache.dao;

import com.poe.cache.model.Shield;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * shields 表数据访问对象。
 */
public class ShieldDao implements CrudRepository<Shield, Integer> {

    private final Connection connection;

    public ShieldDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(Shield s) {
        String sql = "INSERT INTO shields (page_id, page_name, block) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, s);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert shield: " + s.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<Shield> shields) {
        String sql = "INSERT INTO shields (page_id, page_name, block) VALUES (?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Shield s : shields) {
                    setParams(ps, s);
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
            throw new RuntimeException("Failed to batch insert shields", e);
        }
    }

    @Override
    public Optional<Shield> findById(Integer pageId) {
        String sql = "SELECT * FROM shields WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find shield: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Shield> findAll() {
        List<Shield> list = new ArrayList<>();
        String sql = "SELECT * FROM shields ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all shields", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM shields WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete shield: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM shields";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count shields", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Shield s) throws SQLException {
        ps.setInt(1, s.getPageId());
        ps.setString(2, s.getPageName());
        ps.setInt(3, s.getBlock());
    }

    private Shield mapRow(ResultSet rs) throws SQLException {
        Shield s = new Shield();
        s.setPageId(rs.getInt("page_id"));
        s.setPageName(rs.getString("page_name"));
        s.setBlock(rs.getInt("block"));
        return s;
    }
}
