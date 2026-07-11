package com.poe.cache.dao;

import com.poe.cache.model.MapFragment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * map_fragments 表数据访问对象。
 */
public class MapFragmentDao implements CrudRepository<MapFragment, Integer> {

    private final Connection connection;

    public MapFragmentDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(MapFragment mf) {
        String sql = "INSERT INTO map_fragments (page_id, page_name, map_fragment_limit) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, mf);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert map fragment: " + mf.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<MapFragment> fragments) {
        String sql = "INSERT INTO map_fragments (page_id, page_name, map_fragment_limit) VALUES (?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (MapFragment mf : fragments) {
                    setParams(ps, mf);
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
            throw new RuntimeException("Failed to batch insert map fragments", e);
        }
    }

    @Override
    public Optional<MapFragment> findById(Integer pageId) {
        String sql = "SELECT * FROM map_fragments WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find map fragment: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<MapFragment> findAll() {
        List<MapFragment> list = new ArrayList<>();
        String sql = "SELECT * FROM map_fragments ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all map fragments", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM map_fragments WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete map fragment: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM map_fragments";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count map fragments", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, MapFragment mf) throws SQLException {
        ps.setInt(1, mf.getPageId());
        ps.setString(2, mf.getPageName());
        ps.setInt(3, mf.getMapFragmentLimit());
    }

    private MapFragment mapRow(ResultSet rs) throws SQLException {
        MapFragment mf = new MapFragment();
        mf.setPageId(rs.getInt("page_id"));
        mf.setPageName(rs.getString("page_name"));
        mf.setMapFragmentLimit(rs.getInt("map_fragment_limit"));
        return mf;
    }
}
