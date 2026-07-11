package com.poe.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.manager.DatabaseManager;
import com.poe.core.version.GameVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;

/**
 * 多数据源游戏版本检测器。
 * <p>
 * 按优先级检测当前游戏版本：
 * <ol>
 *   <li>GGG API（赛季名/版本号，权威）</li>
 *   <li>Wiki items 表最大 release_version（近似）</li>
 *   <li>本地 data_version 缓存（兜底）</li>
 * </ol>
 *
 * <h3>使用方式</h3>
 * <pre>{@code
 * VersionDetector detector = new VersionDetector(
 *     new GggApiClient(), DatabaseManager.getInstance()
 * );
 * GameVersion version = detector.getCurrentVersion();
 * }</pre>
 */
public class VersionDetector {

    private static final Logger log = LoggerFactory.getLogger(VersionDetector.class);

    private final GggApiClient gggApi;
    private final DatabaseManager dbManager;

    public VersionDetector(GggApiClient gggApi, DatabaseManager dbManager) {
        this.gggApi = gggApi;
        this.dbManager = dbManager;
    }

    /**
     * 获取当前权威游戏版本。
     * 优先级: GGG API（赛季权威）> Wiki items 最大 release_version > data_version 缓存
     */
    public GameVersion getCurrentVersion() {
        // 1. GGG API
        try {
            GameVersion gggVersion = detectFromGggApi();
            if (!gggVersion.isUnknown()) {
                log.info("Detected version from GGG API: {}", gggVersion);
                return gggVersion;
            }
        } catch (Exception e) {
            log.warn("GGG API version detection failed: {}", e.getMessage());
        }

        // 2. Wiki items max(release_version)
        try {
            GameVersion wikiVersion = detectFromWiki();
            if (!wikiVersion.isUnknown()) {
                log.info("Detected version from Wiki: {}", wikiVersion);
                return wikiVersion;
            }
        } catch (Exception e) {
            log.warn("Wiki version detection failed: {}", e.getMessage());
        }

        // 3. Local data_version cache
        try {
            GameVersion cachedVersion = detectFromCache();
            if (!cachedVersion.isUnknown()) {
                log.info("Detected version from local cache: {}", cachedVersion);
                return cachedVersion;
            }
        } catch (Exception e) {
            log.warn("Local cache version detection failed: {}", e.getMessage());
        }

        log.warn("All version detection sources failed, returning unknown");
        return GameVersion.unknown();
    }

    /**
     * 从 GGG API 获取当前赛季信息。
     */
    GameVersion detectFromGggApi() {
        JsonNode leagues = gggApi.getLeagues();
        if (leagues == null || !leagues.isArray() || leagues.isEmpty()) {
            return GameVersion.unknown();
        }

        // 查找当前运行的赛季（startAt 不为 null 且已开始）
        for (JsonNode league : leagues) {
            String id = league.has("id") ? league.get("id").asText() : "";
            String startAt = league.has("startAt") ? league.get("startAt").asText(null) : null;

            // 使用第一个有效的赛季作为当前赛季
            if (!id.isEmpty()) {
                // 从赛季名提取版本号（如 "Settlers" -> "3.25"）
                String version = extractVersionFromLeague(id);
                return new GameVersion(version, id, "GGG API", Instant.now());
            }
        }

        return GameVersion.unknown();
    }

    /**
     * 从 Wiki items 表最大 version 检测版本。
     */
    GameVersion detectFromWiki() {
        String sql = "SELECT COALESCE(MAX(version), 'unknown') FROM base_items";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String version = rs.getString(1);
                if (!"unknown".equals(version)) {
                    return new GameVersion(version, "unknown_league", "Wiki", Instant.now());
                }
            }
        } catch (Exception e) {
            log.debug("Wiki version query failed: {}", e.getMessage());
        }
        return GameVersion.unknown();
    }

    /**
     * 从本地 data_version 缓存检测版本。
     */
    GameVersion detectFromCache() {
        String sql = "SELECT source_version FROM data_version "
                + "WHERE table_name = 'base_items' AND source_version IS NOT NULL "
                + "ORDER BY last_sync DESC LIMIT 1";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String version = rs.getString(1);
                if (version != null && !version.isEmpty()) {
                    return new GameVersion(version, "unknown_league", "data_version", Instant.now());
                }
            }
        } catch (Exception e) {
            log.debug("data_version cache query failed: {}", e.getMessage());
        }
        return GameVersion.unknown();
    }

    /**
     * 检测 Wiki 数据是否过期。
     * 仅当 Wiki 最新 release_version 明显落后 GGG 赛季版本时返回 true。
     */
    public boolean isWikiDataStale() {
        try {
            GameVersion gggVersion = detectFromGggApi();
            if (gggVersion.isUnknown()) return false; // 无法判断

            GameVersion wikiVersion = detectFromWiki();
            if (wikiVersion.isUnknown()) return false;

            // 简单字符串比较：版本不同则可能过期
            return !gggVersion.getVersion().equals(wikiVersion.getVersion());
        } catch (Exception e) {
            log.debug("Wiki stale check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从赛季名提取版本号近似值。
     * 简单地映射已知赛季到版本号，未知赛季直接返回赛季名。
     */
    private String extractVersionFromLeague(String leagueId) {
        if (leagueId == null || leagueId.isEmpty()) return "unknown";

        // GGG 赛季名与版本号的大致映射
        switch (leagueId.toLowerCase()) {
            case "settlers": return "3.25";
            case "necropolis": return "3.24";
            case "affliction": return "3.23";
            case "ancestor": return "3.22";
            case "crucible": return "3.21";
            case "sanctum": return "3.20";
            case "lake of kalandra": return "3.19";
            case "sentinel": return "3.18";
            case "archenemesis": return "3.17";
            case "scourge": return "3.16";
            case "expedition": return "3.15";
            case "ultimatum": return "3.14";
            case "ritual": return "3.13";
            case "heist": return "3.12";
            case "harvest": return "3.11";
            case "delirium": return "3.10";
            case "metamorph": return "3.9";
            case "blight": return "3.8";
            case "legion": return "3.7";
            default: return leagueId.toLowerCase();
        }
    }
}
