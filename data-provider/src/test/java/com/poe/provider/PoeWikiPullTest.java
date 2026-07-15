package com.poe.provider;

import com.google.common.util.concurrent.RateLimiter;
import com.poe.cache.manager.DatabaseManager;
import com.poe.provider.client.WikiApiClient;
import com.poe.provider.sync.DataSyncService;
import com.poe.provider.sync.SyncResult;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.time.Duration;

/**
 * PoE Wiki 拉取测试 — 单表同步 items 到 seed.db。
 *
 * @author yuziyang
 * @since 2026/7/14
 **/
class PoeWikiPullTest {

    @Test
    void pullTest() throws Exception {
        // seed.db 路径
        String seedDbPath = System.getProperty("user.dir")
            + "/../app-ui/src/main/resources/seed.db";

        // 初始化数据库
        DatabaseManager dbm = new DatabaseManager(seedDbPath);
        dbm.forceInit();

        // 使用 HTTP/2 + 浏览器 UA + 0.5 req/s，减轻 Cloudflare 限流
        OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(15))
            .readTimeout(Duration.ofSeconds(60))
            .build();
        RateLimiter slowLimiter = RateLimiter.create(0.5);
        WikiApiClient wikiClient = new WikiApiClient(httpClient, slowLimiter,
            WikiApiClient.WIKI_API);

        DataSyncService syncService = new DataSyncService(wikiClient, dbm);
        syncService.setInterTableDelayMs(5000);
        syncService.setFailureDelayMs(60000);
        syncService.initTableFromWiki("items");

        // 在字段发现和数据同步之间等待，让 Cloudflare 冷却
        System.out.println("Waiting 30s before data sync...");
        Thread.sleep(30000);

        System.out.println("Starting items sync...");
        SyncResult result = syncService.syncTable("items");
        System.out.println("Items sync result: " + result.getSyncedRecords() + " records");

        // VACUUM 并关闭
        try (Connection conn = dbm.getConnection()) {
            conn.createStatement().execute("VACUUM");
        }
        dbm.shutdown();
        System.out.println("seed.db updated at: " + seedDbPath);
    }
}
