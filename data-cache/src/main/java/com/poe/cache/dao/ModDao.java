package com.poe.cache.dao;

import com.poe.cache.model.Mod;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * mods 表数据访问对象。
 */
public class ModDao implements CrudRepository<Mod, Integer> {

    private final Connection connection;

    public ModDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(Mod mod) {
        String sql = "INSERT INTO mods (id, name, name_zh, mod_type, domain, generation_type, " +
            "mod_group, stats, spawn_tags, spawn_weights, required_level, version) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, mod);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert mod: " + mod.getId(), e);
        }
    }

    /**
     * 批量插入，在单个事务内执行以提高性能。
     * 任一行插入失败时，整个批次回滚并恢复自动提交模式。
     */
    @Override
    public void batchInsert(List<Mod> mods) {
        String sql = "INSERT INTO mods (id, name, name_zh, mod_type, domain, generation_type, " +
            "mod_group, stats, spawn_tags, spawn_weights, required_level, version) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Mod mod : mods) {
                    setParams(ps, mod);
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
            throw new RuntimeException("Failed to batch insert mods", e);
        }
    }

    @Override
    public Optional<Mod> findById(Integer id) {
        String sql = "SELECT * FROM mods WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find mod by id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Mod> findAll() {
        String sql = "SELECT * FROM mods ORDER BY id";
        List<Mod> mods = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                mods.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all mods", e);
        }
        return mods;
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM mods WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete mod by id: " + id, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM mods";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count mods", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Mod mod) throws SQLException {
        ps.setInt(1, mod.getId());
        ps.setString(2, mod.getName());
        ps.setString(3, mod.getNameZh());
        ps.setString(4, mod.getModType());
        ps.setString(5, mod.getDomain());
        ps.setString(6, mod.getGenerationType());
        ps.setString(7, mod.getModGroup());
        ps.setString(8, mod.getStats());
        ps.setString(9, mod.getSpawnTags());
        ps.setString(10, mod.getSpawnWeights());
        ps.setInt(11, mod.getRequiredLevel());
        ps.setString(12, mod.getVersion());
    }

    private Mod mapRow(ResultSet rs) throws SQLException {
        Mod mod = new Mod();
        mod.setId(rs.getInt("id"));
        mod.setName(rs.getString("name"));
        mod.setNameZh(rs.getString("name_zh"));
        mod.setModType(rs.getString("mod_type"));
        mod.setDomain(rs.getString("domain"));
        mod.setGenerationType(rs.getString("generation_type"));
        mod.setModGroup(rs.getString("mod_group"));
        mod.setStats(rs.getString("stats"));
        mod.setSpawnTags(rs.getString("spawn_tags"));
        mod.setSpawnWeights(rs.getString("spawn_weights"));
        mod.setRequiredLevel(rs.getInt("required_level"));
        mod.setVersion(rs.getString("version"));
        return mod;
    }
}
