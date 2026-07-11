package com.poe.cache.dao;

import com.poe.cache.model.MonsterResistance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * monster_resistances 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键，记录怪物在剧情第一部/第二部及异界地图的抗性配置。
 * resistance_id 对应 Cargo 的 id 字段。
 */
public class MonsterResistanceDao implements CrudRepository<MonsterResistance, Integer> {

    private final Connection connection;

    public MonsterResistanceDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * 插入单条怪物抗性记录。
     *
     * @param entity 怪物抗性实体
     */
    @Override
    public void insert(MonsterResistance entity) {
        String sql = "INSERT INTO monster_resistances (page_id, page_name, resistance_id, "
            + "maps_chaos, maps_cold, maps_fire, maps_lightning, "
            + "part1_chaos, part1_cold, part1_fire, part1_lightning, "
            + "part2_chaos, part2_cold, part2_fire, part2_lightning) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert monster resistance: " + entity.getPageId(), e);
        }
    }

    /**
     * 批量插入怪物抗性记录（事务）。
     *
     * @param entities 怪物抗性实体列表（非空）
     */
    @Override
    public void batchInsert(List<MonsterResistance> entities) {
        String sql = "INSERT INTO monster_resistances (page_id, page_name, resistance_id, "
            + "maps_chaos, maps_cold, maps_fire, maps_lightning, "
            + "part1_chaos, part1_cold, part1_fire, part1_lightning, "
            + "part2_chaos, part2_cold, part2_fire, part2_lightning) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (MonsterResistance entity : entities) {
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
            throw new RuntimeException("Failed to batch insert monster resistances", e);
        }
    }

    /**
     * 根据主键查询怪物抗性。
     *
     * @param pageId Wiki 页面 ID
     * @return 怪物抗性实体（可能为空）
     */
    @Override
    public Optional<MonsterResistance> findById(Integer pageId) {
        String sql = "SELECT * FROM monster_resistances WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find monster resistance by id: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部怪物抗性记录。
     *
     * @return 按 page_id 升序排列的怪物抗性列表
     */
    @Override
    public List<MonsterResistance> findAll() {
        List<MonsterResistance> list = new ArrayList<>();
        String sql = "SELECT * FROM monster_resistances ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all monster resistances", e);
        }
        return list;
    }

    /**
     * 根据主键删除怪物抗性。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM monster_resistances WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete monster resistance: " + pageId, e);
        }
    }

    /**
     * 统计怪物抗性记录总数。
     *
     * @return monster_resistances 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM monster_resistances";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count monster resistances", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, MonsterResistance mr) throws SQLException {
        ps.setInt(1, mr.getPageId());
        ps.setString(2, mr.getPageName());
        ps.setString(3, mr.getResistanceId());
        ps.setInt(4, mr.getMapsChaos());
        ps.setInt(5, mr.getMapsCold());
        ps.setInt(6, mr.getMapsFire());
        ps.setInt(7, mr.getMapsLightning());
        ps.setInt(8, mr.getPart1Chaos());
        ps.setInt(9, mr.getPart1Cold());
        ps.setInt(10, mr.getPart1Fire());
        ps.setInt(11, mr.getPart1Lightning());
        ps.setInt(12, mr.getPart2Chaos());
        ps.setInt(13, mr.getPart2Cold());
        ps.setInt(14, mr.getPart2Fire());
        ps.setInt(15, mr.getPart2Lightning());
    }

    private MonsterResistance mapRow(ResultSet rs) throws SQLException {
        MonsterResistance mr = new MonsterResistance();
        mr.setPageId(rs.getInt("page_id"));
        mr.setPageName(rs.getString("page_name"));
        mr.setResistanceId(rs.getString("resistance_id"));
        mr.setMapsChaos(rs.getInt("maps_chaos"));
        mr.setMapsCold(rs.getInt("maps_cold"));
        mr.setMapsFire(rs.getInt("maps_fire"));
        mr.setMapsLightning(rs.getInt("maps_lightning"));
        mr.setPart1Chaos(rs.getInt("part1_chaos"));
        mr.setPart1Cold(rs.getInt("part1_cold"));
        mr.setPart1Fire(rs.getInt("part1_fire"));
        mr.setPart1Lightning(rs.getInt("part1_lightning"));
        mr.setPart2Chaos(rs.getInt("part2_chaos"));
        mr.setPart2Cold(rs.getInt("part2_cold"));
        mr.setPart2Fire(rs.getInt("part2_fire"));
        mr.setPart2Lightning(rs.getInt("part2_lightning"));
        return mr;
    }
}
