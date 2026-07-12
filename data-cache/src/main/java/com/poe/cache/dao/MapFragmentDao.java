package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.MapFragment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * map_fragments 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键，通过 page_name 与 base_items 表关联。
 * 记录地图碎片类物品的堆叠上限。
 */
public class MapFragmentDao implements CrudRepository<MapFragment, Integer> {

    private final DataSource dataSource;

    public MapFragmentDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 插入单条地图碎片记录。
     *
     * @param mf 地图碎片实体
     */
    @Override
    public void insert(MapFragment mf) {
        String sql = "INSERT INTO map_fragments (page_id, page_name, map_fragment_limit) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, mf);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert map fragment: " + mf.getPageId(), e);
        }
    }

    /**
     * 批量插入地图碎片记录（事务）。
     * <p>
     * 使用 JDBC batch + 事务保证原子性，失败自动回滚。
     *
     * @param fragments 地图碎片实体列表（非空）
     */
    @Override
    public void batchInsert(List<MapFragment> fragments) {
        String sql = "INSERT INTO map_fragments (page_id, page_name, map_fragment_limit) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (MapFragment mf : fragments) {
                    setParams(ps, mf);
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
            throw new RuntimeException("Failed to batch insert map fragments", e);
        }
    }

    /**
     * 根据主键查询地图碎片。
     *
     * @param pageId Wiki 页面 ID
     * @return 地图碎片实体（可能为空）
     */
    @Override
    public Optional<MapFragment> findById(Integer pageId) {
        String sql = "SELECT * FROM map_fragments WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find map fragment: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部地图碎片记录。
     *
     * @return 按 page_id 升序排列的地图碎片列表
     */
    @Override
    public List<MapFragment> findAll() {
        List<MapFragment> list = new ArrayList<>();
        String sql = "SELECT * FROM map_fragments ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all map fragments", e);
        }
        return list;
    }

    /**
     * 根据主键删除地图碎片。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM map_fragments WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete map fragment: " + pageId, e);
        }
    }

    /**
     * 统计地图碎片记录总数。
     *
     * @return map_fragments 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM map_fragments";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count map fragments", e);
        }
        return 0;
    }

    /** 设置 PreparedStatement 参数（绑定 MapFragment 字段） */
    private void setParams(PreparedStatement ps, MapFragment mf) throws SQLException {
        ps.setInt(1, mf.getPageId());
        ps.setString(2, mf.getPageName());
        ps.setInt(3, mf.getMapFragmentLimit());
    }

    /** 从 ResultSet 映射一行到 MapFragment 实体 */
    private MapFragment mapRow(ResultSet rs) throws SQLException {
        MapFragment mf = new MapFragment();
        mf.setPageId(rs.getInt("page_id"));
        mf.setPageName(rs.getString("page_name"));
        mf.setMapFragmentLimit(rs.getInt("map_fragment_limit"));
        return mf;
    }
}
