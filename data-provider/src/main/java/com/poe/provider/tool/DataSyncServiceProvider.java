package com.poe.provider.tool;

import com.poe.cache.manager.DatabaseManager;
import com.poe.core.db.StartupSyncRunner;
import com.poe.provider.WikiApiClient;
import com.poe.provider.sync.DataSyncService;

import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 为 app-core 层的 {@link StartupSyncRunner} 提供数据同步能力，
 * 封装 data-provider 模块中的 WikiApiClient 和 DataSyncService。
 */
public class DataSyncServiceProvider implements StartupSyncRunner.SyncServiceProvider {

    private static final Logger log = LoggerFactory.getLogger(DataSyncServiceProvider.class);

    private final WikiApiClient wikiClient;
    private final DataSyncService syncService;

    public DataSyncServiceProvider(DatabaseManager dbManager) {
        this.wikiClient = new WikiApiClient();
        this.syncService = new DataSyncService(wikiClient, dbManager);
    }

    @Override
    public boolean hasUpdates() {
        return syncService.hasUpdates();
    }

    @Override
    public Map<String, ?> syncAll() {
        try {
            return syncService.syncAll();
        } catch (SQLException e) {
            log.error("Data sync failed", e);
            return Map.of();
        }
    }
}
