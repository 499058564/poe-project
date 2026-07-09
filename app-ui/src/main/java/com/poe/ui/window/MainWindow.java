package com.poe.ui.window;

import com.poe.ui.components.ContentArea;
import com.poe.ui.components.Sidebar;
import com.poe.ui.components.StatusBar;
import com.poe.ui.constants.LayoutConstants;
import com.poe.ui.constants.PageIds;
import com.poe.ui.enums.PageDefEnum;
import com.poe.ui.i18n.Messages;
import com.poe.ui.i18n.keys.AppKeys;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * 应用主窗口。
 * 组合 Sidebar + ContentArea + StatusBar 三大区域，
 * 负责导航回调、默认页面打开与场景创建。
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

    public MainWindow(Stage stage) {
        this.stage = stage;
        stage.setTitle(Messages.get(AppKeys.TITLE));
        stage.setWidth(LayoutConstants.WINDOW_DEFAULT_WIDTH);
        stage.setHeight(LayoutConstants.WINDOW_DEFAULT_HEIGHT);
        stage.setMinWidth(LayoutConstants.WINDOW_MIN_WIDTH);
        stage.setMinHeight(LayoutConstants.WINDOW_MIN_HEIGHT);
        stage.centerOnScreen();

        sidebar = new Sidebar(PageDefEnum.values());
        contentArea = new ContentArea(PageDefEnum.values());
        statusBar = new StatusBar();

        // 导航回调：侧边栏点击 → 内容区打开对应 Tab
        sidebar.setOnNavigate(pageId -> {
            contentArea.openPage(pageId);
            sidebar.selectPage(pageId);
        });

        // 默认打开物品搜索页
        sidebar.selectPage(PageIds.DEFAULT_PAGE);
        contentArea.openPage(PageIds.DEFAULT_PAGE);

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