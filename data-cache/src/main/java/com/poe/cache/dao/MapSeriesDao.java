package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.MapSeries;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * map_series 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键，通过 page_name 与 base_items 表关联。
 * 记录异界地图系列的名称、标识和排序序号。
 */
public class MapSeriesDao implements CrudRepository<MapSeries, Integer> {

    private final DataSource dataSource;

    public MapSeriesDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 插入单条地图系列记录。
     *
     * @param ms 地图系列实体
     */
    @Override
    public void insert(MapSeries ms) {
        String sql = "INSERT INTO map_series (page_id, page_name, series_id, name, ordinal) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, ms);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert map series: " + ms.getPageId(), e);
        }
    }

    /**
     * 批量插入地图系列记录（事务）。
     * <p>
     * 使用 JDBC batch + 事务保证原子性，失败自动回滚。
     *
     * @param series 地图系列实体列表（非空）
     */
    @Override
    public void batchInsert(List<MapSeries> series) {
        String sql = "INSERT INTO map_series (page_id, page_name, series_id, name, ordinal) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (MapSeries ms : series) {
                    setParams(ps, ms);
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
            throw new RuntimeException("Failed to batch insert map series", e);
        }
    }

    /**
     * 根据主键查询地图系列。
     *
     * @param pageId Wiki 页面 ID
     * @return 地图系列实体（可能为空）
     */
    @Override
    public Optional<MapSeries> findById(Integer pageId) {
        String sql = "SELECT * FROM map_series WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find map series: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部地图系列记录。
     *
     * @return 按 page_id 升序排列的地图系列列表
     */
    @Override
    public List<MapSeries> findAll() {
        List<MapSeries> list = new ArrayList<>();
        String sql = "SELECT * FROM map_series ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all map series", e);
        }
        return list;
    }

    /**
     * 根据主键删除地图系列。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM map_series WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete map series: " + pageId, e);
        }
    }

    /**
     * 统计地图系列记录总数。
     *
     * @return map_series 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM map_series";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count map series", e);
        }
        return 0;
    }

    /** 设置 PreparedStatement 参数（绑定 MapSeries 字段） */
    private void setParams(PreparedStatement ps, MapSeries ms) throws SQLException {
        ps.setInt(1, ms.getPageId());
        ps.setString(2, ms.getPageName());
        ps.setString(3, ms.getSeriesId());
        ps.setString(4, ms.getName());
        ps.setInt(5, ms.getOrdinal());
    }

    /** 从 ResultSet 映射一行到 MapSeries 实体 */
    private MapSeries mapRow(ResultSet rs) throws SQLException {
        MapSeries ms = new MapSeries();
        ms.setPageId(rs.getInt("page_id"));
        ms.setPageName(rs.getString("page_name"));
        ms.setSeriesId(rs.getString("series_id"));
        ms.setName(rs.getString("name"));
        ms.setOrdinal(rs.getInt("ordinal"));
        return ms;
    }
}
