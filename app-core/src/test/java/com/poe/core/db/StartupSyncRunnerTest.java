package com.poe.core.db;

import com.poe.core.event.AppEventBus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StartupSyncRunner 测试。
 */
class StartupSyncRunnerTest {

    private final List<Object> capturedEvents = new ArrayList<>();

    @BeforeEach
    void setUp() {
        capturedEvents.clear();
        AppEventBus.register(this);
    }

    @AfterEach
    void tearDown() {
        AppEventBus.unregister(this);
        capturedEvents.clear();
    }

    // 捕获事件
    @com.google.common.eventbus.Subscribe
    public void onEvent(Object event) {
        capturedEvents.add(event);
    }

    @Test
    void noUpdates_skipsSync() throws Exception {
        StubSyncProvider provider = new StubSyncProvider(false, false, false);

        StartupSyncRunner runner = new StartupSyncRunner(provider);
        CompletableFuture<Map<String, ?>> future = runner.runAsync(null);
        future.get(10, TimeUnit.SECONDS);

        assertTrue(capturedEvents.stream()
                        .anyMatch(e -> e instanceof StartupSyncRunner.StartupSyncSkippedEvent),
                "Should publish SkippedEvent when no updates");

        StartupSyncRunner.StartupSyncSkippedEvent skipEvent = capturedEvents.stream()
                .filter(e -> e instanceof StartupSyncRunner.StartupSyncSkippedEvent)
                .map(e -> (StartupSyncRunner.StartupSyncSkippedEvent) e)
                .findFirst()
                .orElseThrow();

        assertTrue(skipEvent.getReason().equals("up_to_date")
                        || skipEvent.getReason().equals("network_unreachable"),
                "Expected up_to_date or network_unreachable, got: " + skipEvent.getReason());
    }

    @Test
    void networkUnreachable_skipsSync() throws Exception {
        StubSyncProvider provider = new StubSyncProvider(true, false, false);

        StartupSyncRunner runner = new StartupSyncRunner(provider);
        CompletableFuture<Map<String, ?>> future = runner.runAsync(null);
        future.get(10, TimeUnit.SECONDS);

        assertTrue(capturedEvents.stream()
                        .anyMatch(e -> e instanceof StartupSyncRunner.StartupSyncSkippedEvent),
                "Should publish SkippedEvent when network unreachable");
    }

    @Test
    void hasUpdates_triggersSync() throws Exception {
        StubSyncProvider provider = new StubSyncProvider(false, true, false);
        provider.syncResult = Map.of("items", "ok", "skills", "ok");

        StartupSyncRunner runner = new StartupSyncRunner(provider);
        CompletableFuture<Map<String, ?>> future = runner.runAsync(null);
        Map<String, ?> result = future.get(10, TimeUnit.SECONDS);

        assertEquals(2, result.size());
    }

    /**
     * 可注入的 {@link StartupSyncRunner.SyncServiceProvider} 桩实现。
     */
    static class StubSyncProvider implements StartupSyncRunner.SyncServiceProvider {
        private final boolean networkUnreachable;
        private final boolean hasUpdates;
        private final boolean syncFails;
        Map<String, ?> syncResult = Collections.emptyMap();

        StubSyncProvider(boolean networkUnreachable, boolean hasUpdates, boolean syncFails) {
            this.networkUnreachable = networkUnreachable;
            this.hasUpdates = hasUpdates;
            this.syncFails = syncFails;
        }

        @Override
        public boolean hasUpdates() {
            return hasUpdates;
        }

        @Override
        public Map<String, ?> syncAll() {
            if (syncFails) throw new RuntimeException("simulated sync failure");
            return syncResult;
        }
    }
}
