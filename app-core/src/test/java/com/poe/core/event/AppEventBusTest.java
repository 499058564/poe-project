package com.poe.core.event;

import com.google.common.eventbus.Subscribe;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AppEventBus 单元测试，覆盖同步/异步发布、注册/取消注册、核心事件类型。
 *
 * <p>使用匿名内部类作为订阅者，通过 {@link AtomicReference} 和
 * {@link CountDownLatch} 捕获并验证事件参数及线程行为。
 */
class AppEventBusTest {

    // ── 同步事件：验证在当前线程中同步调用订阅者 ──

    @Test
    void postSync_shouldExecuteOnCurrentThread() {
        // 用 AtomicReference 捕获订阅者执行时的线程名
        AtomicReference<String> threadName = new AtomicReference<>();
        Object subscriber = new Object() {
            @Subscribe
            public void on(NavigationEvent e) {
                threadName.set(Thread.currentThread().getName());
            }
        };
        AppEventBus.register(subscriber);

        AppEventBus.postSync(new NavigationEvent("home", "search"));
        // 验证：同步事件在当前线程（测试主线程）执行
        assertEquals(Thread.currentThread().getName(), threadName.get(),
            "同步事件应在当前线程执行");

        AppEventBus.unregister(subscriber);
    }

    // ── 异步事件：验证事件在后台 daemon 线程池中执行 ──

    @Test
    void postAsync_shouldExecuteOnBackgroundThread() throws Exception {
        // CountDownLatch 确保异步线程执行完毕后再验证
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> threadName = new AtomicReference<>();
        Object subscriber = new Object() {
            @Subscribe
            public void on(NavigationEvent e) {
                threadName.set(Thread.currentThread().getName());
                latch.countDown(); // 执行完毕，释放锁
            }
        };
        AppEventBus.register(subscriber);

        // 发布异步事件，当前线程不阻塞
        AppEventBus.postAsync(new NavigationEvent("home", "search"));
        assertTrue(latch.await(5, TimeUnit.SECONDS), "异步事件应在 5s 内触发");

        // 验证：线程名以 poe-event-async 开头（来自 AppEventBus 中的线程工厂）
        assertNotNull(threadName.get());
        assertTrue(threadName.get().startsWith("poe-event-async"),
            "异步事件线程名应以 poe-event-async 开头: " + threadName.get());
        // 验证：不在当前线程执行
        assertNotEquals(Thread.currentThread().getName(), threadName.get(),
            "异步事件不应在当前线程执行");

        AppEventBus.unregister(subscriber);
    }

    // ── 注册 / 取消注册：验证生命周期管理 ──

    @Test
    void subscriber_shouldReceiveEventsAfterRegistration() {
        AtomicInteger received = new AtomicInteger();
        Object subscriber = new Object() {
            @Subscribe
            public void on(NavigationEvent e) {
                received.incrementAndGet();
            }
        };
        AppEventBus.register(subscriber);

        // 注册前无事件
        assertEquals(0, received.get());

        // 每发布一次，订阅者计数 +1
        AppEventBus.postSync(new NavigationEvent("a", "b"));
        assertEquals(1, received.get());
        AppEventBus.postSync(new NavigationEvent("b", "c"));
        assertEquals(2, received.get());

        AppEventBus.unregister(subscriber);
    }

    @Test
    void unregisteredSubscriber_shouldNotReceiveEvents() {
        AtomicInteger received = new AtomicInteger();
        Object subscriber = new Object() {
            @Subscribe
            public void on(NavigationEvent e) {
                received.incrementAndGet();
            }
        };
        AppEventBus.register(subscriber);

        // 注册状态下能收到事件
        AppEventBus.postSync(new NavigationEvent("a", "b"));
        assertEquals(1, received.get());

        // 取消注册后事件不再送达
        AppEventBus.unregister(subscriber);
        AppEventBus.postSync(new NavigationEvent("b", "c"));
        assertEquals(1, received.get(), "取消注册后不应再收到事件");
    }

    // ── 核心事件类型：验证每种事件的 payload 完整性 ──

    @Test
    void navigationEvent_shouldCarrySourceAndTarget() {
        AtomicReference<String> capturedSource = new AtomicReference<>();
        AtomicReference<String> capturedTarget = new AtomicReference<>();
        Object subscriber = new Object() {
            @Subscribe
            public void on(NavigationEvent e) {
                capturedSource.set(e.getSource());
                capturedTarget.set(e.getTarget());
            }
        };
        AppEventBus.register(subscriber);

        // 模拟从基础物品页导航到设置页
        AppEventBus.postSync(new NavigationEvent("item-search/base-items", "settings"));
        assertEquals("item-search/base-items", capturedSource.get());
        assertEquals("settings", capturedTarget.get());

        AppEventBus.unregister(subscriber);
    }

    @Test
    void dataSyncEvents_shouldStreamProgressAndComplete() {
        // 捕获三个同步阶段的数据
        AtomicInteger startTotal = new AtomicInteger();
        AtomicReference<Double> progressValue = new AtomicReference<>();
        AtomicInteger completeCount = new AtomicInteger();
        AtomicReference<Long> completeDuration = new AtomicReference<>();

        Object subscriber = new Object() {
            @Subscribe
            public void onStart(DataSyncStartEvent e) {
                startTotal.set(e.getTotalRecords());
            }

            @Subscribe
            public void onProgress(DataSyncProgressEvent e) {
                // 使用 getProgress() 便捷方法计算百分比
                progressValue.set(e.getProgress());
            }

            @Subscribe
            public void onComplete(DataSyncCompleteEvent e) {
                completeCount.set(e.getSyncedRecords());
                completeDuration.set(e.getDurationMs());
            }
        };
        AppEventBus.register(subscriber);

        // 模拟完整同步流程：开始 → 进度 → 完成
        AppEventBus.postSync(new DataSyncStartEvent("base_items", 500));
        AppEventBus.postSync(new DataSyncProgressEvent("base_items", 250, 500));
        AppEventBus.postSync(new DataSyncCompleteEvent("base_items", 500, 3200L));

        assertEquals(500, startTotal.get());
        assertEquals(0.5, progressValue.get(), 0.001);
        assertEquals(500, completeCount.get());
        assertEquals(3200L, completeDuration.get());

        AppEventBus.unregister(subscriber);
    }

    @Test
    void configChangedEvent_shouldCarryKeyAndValues() {
        AtomicReference<String> capturedKey = new AtomicReference<>();
        AtomicReference<String> capturedOld = new AtomicReference<>();
        AtomicReference<String> capturedNew = new AtomicReference<>();
        Object subscriber = new Object() {
            @Subscribe
            public void on(ConfigChangedEvent e) {
                capturedKey.set(e.getKey());
                capturedOld.set(e.getOldValue());
                capturedNew.set(e.getNewValue());
            }
        };
        AppEventBus.register(subscriber);

        // 模拟用户切换语言
        AppEventBus.postSync(new ConfigChangedEvent("language", "zh", "en"));
        assertEquals("language", capturedKey.get());
        assertEquals("zh", capturedOld.get());
        assertEquals("en", capturedNew.get());

        AppEventBus.unregister(subscriber);
    }

    // ── 边界情况：多订阅者、批量异步事件 ──

    /** 验证多个订阅者能独立接收同一事件 */
    /** 验证多个订阅者能独立接收同一事件 */
    @Test
    void multipleSubscribers_shouldAllReceiveEvent() {
        AtomicInteger countA = new AtomicInteger();
        AtomicInteger countB = new AtomicInteger();
        Object subA = new Object() {
            @Subscribe
            public void on(NavigationEvent e) { countA.incrementAndGet(); }
        };
        Object subB = new Object() {
            @Subscribe
            public void on(NavigationEvent e) { countB.incrementAndGet(); }
        };
        AppEventBus.register(subA);
        AppEventBus.register(subB);

        // 两个订阅者应各自收到事件
        AppEventBus.postSync(new NavigationEvent("x", "y"));
        assertEquals(1, countA.get());
        assertEquals(1, countB.get());

        AppEventBus.unregister(subA);
        AppEventBus.unregister(subB);
    }

    /** 验证异步批量发布时所有事件都能被正确接收 */
    @Test
    void postAsync_multipleEvents_shouldAllArrive() throws Exception {
        int eventCount = 10;
        // CountDownLatch 初始化为事件总数，每收到一个事件 countDown 一次
        CountDownLatch latch = new CountDownLatch(eventCount);
        AtomicInteger received = new AtomicInteger();
        Object subscriber = new Object() {
            @Subscribe
            public void on(NavigationEvent e) {
                received.incrementAndGet();
                latch.countDown();
            }
        };
        AppEventBus.register(subscriber);

        // 批量投递 10 个异步事件
        for (int i = 0; i < eventCount; i++) {
            AppEventBus.postAsync(new NavigationEvent("src", "tgt" + i));
        }
        // 等待所有事件处理完毕（10 秒超时）
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        assertEquals(eventCount, received.get());

        AppEventBus.unregister(subscriber);
    }
}
