package com.poe.core.event;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/**
 * 应用级事件总线，基于 Guava EventBus 封装。
 *
 * <p>提供同步和异步两种发布方式：
 * <ul>
 *   <li>{@link #postSync(Object)} — 当前线程同步执行</li>
 *   <li>{@link #postAsync(Object)} — 后台线程池异步执行</li>
 * </ul>
 *
 * <p>使用方式：
 * <pre>{@code
 *   // 注册订阅者
 *   AppEventBus.register(this);
 *
 *   // 订阅事件
 *   @Subscribe public void onEvent(NavigationEvent e) { ... }
 *
 *   // 发布事件（无需注册）
 *   AppEventBus.postSync(new NavigationEvent("src", "target"));
 * }</pre>
 */
public final class AppEventBus {

    /** 同步总线名称 */
    private static final String SYNC_BUS_NAME = "poe-sync";
    /** 异步线程池大小 */
    private static final int ASYNC_POOL_SIZE = 4;

    /** 同步总线：在当前线程中依次调用所有匹配的 {@code @Subscribe} 方法 */
    private static final EventBus SYNC_BUS = new EventBus(SYNC_BUS_NAME);

    /**
     * 异步线程池，使用 daemon 线程避免阻止 JVM 退出。
     * 固定 4 线程，适合 I/O 密集的同步/搜索任务。
     */
    private static final ExecutorService ASYNC_EXECUTOR =
        Executors.newFixedThreadPool(ASYNC_POOL_SIZE, r -> {
            Thread t = new Thread(r, "poe-event-async");
            t.setDaemon(true);
            return t;
        });

    /** 异步总线：将事件投递到线程池中执行，不阻塞发布者 */
    private static final AsyncEventBus ASYNC_BUS = new AsyncEventBus(ASYNC_EXECUTOR);

    private AppEventBus() {
        // 工具类不允许实例化
    }

    /**
     * 注册订阅者到同步和异步两条总线。
     *
     * @param subscriber 包含 {@code @Subscribe} 方法的对象
     */
    public static void register(Object subscriber) {
        SYNC_BUS.register(subscriber);
        ASYNC_BUS.register(subscriber);
    }

    /**
     * 从两条总线取消注册。
     *
     * @param subscriber 已注册的订阅者
     */
    public static void unregister(Object subscriber) {
        SYNC_BUS.unregister(subscriber);
        ASYNC_BUS.unregister(subscriber);
    }

    /**
     * 同步发布事件，在当前线程中调用所有订阅者。
     *
     * @param event 事件对象
     */
    public static void postSync(Object event) {
        SYNC_BUS.post(event);
    }

    /**
     * 异步发布事件，由后台线程池调用所有订阅者。
     *
     * @param event 事件对象
     */
    public static void postAsync(Object event) {
        ASYNC_BUS.post(event);
    }
}
