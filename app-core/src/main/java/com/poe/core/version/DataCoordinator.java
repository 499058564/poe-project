package com.poe.core.version;

import com.poe.cache.manager.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 多数据源数据协调策略。
 * <p>
 * 根据表名和已有数据源状态，决定使用哪个来源同步数据。
 * 策略规则（按优先级）：
 * <ol>
 *   <li>价格类数据 → poe.ninja</li>
 *   <li>Wiki 覆盖的表 → Wiki Cargo</li>
 *   <li>POB 补充数据 → 仅在 Wiki 数据更新后做完整性验证</li>
 *   <li>否则 → 跳过</li>
 * </ol>
 */
public class DataCoordinator {

    private static final Logger log = LoggerFactory.getLogger(DataCoordinator.class);

    private static final Set<String> WIKI_TABLES = new HashSet<>(Arrays.asList(
        "base_items", "skill_gems", "passive_skills", "mods",
        "weapons", "armours", "flasks", "jewels",
        "monsters", "world_areas", "quests", "items"
    ));

    private static final Set<String> NINJA_TABLES = new HashSet<>(Arrays.asList(
        "currency_prices", "item_prices"
    ));

    private static final Map<String, String> TABLE_TO_SOURCE = new LinkedHashMap<>();

    static {
        WIKI_TABLES.forEach(t -> TABLE_TO_SOURCE.put(t, "wiki"));
        NINJA_TABLES.forEach(t -> TABLE_TO_SOURCE.put(t, "poe.ninja"));
    }

    private final DatabaseManager dbManager;

    public DataCoordinator(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * 根据表名决定同步策略。
     *
     * @return 同步决策结果
     */
    public SyncDecision decide(String tableName, GameVersion currentVersion) {
        String source = resolveSource(tableName);
        boolean needsSync = shouldSync(tableName, source, currentVersion);
        return new SyncDecision(tableName, source, needsSync, currentVersion);
    }

    /**
     * 判断某表是否应由 Wiki 同步。
     */
    public boolean isWikiTable(String tableName) {
        return WIKI_TABLES.contains(tableName);
    }

    /**
     * 判断某表是否是经济类数据。
     */
    public boolean isNinjaTable(String tableName) {
        return NINJA_TABLES.contains(tableName);
    }

    /**
     * 解析表的数据源。
     */
    private String resolveSource(String tableName) {
        if (NINJA_TABLES.contains(tableName)) return "poe.ninja";
        if (WIKI_TABLES.contains(tableName)) return "wiki";
        return "unknown";
    }

    /**
     * 判断某表是否需要同步。
     * 伪实现：查询 data_version 表，若本地版本落后当前游戏版本则需同步。
     */
    boolean shouldSync(String tableName, String source, GameVersion currentVersion) {
        if (currentVersion.isUnknown()) return true;

        String sql = "SELECT source_version FROM data_version WHERE table_name = ? AND source = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, source);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String cached = rs.getString(1);
                    if (cached != null && cached.equals(currentVersion.getVersion())) {
                        return false; // 版本一致，无需同步
                    }
                }
            }
        } catch (SQLException e) {
            log.warn("Failed to check sync status for {}: {}", tableName, e.getMessage());
            return true; // 出错时默认同步
        }
        return true; // 第一次或无记录时同步
    }

    /**
     * 同步完成后更新 data_version 表。
     */
    public void recordSync(String tableName, String source, String sourceVersion, int recordCount) {
        String updateSql = "UPDATE data_version SET last_sync = ?, record_count = ?, "
                + "source = ?, source_version = ?, wiki_version = NULL WHERE table_name = ?";
        String insertSql = "INSERT INTO data_version "
                + "(table_name, last_sync, record_count, source, source_version) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, Instant.now().toString());
                ps.setInt(2, recordCount);
                ps.setString(3, source);
                ps.setString(4, sourceVersion);
                ps.setString(5, tableName);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    try (PreparedStatement ips = conn.prepareStatement(insertSql)) {
                        ips.setString(1, tableName);
                        ips.setString(2, Instant.now().toString());
                        ips.setInt(3, recordCount);
                        ips.setString(4, source);
                        ips.setString(5, sourceVersion);
                        ips.executeUpdate();
                    }
                }
            }
            log.info("Recorded sync: table={} source={} version={} count={}",
                tableName, source, sourceVersion, recordCount);
        } catch (SQLException e) {
            log.error("Failed to record sync for {}: {}", tableName, e.getMessage());
        }
    }

    /**
     * 同步决策值对象。
     */
    public static class SyncDecision {
        private final String tableName;
        private final String source;
        private final boolean needsSync;
        private final GameVersion gameVersion;

        SyncDecision(String tableName, String source, boolean needsSync, GameVersion gameVersion) {
            this.tableName = tableName;
            this.source = source;
            this.needsSync = needsSync;
            this.gameVersion = gameVersion;
        }

        public String getTableName() { return tableName; }
        public String getSource() { return source; }
        public boolean needsSync() { return needsSync; }
        public GameVersion getGameVersion() { return gameVersion; }

        @Override
        public String toString() {
            return "SyncDecision{" + tableName + " source=" + source
                + " needsSync=" + needsSync + " version=" + gameVersion.getVersion() + "}";
        }
    }
}
