package com.poe.cache.dao;

import com.poe.cache.model.GameMap;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * maps 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键，通过 page_name 与 base_items 表关联。
 * 记录异界地图的区域等级、系列、阶级等信息，包括传奇版本独有的属性。
 */
public class MapDao implements CrudRepository<GameMap, Integer> {

    private final Connection connection;

    public MapDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * 插入单条地图记录。
     *
     * @param m 地图实体
     */
    @Override
    public void insert(GameMap m) {
        String sql = "INSERT INTO maps (page_id, page_name, area_id, area_level, guild_character, "
            + "series, tier, unique_area_id, unique_area_level, unique_guild_character) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, m);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert map: " + m.getPageId(), e);
        }
    }

    /**
     * 批量插入地图记录（事务）。
     * <p>
     * 使用 JDBC batch + 事务保证原子性，失败自动回滚。
     *
     * @param maps 地图实体列表（非空）
     */
    @Override
    public void batchInsert(List<GameMap> maps) {
        String sql = "INSERT INTO maps (page_id, page_name, area_id, area_level, guild_character, "
            + "series, tier, unique_area_id, unique_area_level, unique_guild_character) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (GameMap m : maps) {
                    setParams(ps, m);
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
            throw new RuntimeException("Failed to batch insert maps", e);
        }
    }

    /**
     * 根据主键查询地图。
     *
     * @param pageId Wiki 页面 ID
     * @return 地图实体（可能为空）
     */
    @Override
    public Optional<GameMap> findById(Integer pageId) {
        String sql = "SELECT * FROM maps WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find map: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部地图记录。
     *
     * @return 按 page_id 升序排列的地图列表
     */
    @Override
    public List<GameMap> findAll() {
        List<GameMap> list = new ArrayList<>();
        String sql = "SELECT * FROM maps ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all maps", e);
        }
        return list;
    }

    /**
     * 根据主键删除地图。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM maps WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete map: " + pageId, e);
        }
    }

    /**
     * 统计地图记录总数。
     *
     * @return maps 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM maps";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count maps", e);
        }
        return 0;
    }

    /** 设置 PreparedStatement 参数（绑定 GameMap 字段） */
    private void setParams(PreparedStatement ps, GameMap m) throws SQLException {
        ps.setInt(1, m.getPageId());
        ps.setString(2, m.getPageName());
        ps.setString(3, m.getAreaId());
        ps.setInt(4, m.getAreaLevel());
        ps.setString(5, m.getGuildCharacter());
        ps.setString(6, m.getSeries());
        ps.setInt(7, m.getTier());
        ps.setString(8, m.getUniqueAreaId());
        ps.setInt(9, m.getUniqueAreaLevel());
        ps.setString(10, m.getUniqueGuildCharacter());
    }

    /** 从 ResultSet 映射一行到 GameMap 实体 */
    private GameMap mapRow(ResultSet rs) throws SQLException {
        GameMap m = new GameMap();
        m.setPageId(rs.getInt("page_id"));
        m.setPageName(rs.getString("page_name"));
        m.setAreaId(rs.getString("area_id"));
        m.setAreaLevel(rs.getInt("area_level"));
        m.setGuildCharacter(rs.getString("guild_character"));
        m.setSeries(rs.getString("series"));
        m.setTier(rs.getInt("tier"));
        m.setUniqueAreaId(rs.getString("unique_area_id"));
        m.setUniqueAreaLevel(rs.getInt("unique_area_level"));
        m.setUniqueGuildCharacter(rs.getString("unique_guild_character"));
        return m;
    }
}
