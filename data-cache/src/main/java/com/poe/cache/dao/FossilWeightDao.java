package com.poe.cache.dao;

import com.poe.cache.model.FossilWeight;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * fossil_weights 表数据访问对象。
 * <p>
 * 表使用 (base_item_id, tag) 作为复合主键。
 * 记录化石对各类词缀标签的出现权重影响。
 */
public class FossilWeightDao implements CrudRepository<FossilWeight, Integer> {

    private final Connection connection;

    public FossilWeightDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(FossilWeight entity) {
        String sql = "INSERT INTO fossil_weights (page_id, page_name, base_item_id, " +
            "ordinal, tag, weight_type, weight) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert fossil_weight: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<FossilWeight> entities) {
        String sql = "INSERT INTO fossil_weights (page_id, page_name, base_item_id, " +
            "ordinal, tag, weight_type, weight) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (FossilWeight entity : entities) {
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
            throw new RuntimeException("Failed to batch insert fossil_weights", e);
        }
    }

    @Override
    public Optional<FossilWeight> findById(Integer pageId) {
        String sql = "SELECT * FROM fossil_weights WHERE page_id = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find fossil_weight: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<FossilWeight> findAll() {
        List<FossilWeight> list = new ArrayList<>();
        String sql = "SELECT * FROM fossil_weights ORDER BY base_item_id, tag";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all fossil_weights", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM fossil_weights WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete fossil_weights: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM fossil_weights";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count fossil_weights", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, FossilWeight w) throws SQLException {
        ps.setInt(1, w.getPageId());
        ps.setString(2, w.getPageName());
        ps.setString(3, w.getBaseItemId());
        ps.setInt(4, w.getOrdinal());
        ps.setString(5, w.getTag());
        ps.setString(6, w.getWeightType());
        ps.setInt(7, w.getWeight());
    }

    private FossilWeight mapRow(ResultSet rs) throws SQLException {
        FossilWeight w = new FossilWeight();
        w.setPageId(rs.getInt("page_id"));
        w.setPageName(rs.getString("page_name"));
        w.setBaseItemId(rs.getString("base_item_id"));
        w.setOrdinal(rs.getInt("ordinal"));
        w.setTag(rs.getString("tag"));
        w.setWeightType(rs.getString("weight_type"));
        w.setWeight(rs.getInt("weight"));
        return w;
    }
}
