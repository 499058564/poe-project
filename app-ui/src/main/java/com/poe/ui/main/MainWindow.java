package com.poe.ui.main;

import javafx.stage.Stage;

/**
 * 应用主窗口，负责窗口基础配置：标题、尺寸、居中。
 */
public class MainWindow {

    private static final String APP_TITLE = "POE Tool - v0.1.0";
    private static final int DEFAULT_WIDTH = 1280;
    private static final int DEFAULT_HEIGHT = 800;
    private static final int MIN_WIDTH = 1024;
    private static final int MIN_HEIGHT = 600;

    private final Stage stage;

    public MainWindow(Stage stage) {
        this.stage = stage;
        stage.setTitle(APP_TITLE);
        stage.setWidth(DEFAULT_WIDTH);
        stage.setHeight(DEFAULT_HEIGHT);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.centerOnScreen();
    }

    public void show() {
        stage.show();
    }
}
