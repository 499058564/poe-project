package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.ItemStat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * item_stats 表数据访问对象。
 * <p>
 * 表使用 (page_id, stat_id) 作为复合主键。
 * 记录暗金等物品的固定词缀属性值。
 */
public class ItemStatDao implements CrudRepository<ItemStat, Integer> {

    private final DataSource dataSource;

    public ItemStatDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(ItemStat entity) {
        String sql = "INSERT INTO item_stats (page_id, page_name, avg, stat_id, " +
            "max_value, min_value, mod_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert item_stat: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<ItemStat> entities) {
        String sql = "INSERT INTO item_stats (page_id, page_name, avg, stat_id, " +
            "max_value, min_value, mod_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (ItemStat entity : entities) {
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
            throw new RuntimeException("Failed to batch insert item_stats", e);
        }
    }

    @Override
    public Optional<ItemStat> findById(Integer pageId) {
        String sql = "SELECT * FROM item_stats WHERE page_id = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find item_stat: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<ItemStat> findAll() {
        List<ItemStat> list = new ArrayList<>();
        String sql = "SELECT * FROM item_stats ORDER BY page_id, stat_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all item_stats", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM item_stats WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete item_stats: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM item_stats";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count item_stats", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, ItemStat s) throws SQLException {
        ps.setInt(1, s.getPageId());
        ps.setString(2, s.getPageName());
        ps.setInt(3, s.getAvg());
        ps.setString(4, s.getStatId());
        ps.setInt(5, s.getMaxValue());
        ps.setInt(6, s.getMinValue());
        ps.setString(7, s.getModId());
    }

    private ItemStat mapRow(ResultSet rs) throws SQLException {
        ItemStat s = new ItemStat();
        s.setPageId(rs.getInt("page_id"));
        s.setPageName(rs.getString("page_name"));
        s.setAvg(rs.getInt("avg"));
        s.setStatId(rs.getString("stat_id"));
        s.setMaxValue(rs.getInt("max_value"));
        s.setMinValue(rs.getInt("min_value"));
        s.setModId(rs.getString("mod_id"));
        return s;
    }
}
