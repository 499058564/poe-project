package com.poe.cache.dao;

import com.poe.cache.model.Stackable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * stackables 表数据访问对象。
 */
public class StackableDao implements CrudRepository<Stackable, Integer> {

    private final Connection connection;

    public StackableDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(Stackable s) {
        String sql = "INSERT INTO stackables (page_id, page_name, stack_size, stack_size_currency_tab) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, s);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert stackable: " + s.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<Stackable> stackables) {
        String sql = "INSERT INTO stackables (page_id, page_name, stack_size, stack_size_currency_tab) VALUES (?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Stackable s : stackables) {
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
            throw new RuntimeException("Failed to batch insert stackables", e);
        }
    }

    @Override
    public Optional<Stackable> findById(Integer pageId) {
        String sql = "SELECT * FROM stackables WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find stackable: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Stackable> findAll() {
        List<Stackable> list = new ArrayList<>();
        String sql = "SELECT * FROM stackables ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all stackables", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM stackables WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete stackable: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM stackables";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count stackables", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Stackable s) throws SQLException {
        ps.setInt(1, s.getPageId());
        ps.setString(2, s.getPageName());
        ps.setInt(3, s.getStackSize());
        ps.setInt(4, s.getStackSizeCurrencyTab());
    }

    private Stackable mapRow(ResultSet rs) throws SQLException {
        Stackable s = new Stackable();
        s.setPageId(rs.getInt("page_id"));
        s.setPageName(rs.getString("page_name"));
        s.setStackSize(rs.getInt("stack_size"));
        s.setStackSizeCurrencyTab(rs.getInt("stack_size_currency_tab"));
        return s;
    }
}
