package com.poe.ui;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.stage.Stage;
import com.poe.ui.window.MainWindow;
import com.poe.ui.theme.ThemeConstants;

import java.util.Objects;

/**
 * PoE Tool 应用主入口。
 */
public class PoeApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 集成 AtlantaFX 暗黑主题
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        MainWindow mainWindow = new MainWindow(primaryStage);

        // 加载自定义 PoE 暗黑样式
        mainWindow.getScene().getStylesheets().add(
            Objects.requireNonNull(getClass().getResource(ThemeConstants.DARK_THEME_CSS)).toExternalForm()
        );

        mainWindow.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
