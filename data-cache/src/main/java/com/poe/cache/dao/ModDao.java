package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.Mod;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * mods 表数据访问对象。
 * <p>
 * 表使用 id 作为主键。
 * 记录装备词缀的完整信息，包括类型、域、生成方式、属性标签和权重。
 */
public class ModDao implements CrudRepository<Mod, Integer> {

    private final DataSource dataSource;

    public ModDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 插入单条词缀记录。
     *
     * @param mod 词缀实体
     */
    @Override
    public void insert(Mod mod) {
        String sql = "INSERT INTO mods (id, name, name_zh, mod_type, domain, generation_type, " +
            "mod_group, stats, spawn_tags, spawn_weights, required_level, " +
            "tier_text, granted_buff_id, granted_buff_value, granted_skill, version) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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
            "mod_group, stats, spawn_tags, spawn_weights, required_level, " +
            "tier_text, granted_buff_id, granted_buff_value, granted_skill, version) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Mod mod : mods) {
                    setParams(ps, mod);
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
            throw new RuntimeException("Failed to batch insert mods", e);
        }
    }

    /**
     * 根据主键查询词缀。
     *
     * @param id 词缀 ID
     * @return 词缀实体（可能为空）
     */
    @Override
    public Optional<Mod> findById(Integer id) {
        String sql = "SELECT * FROM mods WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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

    /**
     * 查询全部词缀记录。
     *
     * @return 按 id 升序排列的词缀列表
     */
    @Override
    public List<Mod> findAll() {
        String sql = "SELECT * FROM mods ORDER BY id";
        List<Mod> mods = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                mods.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all mods", e);
        }
        return mods;
    }

    /**
     * 根据主键删除词缀。
     *
     * @param id 词缀 ID
     */
    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM mods WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete mod by id: " + id, e);
        }
    }

    /**
     * 统计词缀记录总数。
     *
     * @return mods 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM mods";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count mods", e);
        }
        return 0;
    }

    /** 设置 PreparedStatement 参数（绑定 Mod 字段） */
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
        ps.setString(12, mod.getTierText());
        ps.setString(13, mod.getGrantedBuffId());
        ps.setInt(14, mod.getGrantedBuffValue());
        ps.setString(15, mod.getGrantedSkill());
        ps.setString(16, mod.getVersion());
    }

    /** 从 ResultSet 映射一行到 Mod 实体 */
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
        mod.setTierText(rs.getString("tier_text"));
        mod.setGrantedBuffId(rs.getString("granted_buff_id"));
        mod.setGrantedBuffValue(rs.getInt("granted_buff_value"));
        mod.setGrantedSkill(rs.getString("granted_skill"));
        mod.setVersion(rs.getString("version"));
        return mod;
    }
}
