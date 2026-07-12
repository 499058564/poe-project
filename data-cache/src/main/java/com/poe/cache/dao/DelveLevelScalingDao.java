package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.DelveLevelScaling;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DelveLevelScalingDao implements CrudRepository<DelveLevelScaling, Integer> {

    private final DataSource dataSource;

    public DelveLevelScalingDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(DelveLevelScaling entity) {
        String sql = "INSERT INTO delve_level_scaling (depth, page_name, darkness_resistance, light_radius, "
            + "monster_damage, monster_level, monster_life, sulphite_cost) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert delve_level_scaling: " + entity.getDepth(), e);
        }
    }

    @Override
    public void batchInsert(List<DelveLevelScaling> entities) {
        String sql = "INSERT INTO delve_level_scaling (depth, page_name, darkness_resistance, light_radius, "
            + "monster_damage, monster_level, monster_life, sulphite_cost) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (DelveLevelScaling entity : entities) {
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
            throw new RuntimeException("Failed to batch insert delve_level_scaling", e);
        }
    }

    @Override
    public Optional<DelveLevelScaling> findById(Integer depth) {
        String sql = "SELECT * FROM delve_level_scaling WHERE depth = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, depth);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find delve_level_scaling by depth: " + depth, e);
        }
        return Optional.empty();
    }

    @Override
    public List<DelveLevelScaling> findAll() {
        List<DelveLevelScaling> list = new ArrayList<>();
        String sql = "SELECT * FROM delve_level_scaling ORDER BY depth";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all delve_level_scaling", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer depth) {
        String sql = "DELETE FROM delve_level_scaling WHERE depth = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, depth);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete delve_level_scaling: " + depth, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM delve_level_scaling";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count delve_level_scaling", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, DelveLevelScaling d) throws SQLException {
        ps.setInt(1, d.getDepth());
        ps.setString(2, d.getPageName());
        ps.setInt(3, d.getDarknessResistance());
        ps.setDouble(4, d.getLightRadius());
        ps.setDouble(5, d.getMonsterDamage());
        ps.setInt(6, d.getMonsterLevel());
        ps.setDouble(7, d.getMonsterLife());
        ps.setInt(8, d.getSulphiteCost());
    }

    private DelveLevelScaling mapRow(ResultSet rs) throws SQLException {
        DelveLevelScaling d = new DelveLevelScaling();
        d.setDepth(rs.getInt("depth"));
        d.setPageName(rs.getString("page_name"));
        d.setDarknessResistance(rs.getInt("darkness_resistance"));
        d.setLightRadius(rs.getDouble("light_radius"));
        d.setMonsterDamage(rs.getDouble("monster_damage"));
        d.setMonsterLevel(rs.getInt("monster_level"));
        d.setMonsterLife(rs.getDouble("monster_life"));
        d.setSulphiteCost(rs.getInt("sulphite_cost"));
        return d;
    }
}
