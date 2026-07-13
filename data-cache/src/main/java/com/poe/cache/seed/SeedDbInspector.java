package com.poe.cache.seed;

import java.sql.*;
import java.util.*;

/**
 * seed.db 完整性检测工具，提供表行数统计、空表发现、data_version 查询等功能。
 *
 * <p>可用于：
 * <ul>
 *   <li>自动化测试中的 seed.db 健康检查</li>
 *   <li>UI 中显示数据库统计信息</li>
 *   <li>SeedDbBuilder 构建后的验证</li>
 * </ul>
 *
 * <p>线程安全：实例本身无状态，通过传入的 {@link Connection} 操作。
 */
public final class SeedDbInspector {

    private SeedDbInspector() {
        // utility class
    }

    /**
     * 一组通常不会为空的"核心"表名。
     * <p>注意：{@code base_items} / {@code items_fts} 未列入，因为 PoE Wiki 的
     * {@code items} Cargo 表存在服务端 {@code MWException} 崩溃问题，暂时无法同步。
     */
    public static final Set<String> DEFAULT_CORE_TABLES = Set.of(
        "skill_gems", "passive_skills", "mods",
        "translations", "data_version"
    );

    // ---- 表发现 ----

    /** 获取所有用户表名（排除 sqlite_ 内部表和 FTS 影子表） */
    public static List<String> listUserTables(Connection conn) throws SQLException {
        List<String> tables = new ArrayList<>();
        String sql = "SELECT name FROM sqlite_master WHERE type='table' "
            + "AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'items_fts_%' "
            + "ORDER BY name";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tables.add(rs.getString("name"));
            }
        }
        return tables;
    }

    // ---- 行数统计 ----

    /** 返回指定表的行数，若表不存在或查询失败返回 -1 */
    public static int countRows(Connection conn, String table) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT COUNT(*) FROM \"" + table + "\"")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return -1;
        }
    }

    /** 返回所有用户表的行数总和 */
    public static int totalRows(Connection conn) throws SQLException {
        int total = 0;
        for (String table : listUserTables(conn)) {
            int rows = countRows(conn, table);
            if (rows > 0) {
                total += rows;
            }
        }
        return total;
    }

    /** 按行数降序返回 (表名, 行数) 列表 */
    public static List<Map.Entry<String, Integer>> sortedByRows(Connection conn)
            throws SQLException {
        List<Map.Entry<String, Integer>> result = new ArrayList<>();
        for (String table : listUserTables(conn)) {
            result.add(new AbstractMap.SimpleEntry<>(table, countRows(conn, table)));
        }
        result.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        return result;
    }

    // ---- 空表检测 ----

    /** 查找所有行数为 0 的用户表 */
    public static List<String> findEmptyTables(Connection conn) throws SQLException {
        List<String> empty = new ArrayList<>();
        for (String table : listUserTables(conn)) {
            if (countRows(conn, table) == 0) {
                empty.add(table);
            }
        }
        return empty;
    }

    /** 查找指定核心表中行数为 0 的表 */
    public static List<String> findEmptyCoreTables(Connection conn,
                                                   Set<String> coreTables)
            throws SQLException {
        List<String> problems = new ArrayList<>();
        for (String table : coreTables) {
            if (listUserTables(conn).contains(table) && countRows(conn, table) == 0) {
                problems.add(table);
            }
        }
        return problems;
    }

    // ---- data_version 查询 ----

    /** 获取某表的 data_version 记录，无记录返回 null */
    public static String getDataVersion(Connection conn, String table) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT record_count, synced_at FROM data_version WHERE table_name = ?")) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("record_count") + " rows @ "
                        + rs.getString("synced_at");
                }
            }
        } catch (SQLException e) {
            // data_version 表可能不存在
        }
        return null;
    }

    // ---- 完整性报告 ----

    /** 生成多行可读的完整性报告 */
    public static String buildReport(Connection conn) throws SQLException {
        StringBuilder sb = new StringBuilder();
        int tableCount = listUserTables(conn).size();
        int total = totalRows(conn);
        List<String> empty = findEmptyTables(conn);

        sb.append("=== seed.db 完整性报告 ===\n");
        sb.append(String.format("表总数: %d, 总行数: %,d\n", tableCount, total));

        if (empty.isEmpty()) {
            sb.append("所有表均包含数据。\n");
        } else {
            sb.append(String.format("空表 (%d):\n", empty.size()));
            for (String t : empty) {
                String dv = getDataVersion(conn, t);
                sb.append(String.format("  %s  data_version=%s\n", t,
                    dv != null ? dv : "(无)"));
            }
        }
        return sb.toString();
    }

    /**
     * 快速健康检查：返回 true 表示所有核心表都有数据。
     * 适合在测试 teardown 或 CI 中调用。
     */
    public static boolean isHealthy(Connection conn, Set<String> coreTables)
            throws SQLException {
        return findEmptyCoreTables(conn, coreTables).isEmpty();
    }

    /** 使用默认核心表的健康检查 */
    public static boolean isHealthy(Connection conn) throws SQLException {
        return isHealthy(conn, DEFAULT_CORE_TABLES);
    }
}
