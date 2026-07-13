package com.poe.core.db;

import com.poe.core.event.AppEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 数据库初始化器：首次启动时从 classpath 复制内置种子数据库。
 *
 * <h3>补偿策略</h3>
 * 使用 {@code .seed_ok} marker 文件区分三种状态：
 * <ol>
 *   <li>DB 存在 + marker 存在 → 正常，跳过</li>
 *   <li>DB 存在 + marker 不存在 → 上次复制失败，本次重试</li>
 *   <li>DB 不存在 → 首次启动，执行 seed.db 复制</li>
 * </ol>
 * 复制失败时不创建占位文件，由后续 {@code DatabaseManager} 自动创建
 * 空库并运行迁移（schema 完整，数据为空）。下次启动 marker 仍缺失，
 * 自动重试 seed.db 复制。
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
    private final Path seedMarker;
    private final Class<?> resourceClass;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * 使用默认 resource class 创建（测试用，seed.db 不在 classpath 中）。
     */
    public DatabaseInitializer(Path appDir) {
        this(appDir, DatabaseInitializer.class);
    }

    /**
     * 使用指定 class 的 classloader 加载 seed.db 资源。
     *
     * @param appDir        应用数据目录（如 {@code ~/.poe-tool}）
     * @param resourceClass seed.db 所在模块的类（如 {@code PoeApplication.class}）
     */
    public DatabaseInitializer(Path appDir, Class<?> resourceClass) {
        Path dataDir = appDir.resolve("data");
        this.targetDb = dataDir.resolve("poe.db");
        this.seedMarker = dataDir.resolve(".seed_ok");
        this.resourceClass = resourceClass;
        log.debug("DatabaseInitializer created: target={}, marker={}, resourceClass={}",
                targetDb, seedMarker, resourceClass.getName());
    }

    /**
     * 确保数据库就绪。首次调用时复制 seed.db。
     *
     * @return 目标数据库路径
     */
    public Path initialize() {
        if (!initialized.compareAndSet(false, true)) {
            log.debug("DatabaseInitializer already executed, skipping");
            return targetDb;
        }

        log.info("Initializing database: target={}, marker={}, resource={}",
                targetDb, seedMarker, SEED_RESOURCE);

        boolean dbExists = Files.exists(targetDb);
        boolean markerExists = Files.exists(seedMarker);

        // 状态 1：DB 存在且 marker 存在 → 正常，跳过
        if (dbExists && markerExists) {
            log.info("Database already exists and is seeded: {} ({} bytes)",
                    targetDb, fileSize(targetDb));
            AppEventBus.postAsync(new DatabaseReadyEvent(true, null));
            return targetDb;
        }

        // 状态 2：DB 存在但 marker 缺失 → 上次复制失败，重试
        if (dbExists) {
            log.warn("Database exists but seed marker is missing: db={}, size={} bytes. "
                    + "Previous seed copy may have failed, retrying...",
                    targetDb, fileSize(targetDb));
        } else {
            log.info("Database not found at {}, first launch — will copy from seed.db",
                    targetDb);
        }

        // 状态 2/3：DB 不存在或 marker 缺失 → 尝试（重新）复制 seed.db
        try {
            Files.createDirectories(targetDb.getParent());
            log.debug("Data directory created: {}", targetDb.getParent());

            long startTime = System.currentTimeMillis();
            copySeedDb();
            long elapsed = System.currentTimeMillis() - startTime;

            Files.createFile(seedMarker);
            long dbSize = Files.size(targetDb);
            log.info("Seed database copied successfully: {} ({} bytes) in {} ms",
                    targetDb, dbSize, elapsed);
            AppEventBus.postAsync(new DatabaseReadyEvent(true, "seed"));
        } catch (Exception e) {
            log.error("Failed to copy seed database from {} using classloader of {}. "
                    + "Will retry next startup.",
                    SEED_RESOURCE, resourceClass.getName(), e);
            AppEventBus.postAsync(new DatabaseReadyEvent(false, e.getMessage()));
            // 不创建占位文件：DatabaseManager 会自动创建有效空库 + 运行迁移，
            // 下次启动 marker 仍缺失，自动重试 seed.db 复制
        }

        return targetDb;
    }

    private void copySeedDb() throws IOException {
        log.debug("Looking up seed resource: {} from classloader of {}",
                SEED_RESOURCE, resourceClass.getName());

        try (InputStream in = resourceClass.getResourceAsStream(SEED_RESOURCE)) {
            if (in == null) {
                throw new IOException("Seed database resource not found: " + SEED_RESOURCE
                        + " (classloader of " + resourceClass.getName() + ")");
            }
            log.debug("Seed resource found, copying to {} ...", targetDb);
            Files.copy(in, targetDb, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static long fileSize(Path path) {
        try {
            return Files.exists(path) ? Files.size(path) : -1;
        } catch (IOException e) {
            return -2;
        }
    }

    /** 标记为已初始化（用于不依赖 seed.db 的测试） */
    void markInitialized() {
        initialized.set(true);
    }
}
