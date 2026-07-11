package com.poe.cache.dao;

import com.poe.cache.model.ModGenerationWeight;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * mod_generation_weights 表数据访问对象。
 * <p>
 * 表使用 (page_id, ordinal) 作为复合主键。
 * 记录词缀在不同生成类型上的权重分布。
 */
public class ModGenerationWeightDao implements CrudRepository<ModGenerationWeight, Integer> {

    private final Connection connection;

    public ModGenerationWeightDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(ModGenerationWeight entity) {
        String sql = "INSERT INTO mod_generation_weights (page_id, page_name, ordinal, tag, value) " +
            "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert mod_generation_weight: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<ModGenerationWeight> entities) {
        String sql = "INSERT INTO mod_generation_weights (page_id, page_name, ordinal, tag, value) " +
            "VALUES (?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (ModGenerationWeight entity : entities) {
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
            throw new RuntimeException("Failed to batch insert mod_generation_weights", e);
        }
    }

    @Override
    public Optional<ModGenerationWeight> findById(Integer pageId) {
        String sql = "SELECT * FROM mod_generation_weights WHERE page_id = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find mod_generation_weight: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<ModGenerationWeight> findAll() {
        List<ModGenerationWeight> list = new ArrayList<>();
        String sql = "SELECT * FROM mod_generation_weights ORDER BY page_id, ordinal";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all mod_generation_weights", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM mod_generation_weights WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete mod_generation_weights: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM mod_generation_weights";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count mod_generation_weights", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, ModGenerationWeight w) throws SQLException {
        ps.setInt(1, w.getPageId());
        ps.setString(2, w.getPageName());
        ps.setInt(3, w.getOrdinal());
        ps.setString(4, w.getTag());
        ps.setInt(5, w.getValue());
    }

    private ModGenerationWeight mapRow(ResultSet rs) throws SQLException {
        ModGenerationWeight w = new ModGenerationWeight();
        w.setPageId(rs.getInt("page_id"));
        w.setPageName(rs.getString("page_name"));
        w.setOrdinal(rs.getInt("ordinal"));
        w.setTag(rs.getString("tag"));
        w.setValue(rs.getInt("value"));
        return w;
    }
}
