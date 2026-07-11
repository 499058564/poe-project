package com.poe.cache.dao;

import com.poe.cache.model.MapSeries;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * map_series 表数据访问对象。
 */
public class MapSeriesDao implements CrudRepository<MapSeries, Integer> {

    private final Connection connection;

    public MapSeriesDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(MapSeries ms) {
        String sql = "INSERT INTO map_series (page_id, page_name, series_id, name, ordinal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, ms);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert map series: " + ms.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<MapSeries> series) {
        String sql = "INSERT INTO map_series (page_id, page_name, series_id, name, ordinal) VALUES (?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (MapSeries ms : series) {
                    setParams(ps, ms);
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
            throw new RuntimeException("Failed to batch insert map series", e);
        }
    }

    @Override
    public Optional<MapSeries> findById(Integer pageId) {
        String sql = "SELECT * FROM map_series WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find map series: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<MapSeries> findAll() {
        List<MapSeries> list = new ArrayList<>();
        String sql = "SELECT * FROM map_series ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all map series", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM map_series WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete map series: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM map_series";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count map series", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, MapSeries ms) throws SQLException {
        ps.setInt(1, ms.getPageId());
        ps.setString(2, ms.getPageName());
        ps.setString(3, ms.getSeriesId());
        ps.setString(4, ms.getName());
        ps.setInt(5, ms.getOrdinal());
    }

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
