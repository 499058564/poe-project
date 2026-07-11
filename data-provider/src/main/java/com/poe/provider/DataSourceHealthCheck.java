package com.poe.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.manager.DatabaseManager;
import com.poe.core.version.DataSourceHealth;
import com.poe.core.version.DataSourceHealth.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 多数据源启动健康检查。
 * <p>
 * 在应用启动时对所有配置的数据源执行连通性检查：
 * <ul>
 *   <li>GGG API — ping /public-stash-tabs</li>
 *   <li>poe.ninja — ping CurrencyOverview</li>
 *   <li>Wiki Cargo（本地缓存）— 检查 base_items 表是否有数据</li>
 *   <li>POB — 检查 pob-runtime 子模块是否已初始化</li>
 * </ul>
 *
 * <h3>使用方式</h3>
 * <pre>{@code
 * DataSourceHealthCheck check = new DataSourceHealthCheck(
 *     gggApiClient, ninjaClient, pobExtractor, dbManager
 * );
 * List<DataSourceHealth> report = check.checkAll();
 * report.forEach(h -> logger.info("{}: {}", h.getSourceName(), h.getStatus()));
 * }</pre>
 */
public class DataSourceHealthCheck {

    private static final Logger log = LoggerFactory.getLogger(DataSourceHealthCheck.class);

    private final GggApiClient gggApi;
    private final PoeNinjaClient ninjaClient;
    private final PobDataExtractor pobExtractor;
    private final DatabaseManager dbManager;

    public DataSourceHealthCheck(GggApiClient gggApi, PoeNinjaClient ninjaClient,
                                 PobDataExtractor pobExtractor, DatabaseManager dbManager) {
        this.gggApi = gggApi;
        this.ninjaClient = ninjaClient;
        this.pobExtractor = pobExtractor;
        this.dbManager = dbManager;
    }

    /**
     * 检查所有数据源并返回健康报告。
     */
    public List<DataSourceHealth> checkAll() {
        List<DataSourceHealth> results = new ArrayList<>();
        results.add(checkGggApi());
        results.add(checkPoeNinja());
        results.add(checkWikiCache());
        results.add(checkPob());
        return Collections.unmodifiableList(results);
    }

    /**
     * 检查 GGG API 可用性。
     */
    DataSourceHealth checkGggApi() {
        long start = System.currentTimeMillis();
        try {
            JsonNode leagues = gggApi.getLeagues();
            if (leagues != null && leagues.isArray() && !leagues.isEmpty()) {
                long elapsed = System.currentTimeMillis() - start;
                return new DataSourceHealth("GGG API", Status.HEALTHY,
                    elapsed, "Responded with " + leagues.size() + " leagues");
            }
            if (leagues != null && leagues.isArray() && leagues.isEmpty()) {
                return new DataSourceHealth("GGG API", Status.DEGRADED,
                    System.currentTimeMillis() - start, "Empty league list returned");
            }
            return new DataSourceHealth("GGG API", Status.DEGRADED,
                System.currentTimeMillis() - start, "Empty or invalid response");
        } catch (Exception e) {
            return new DataSourceHealth("GGG API", Status.UNAVAILABLE,
                System.currentTimeMillis() - start, e.getMessage());
        }
    }

    /**
     * 检查 poe.ninja API 可用性。
     */
    DataSourceHealth checkPoeNinja() {
        long start = System.currentTimeMillis();
        try {
            JsonNode overview = ninjaClient.getCurrencyOverview("Standard", "Currency");
            if (overview != null && overview.has("lines")) {
                long elapsed = System.currentTimeMillis() - start;
                int count = overview.get("lines").size();
                return new DataSourceHealth("poe.ninja", Status.HEALTHY,
                    elapsed, "Responded with " + count + " currency entries");
            }
            return new DataSourceHealth("poe.ninja", Status.DEGRADED,
                System.currentTimeMillis() - start, "Response missing lines array");
        } catch (Exception e) {
            return new DataSourceHealth("poe.ninja", Status.UNAVAILABLE,
                System.currentTimeMillis() - start, e.getMessage());
        }
    }

    /**
     * 检查 Wiki 本地缓存可用性（检查 base_items 表）。
     */
    DataSourceHealth checkWikiCache() {
        long start = System.currentTimeMillis();
        try {
            Connection conn = dbManager.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM base_items")) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    long elapsed = System.currentTimeMillis() - start;
                    if (count > 0) {
                        return new DataSourceHealth("Wiki Cache", Status.HEALTHY,
                            elapsed, count + " items cached");
                    } else {
                        return new DataSourceHealth("Wiki Cache", Status.DEGRADED,
                            elapsed, "Cache is empty — sync needed");
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Wiki cache check failed: {}", e.getMessage());
        }
        return new DataSourceHealth("Wiki Cache", Status.UNAVAILABLE,
            System.currentTimeMillis() - start, "Cannot query base_items table");
    }

    /**
     * 检查 POB 数据可用性。
     */
    DataSourceHealth checkPob() {
        long start = System.currentTimeMillis();
        try {
            boolean available = pobExtractor.isDataAvailable();
            long elapsed = System.currentTimeMillis() - start;
            if (available) {
                return new DataSourceHealth("POB", Status.HEALTHY,
                    elapsed, "POB data directory accessible at " + pobExtractor.getPobRoot());
            } else {
                return new DataSourceHealth("POB", Status.DEGRADED,
                    elapsed, "POB data directory not found — submodule may not be initialized");
            }
        } catch (Exception e) {
            return new DataSourceHealth("POB", Status.UNAVAILABLE,
                System.currentTimeMillis() - start, e.getMessage());
        }
    }
}
