package com.poe.cache.dao;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO 基类，封装所有表的通用 CRUD 操作。
 *
 * <p>子类只需实现 5 个抽象方法即可获得完整的单表增删改查能力：
 * <ul>
 *   <li>{@link #tableName()} — 表名</li>
 *   <li>{@link #insertColumns()} — INSERT 的非主键列名列表</li>
 *   <li>{@link #bindInsertParams(PreparedStatement, Object)} — 绑定 INSERT 参数</li>
 *   <li>{@link #setEntityId(Object, int)} — 回填自增主键</li>
 *   <li>{@link #mapRow(ResultSet)} — ResultSet → 实体映射</li>
 * </ul>
 *
 * @param <T> 实体类型
 * @author yuziyang
 * @since 2026/7/15
 */
public abstract class BaseDao<T> {

    protected final DataSource dataSource;

    protected BaseDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ─── 子类必须实现的抽象方法 ───

    /** 表名 */
    protected abstract String tableName();

    /** 非主键列名，逗号分隔，用于 INSERT */
    protected abstract String insertColumns();

    /** 为 INSERT 语句绑定参数，按 insertColumns 顺序 */
    protected abstract void bindInsertParams(PreparedStatement ps, T entity) throws SQLException;

    /** 将数据库自增主键回填到实体 */
    protected abstract void setEntityId(T entity, int id);

    /** ResultSet 当前行 → 实体对象 */
    protected abstract T mapRow(ResultSet rs) throws SQLException;

    // ─── 通用 CRUD ───

    /**
     * 按主键查询。
     *
     * @param id 主键值
     * @return 实体（可能为空）
     */
    public Optional<T> findById(int id) throws SQLException {
        String sql = "SELECT * FROM " + tableName() + " WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 查询全表。
     *
     * @return 所有记录，按 id 升序
     */
    public List<T> findAll() throws SQLException {
        String sql = "SELECT * FROM " + tableName() + " ORDER BY id";
        List<T> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    /**
     * 按主键删除。
     *
     * @param id 主键值
     */
    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM " + tableName() + " WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * 统计行数。
     *
     * @return 总记录数
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * 插入一条记录，自动回填自增主键。
     *
     * @param entity 实体
     */
    public void insert(T entity) throws SQLException {
        String columns = insertColumns();
        String[] colArr = columns.split(",");
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < colArr.length; i++) {
            if (i > 0) placeholders.append(", ");
            placeholders.append("?");
        }
        String sql = "INSERT INTO " + tableName() + " (" + columns + ") VALUES (" + placeholders + ")";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindInsertParams(ps, entity);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    setEntityId(entity, rs.getInt(1));
                }
            }
        }
    }

    /**
     * 批量插入。
     *
     * @param entities 实体列表
     */
    public void batchInsert(List<T> entities) throws SQLException {
        String columns = insertColumns();
        String[] colArr = columns.split(",");
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < colArr.length; i++) {
            if (i > 0) placeholders.append(", ");
            placeholders.append("?");
        }
        String sql = "INSERT INTO " + tableName() + " (" + columns + ") VALUES (" + placeholders + ")";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (T entity : entities) {
                bindInsertParams(ps, entity);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
