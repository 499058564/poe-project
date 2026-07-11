package com.poe.cache.dao;

import com.poe.cache.model.Flask;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * flasks 表数据访问对象。
 */
public class FlaskDao implements CrudRepository<Flask, Integer> {

    private final Connection connection;

    public FlaskDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(Flask f) {
        String sql = "INSERT INTO flasks (page_id, page_name, charges_max, charges_per_use, duration, life, mana) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, f);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert flask: " + f.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<Flask> flasks) {
        String sql = "INSERT INTO flasks (page_id, page_name, charges_max, charges_per_use, duration, life, mana) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Flask f : flasks) {
                    setParams(ps, f);
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
            throw new RuntimeException("Failed to batch insert flasks", e);
        }
    }

    @Override
    public Optional<Flask> findById(Integer pageId) {
        String sql = "SELECT * FROM flasks WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find flask: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Flask> findAll() {
        List<Flask> list = new ArrayList<>();
        String sql = "SELECT * FROM flasks ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all flasks", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM flasks WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete flask: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM flasks";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count flasks", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Flask f) throws SQLException {
        ps.setInt(1, f.getPageId());
        ps.setString(2, f.getPageName());
        ps.setInt(3, f.getChargesMax());
        ps.setInt(4, f.getChargesPerUse());
        ps.setDouble(5, f.getDuration());
        ps.setInt(6, f.getLife());
        ps.setInt(7, f.getMana());
    }

    private Flask mapRow(ResultSet rs) throws SQLException {
        Flask f = new Flask();
        f.setPageId(rs.getInt("page_id"));
        f.setPageName(rs.getString("page_name"));
        f.setChargesMax(rs.getInt("charges_max"));
        f.setChargesPerUse(rs.getInt("charges_per_use"));
        f.setDuration(rs.getDouble("duration"));
        f.setLife(rs.getInt("life"));
        f.setMana(rs.getInt("mana"));
        return f;
    }
}
