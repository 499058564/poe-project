package com.poe.core.db;

import com.poe.core.event.AppEventBus;
import com.poe.core.event.DataSyncCompleteEvent;
import com.poe.core.event.DataSyncStartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 数据库初始化器：首次启动时从 classpath 复制内置种子数据库。
 *
 * <p>设计要点：
 * <ul>
 *   <li>检查 {@code ~/.poe-tool/data/poe.db} 是否存在</li>
 *   <li>不存在则从 classpath {@code /seed.db} 复制</li>
 *   <li>线程安全：仅执行一次</li>
 *   <li>复制后触发 {@link DatabaseReadyEvent}</li>
 * </ul>
 */
public class DatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    static final String SEED_RESOURCE = "/seed.db";

    private final Path targetDb;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public DatabaseInitializer(Path appDir) {
        this.targetDb = appDir.resolve("data").resolve("poe.db");
    }

    /**
     * 确保数据库就绪。首次调用时复制 seed.db。
     *
     * @return 目标数据库路径
     */
    public Path initialize() {
        if (!initialized.compareAndSet(false, true)) {
            return targetDb; // 已经初始化过
        }

        if (Files.exists(targetDb)) {
            log.info("Database already exists: {}", targetDb);
            AppEventBus.postAsync(new DatabaseReadyEvent(true, null));
            return targetDb;
        }

        // 首次启动 —— 复制种子数据库
        try {
            Files.createDirectories(targetDb.getParent());
            copySeedDb();
            log.info("Seed database initialized: {} ({} bytes)",
                    targetDb, Files.size(targetDb));
            AppEventBus.postAsync(new DatabaseReadyEvent(true, "seed"));
        } catch (Exception e) {
            log.error("Failed to initialize seed database", e);
            AppEventBus.postAsync(new DatabaseReadyEvent(false, e.getMessage()));
            // 创建空数据库以允许应用继续运行
            try {
                Files.createDirectories(targetDb.getParent());
                Files.createFile(targetDb);
            } catch (IOException ignored) {
            }
        }

        return targetDb;
    }

    private void copySeedDb() throws IOException {
        try (InputStream in = getClass().getResourceAsStream(SEED_RESOURCE)) {
            if (in == null) {
                throw new IOException("Seed database resource not found: " + SEED_RESOURCE);
            }
            Files.copy(in, targetDb, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** 标记为已初始化（用于不依赖 seed.db 的测试） */
    void markInitialized() {
        initialized.set(true);
    }
}
