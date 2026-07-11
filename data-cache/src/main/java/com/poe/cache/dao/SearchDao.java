package com.poe.cache.dao;

import com.poe.cache.model.ItemSummary;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FTS5 全文搜索 DAO。
 * <p>
 * 使用 items_fts 外部内容表（content='base_items'），通过 {@code JOIN} 连接主表
 * 获取完整字段。搜索语法支持 FTS5 标准查询表达式：
 * <ul>
 *   <li>普通词："Iron" → 精确匹配</li>
 *   <li>前缀匹配："Iron*" → 匹配所有以 Iron 开头的词</li>
 *   <li>多词查询："Iron Ring" → AND 逻辑</li>
 * </ul>
 * 注意：FTS5 默认不分割 CJK 字符，中文搜索需使用完整短语或前缀通配符。
 * <p>
 * 此类不实现 CrudRepository，因为它不操作单表而是跨表 FTS 查询。
 */
public class SearchDao {

    private final DataSource dataSource;

    public SearchDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * FTS5 全文搜索物品（无类别过滤）。
     *
     * @param keyword 搜索关键词
     * @param limit   返回条数上限
     * @param offset  偏移量
     * @return 搜索结果摘要列表
     */
    public List<ItemSummary> searchItems(String keyword, int limit, int offset) {
        return searchItems(keyword, null, limit, offset);
    }

    /**
     * FTS5 全文搜索物品（支持类别过滤）。
     *
     * @param keyword   搜索关键词
     * @param itemClass 物品类别过滤，null 表示不过滤
     * @param limit     返回条数上限
     * @param offset    偏移量
     * @return 搜索结果摘要列表
     */
    public List<ItemSummary> searchItems(String keyword, String itemClass, int limit, int offset) {
        String sql = "SELECT b.id, b.name, b.name_zh, b.class, b.drop_level, b.wiki_url, f.rank " +
            "FROM items_fts f JOIN base_items b ON f.rowid = b.id " +
            "WHERE items_fts MATCH ? " +
            (itemClass != null ? "AND b.class = ? " : "") +
            "ORDER BY rank LIMIT ? OFFSET ?";
        List<ItemSummary> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, keyword);
            if (itemClass != null) {
                ps.setString(idx++, itemClass);
            }
            ps.setInt(idx++, limit);
            ps.setInt(idx, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ItemSummary summary = new ItemSummary(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("name_zh"),
                        rs.getString("class"),
                        rs.getInt("drop_level"),
                        rs.getString("wiki_url"),
                        rs.getDouble("rank")
                    );
                    results.add(summary);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search items with keyword: " + keyword, e);
        }
        return results;
    }

    /**
     * FTS5 搜索结果总数（无类别过滤）。
     *
     * @param keyword 搜索关键词
     * @return 匹配记录数
     */
    public int searchItemCount(String keyword) {
        return searchItemCount(keyword, null);
    }

    /**
     * FTS5 搜索结果总数（支持类别过滤）。
     *
     * @param keyword   搜索关键词
     * @param itemClass 物品类别过滤，null 表示不过滤
     * @return 匹配记录数
     */
    public int searchItemCount(String keyword, String itemClass) {
        String sql = "SELECT COUNT(*) FROM items_fts f " +
            "JOIN base_items b ON f.rowid = b.id " +
            "WHERE items_fts MATCH ? " +
            (itemClass != null ? "AND b.class = ? " : "");
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, keyword);
            if (itemClass != null) {
                ps.setString(idx++, itemClass);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count search results for: " + keyword, e);
        }
        return 0;
    }
}
