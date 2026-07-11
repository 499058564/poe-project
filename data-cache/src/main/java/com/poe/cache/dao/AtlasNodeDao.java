package com.poe.cache.dao;

import com.poe.cache.model.AtlasNode;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * atlas_nodes 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键，记录异界图鉴节点的区域、层级与连接信息。
 */
public class AtlasNodeDao implements CrudRepository<AtlasNode, Integer> {

    private final javax.sql.DataSource dataSource;

    public AtlasNodeDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 插入单条异界图鉴节点记录。
     *
     * @param entity 异界图鉴节点实体
     */
    @Override
    public void insert(AtlasNode entity) {
        String sql = "INSERT INTO atlas_nodes (page_id, page_name, area_id, connections, "
            + "div_cards, id, is_off_atlas, region_connections_0, region_connections_1, "
            + "region_connections_2, region_connections_3, region_connections_4, "
            + "region_id, region_minimum, series_id, tier_0, tier_1, tier_2, tier_3, tier_4) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert atlas node: " + entity.getPageId(), e);
        }
    }

    /**
     * 批量插入异界图鉴节点记录（事务）。
     *
     * @param entities 异界图鉴节点实体列表（非空）
     */
    @Override
    public void batchInsert(List<AtlasNode> entities) {
        String sql = "INSERT INTO atlas_nodes (page_id, page_name, area_id, connections, "
            + "div_cards, id, is_off_atlas, region_connections_0, region_connections_1, "
            + "region_connections_2, region_connections_3, region_connections_4, "
            + "region_id, region_minimum, series_id, tier_0, tier_1, tier_2, tier_3, tier_4) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (AtlasNode entity : entities) {
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
            throw new RuntimeException("Failed to batch insert atlas nodes", e);
        }
    }

    /**
     * 根据主键查询异界图鉴节点。
     *
     * @param pageId Wiki 页面 ID
     * @return 异界图鉴节点实体（可能为空）
     */
    @Override
    public Optional<AtlasNode> findById(Integer pageId) {
        String sql = "SELECT * FROM atlas_nodes WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find atlas node by id: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部异界图鉴节点记录。
     *
     * @return 按 page_id 升序排列的异界图鉴节点列表
     */
    @Override
    public List<AtlasNode> findAll() {
        List<AtlasNode> list = new ArrayList<>();
        String sql = "SELECT * FROM atlas_nodes ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all atlas nodes", e);
        }
        return list;
    }

    /**
     * 根据主键删除异界图鉴节点。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM atlas_nodes WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete atlas node: " + pageId, e);
        }
    }

    /**
     * 统计异界图鉴节点记录总数。
     *
     * @return atlas_nodes 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM atlas_nodes";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count atlas nodes", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, AtlasNode an) throws SQLException {
        ps.setInt(1, an.getPageId());
        ps.setString(2, an.getPageName());
        ps.setString(3, an.getAreaId());
        ps.setString(4, an.getConnections());
        ps.setString(5, an.getDivCards());
        ps.setString(6, an.getId());
        ps.setInt(7, an.isOffAtlas() ? 1 : 0);
        ps.setString(8, an.getRegionConnections0());
        ps.setString(9, an.getRegionConnections1());
        ps.setString(10, an.getRegionConnections2());
        ps.setString(11, an.getRegionConnections3());
        ps.setString(12, an.getRegionConnections4());
        ps.setString(13, an.getRegionId());
        ps.setInt(14, an.getRegionMinimum());
        ps.setInt(15, an.getSeriesId());
        ps.setInt(16, an.getTier0());
        ps.setInt(17, an.getTier1());
        ps.setInt(18, an.getTier2());
        ps.setInt(19, an.getTier3());
        ps.setInt(20, an.getTier4());
    }

    private AtlasNode mapRow(ResultSet rs) throws SQLException {
        AtlasNode an = new AtlasNode();
        an.setPageId(rs.getInt("page_id"));
        an.setPageName(rs.getString("page_name"));
        an.setAreaId(rs.getString("area_id"));
        an.setConnections(rs.getString("connections"));
        an.setDivCards(rs.getString("div_cards"));
        an.setId(rs.getString("id"));
        an.setOffAtlas(rs.getInt("is_off_atlas") == 1);
        an.setRegionConnections0(rs.getString("region_connections_0"));
        an.setRegionConnections1(rs.getString("region_connections_1"));
        an.setRegionConnections2(rs.getString("region_connections_2"));
        an.setRegionConnections3(rs.getString("region_connections_3"));
        an.setRegionConnections4(rs.getString("region_connections_4"));
        an.setRegionId(rs.getString("region_id"));
        an.setRegionMinimum(rs.getInt("region_minimum"));
        an.setSeriesId(rs.getInt("series_id"));
        an.setTier0(rs.getInt("tier_0"));
        an.setTier1(rs.getInt("tier_1"));
        an.setTier2(rs.getInt("tier_2"));
        an.setTier3(rs.getInt("tier_3"));
        an.setTier4(rs.getInt("tier_4"));
        return an;
    }
}
