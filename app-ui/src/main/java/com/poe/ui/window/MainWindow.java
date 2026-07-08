package com.poe.ui.window;

import com.poe.ui.components.ContentArea;
import com.poe.ui.components.Sidebar;
import com.poe.ui.components.StatusBar;
import com.poe.ui.i18n.Messages;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * 应用主窗口。
 * <pre>
 * ┌──────────────────────────────────────────┐
 * │         Sidebar  │  TabPane             │
 * │         (导航菜单)  │  (多标签内容区)        │
 * ├──────────────────┴──────────────────────┤
 * │  状态栏: 数据版本 | 同步时间 | 状态        │
 * └──────────────────────────────────────────┘
 * </pre>
 */
public class MainWindow {

    private static final int DEFAULT_WIDTH = 1280;
    private static final int DEFAULT_HEIGHT = 800;
    private static final int MIN_WIDTH = 1024;
    private static final int MIN_HEIGHT = 600;

    private final Stage stage;
    private final Scene scene;
    private final Sidebar sidebar;
    private final ContentArea contentArea;
    private final StatusBar statusBar;

    public MainWindow(Stage stage) {
        this.stage = stage;
        stage.setTitle(Messages.get("app.title"));
        stage.setWidth(DEFAULT_WIDTH);
        stage.setHeight(DEFAULT_HEIGHT);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.centerOnScreen();

        sidebar = new Sidebar();
        contentArea = new ContentArea();
        statusBar = new StatusBar();

        // 导航回调：侧边栏点击 → 内容区打开对应 Tab
        sidebar.setOnNavigate(pageId -> {
            contentArea.openPage(pageId);
            sidebar.selectPage(pageId);
        });

        // 默认打开物品搜索页
        sidebar.selectPage("item-search");
        contentArea.openPage("item-search");

        BorderPane root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(contentArea);
        root.setBottom(statusBar);

        this.scene = new Scene(root);
        stage.setScene(scene);
    }

    public Scene getScene() {
        return scene;
    }

    public Sidebar getSidebar() {
        return sidebar;
    }

    public ContentArea getContentArea() {
        return contentArea;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    public void show() {
        stage.show();
    }
}
