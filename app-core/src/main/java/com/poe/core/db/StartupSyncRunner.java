package com.poe.core.db;

import com.poe.core.event.AppEventBus;
import com.poe.core.event.DataSyncCompleteEvent;
import com.poe.core.event.DataSyncProgressEvent;
import com.poe.core.event.DataSyncStartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 启动时增量同步器：后台检查远程更新并按需同步。
 *
 * <p>在 DatabaseInitializer 完成后异步运行：
 * <ol>
 *   <li>检测网络可达性</li>
 *   <li>调用 {@code DataSyncService.hasUpdates()} 做版本对比</li>
 *   <li>有更新则触发 {@code syncAll()}</li>
 *   <li>通过 {@link AppEventBus} 发布进度/完成事件</li>
 * </ol>
 *
 * <p>需要网络和远程数据源的调用由 {@link SyncServiceProvider} 提供，
 * 允许 app-core 层不直接依赖 data-provider 模块。
 */
public class StartupSyncRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupSyncRunner.class);

    private final SyncServiceProvider syncProvider;

    public StartupSyncRunner(SyncServiceProvider syncProvider) {
        this.syncProvider = syncProvider;
    }

    /**
     * 异步执行启动同步。
     *
     * @return 同步结果的 CompletableFuture
     */
    public CompletableFuture<Map<String, ?>> runAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return doSync();
            } catch (Exception e) {
                log.warn("Startup sync failed: {}", e.getMessage());
                AppEventBus.postAsync(new StartupSyncFailedEvent(e.getMessage()));
                return Map.of();
            }
        });
    }

    private Map<String, ?> doSync() {
        // 1. 网络检测
        if (!isNetworkReachable()) {
            log.info("Network unreachable, skipping startup sync");
            AppEventBus.postAsync(new StartupSyncSkippedEvent("network_unreachable"));
            return Map.of();
        }

        // 2. 检查更新
        if (!syncProvider.hasUpdates()) {
            log.info("No remote updates detected");
            AppEventBus.postAsync(new StartupSyncSkippedEvent("up_to_date"));
            return Map.of();
        }

        // 3. 执行全量同步
        log.info("Remote updates detected, starting sync...");
        AppEventBus.postSync(new DataSyncStartEvent("startup", -1));

        Instant start = Instant.now();
        Map<String, ?> results = syncProvider.syncAll();
        Duration elapsed = Duration.between(start, Instant.now());

        int tableCount = results.size();

        AppEventBus.postSync(new DataSyncCompleteEvent("startup", tableCount, elapsed.toMillis()));
        log.info("Startup sync complete: {} tables in {} ms", tableCount, elapsed.toMillis());

        return results;
    }

    private boolean isNetworkReachable() {
        try {
            HttpURLConnection conn = (HttpURLConnection)
                    new URL("https://www.pathofexile.com").openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("HEAD");
            int code = conn.getResponseCode();
            conn.disconnect();
            return code > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ========== 内部事件 ==========

    /** 同步因离线/无需同步被跳过时发布。 */
    public static class StartupSyncSkippedEvent {
        private final String reason;

        public StartupSyncSkippedEvent(String reason) {
            this.reason = reason;
        }

        public String getReason() {
            return reason;
        }
    }

    /** 同步失败时发布。 */
    public static class StartupSyncFailedEvent {
        private final String errorMessage;

        public StartupSyncFailedEvent(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * 同步服务提供者接口（解耦 app-core 与 data-provider）。
     * <p>由 data-provider 模块实现，在启动时注入。
     */
    public interface SyncServiceProvider {
        boolean hasUpdates();
        Map<String, ?> syncAll();
    }
}
