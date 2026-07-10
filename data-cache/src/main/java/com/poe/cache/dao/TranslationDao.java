package com.poe.cache.dao;

import java.sql.*;
import java.util.*;

/**
 * translations 表数据访问对象。
 * <p>
 * 表使用 (source, domain) 联合主键，同一源文本在不同领域可存在独立翻译。
 * 保存操作使用 {@code INSERT OR REPLACE}，自动处理新增/覆盖。
 */
public class TranslationDao {

    private final Connection connection;

    public TranslationDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * 查询单个翻译。
     *
     * @param source 源文本
     * @param domain 领域（item/skill/passive/mod/map）
     * @return 翻译后的文本，未找到时返回 empty
     */
    public Optional<String> translate(String source, String domain) {
        String sql = "SELECT target FROM translations WHERE source = ? AND domain = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, source);
            ps.setString(2, domain);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getString("target"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to translate: " + source + " domain=" + domain, e);
        }
        return Optional.empty();
    }

    /**
     * 批量查询翻译。
     * <p>
     * 根据输入列表动态构造 {@code WHERE source IN (?, ?, ...)} 语句实现单次查询。
     *
     * @param sources 源文本列表
     * @param domain  领域
     * @return 源文本 -> 翻译文本 的映射，未找到的文本不会出现在返回 Map 中
     */
    public Map<String, String> batchTranslate(List<String> sources, String domain) {
        if (sources == null || sources.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        String placeholders = String.join(",", Collections.nCopies(sources.size(), "?"));
        String sql = "SELECT source, target FROM translations WHERE source IN (" + placeholders + ") AND domain = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < sources.size(); i++) {
                ps.setString(i + 1, sources.get(i));
            }
            ps.setString(sources.size() + 1, domain);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("source"), rs.getString("target"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to batch translate " + sources.size() + " sources", e);
        }
        return result;
    }

    /**
     * 保存单条翻译（INSERT OR REPLACE）。
     */
    public void saveTranslation(String source, String target, String domain) {
        String sql = "INSERT OR REPLACE INTO translations (source, target, domain) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, source);
            ps.setString(2, target);
            ps.setString(3, domain);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save translation: " + source, e);
        }
    }

    /**
     * 批量保存翻译。
     */
    public void batchSave(Map<String, String> translations, String domain) {
        if (translations == null || translations.isEmpty()) {
            return;
        }
        String sql = "INSERT OR REPLACE INTO translations (source, target, domain) VALUES (?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Map.Entry<String, String> entry : translations.entrySet()) {
                    ps.setString(1, entry.getKey());
                    ps.setString(2, entry.getValue());
                    ps.setString(3, domain);
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
            throw new RuntimeException("Failed to batch save translations", e);
        }
    }
}
