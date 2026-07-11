package com.poe.ui.window;

import com.poe.core.service.ItemSearchService;
import com.poe.ui.components.ContentArea;
import com.poe.ui.components.Sidebar;
import com.poe.ui.components.StatusBar;
import com.poe.ui.constants.LayoutConstants;
import com.poe.ui.constants.PageIds;
import com.poe.ui.enums.PageDefEnum;
import com.poe.ui.i18n.Messages;
import com.poe.ui.i18n.keys.AppKeys;
import com.poe.ui.page.ItemSearchView;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 应用主窗口。
 * 组合 Sidebar + ContentArea + StatusBar 三大区域，
 * 负责导航回调、默认页面打开、服务初始化与场景创建。
 */
public class MainWindow {

    /** 顶层 Stage 引用 */
    private final Stage stage;
    /** 顶层 Scene（用于外部注入样式表） */
    private final Scene scene;
    /** 左侧导航侧边栏 */
    private final Sidebar sidebar;
    /** 中央多标签内容区 */
    private final ContentArea contentArea;
    /** 底部状态栏 */
    private final StatusBar statusBar;

    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);

    /**
     * 构造主窗口，初始化 Sidebar、ContentArea、StatusBar 三大组件，
     * 设置导航回调与默认页面，并将根布局挂载到 Scene 上。
     *
     * @param stage JavaFX 顶层舞台
     */
    public MainWindow(Stage stage) {
        this.stage = stage;
        log.info("Creating main window: {}x{}", LayoutConstants.WINDOW_DEFAULT_WIDTH,
            LayoutConstants.WINDOW_DEFAULT_HEIGHT);
        stage.setTitle(Messages.get(AppKeys.TITLE));
        stage.setWidth(LayoutConstants.WINDOW_DEFAULT_WIDTH);
        stage.setHeight(LayoutConstants.WINDOW_DEFAULT_HEIGHT);
        stage.setMinWidth(LayoutConstants.WINDOW_MIN_WIDTH);
        stage.setMinHeight(LayoutConstants.WINDOW_MIN_HEIGHT);
        stage.centerOnScreen();

        ItemSearchService searchService = initItemSearchServices();

        sidebar = new Sidebar(PageDefEnum.values());
        contentArea = new ContentArea(PageDefEnum.values());

        // Wire page factory — map page IDs to real pages
        contentArea.setPageFactory(pageId -> {
            switch (pageId) {
                case PageIds.ITEM_BASE:
                    return new ItemSearchView(searchService);
                case PageIds.ITEM_UNIQUE:
                    return new ItemSearchView(searchService);
                case PageIds.ITEM_CURRENCY:
                    return new ItemSearchView(searchService);
                default:
                    return null; // fall back to placeholder
            }
        });

        statusBar = new StatusBar();

        // 导航回调：侧边栏点击 → 内容区打开对应 Tab
        sidebar.setOnNavigate(pageId -> {
            log.debug("Navigate to page: {}", pageId);
            contentArea.openPage(pageId);
            sidebar.selectPage(pageId);
        });

        // 默认打开物品搜索页
        log.info("Opening default page: {}", PageIds.DEFAULT_PAGE);
        sidebar.selectPage(PageIds.DEFAULT_PAGE);
        contentArea.openPage(PageIds.DEFAULT_PAGE);

        BorderPane root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(contentArea);
        root.setBottom(statusBar);

        this.scene = new Scene(root);
        stage.setScene(scene);
        log.debug("Main window scene initialized");
    }

    /**
     * 初始化后端服务：数据库管理器、DAO、翻译服务、物品搜索服务。
     *
     * @return ItemSearchService 实例
     */
    private ItemSearchService initItemSearchServices() {
        try {
            log.info("MainWindow|initializeServices|successfully");
            return new ItemSearchService();
        } catch (Exception e) {
            log.error("MainWindow|initializeServices|failed", e);
            return null;
        }
    }

    /**
     * 获取主窗口 Scene，供外部注入样式表。
     *
     * @return 当前场景
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * 获取左侧导航侧边栏。
     *
     * @return Sidebar 实例
     */
    public Sidebar getSidebar() {
        return sidebar;
    }

    /**
     * 获取中央多标签内容区。
     *
     * @return ContentArea 实例
     */
    public ContentArea getContentArea() {
        return contentArea;
    }

    /**
     * 获取底部状态栏。
     *
     * @return StatusBar 实例
     */
    public StatusBar getStatusBar() {
        return statusBar;
    }

    /** 显示主窗口。 */
    public void show() {
        stage.show();
    }
}