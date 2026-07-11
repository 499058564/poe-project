package com.poe.ui;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.stage.Stage;
import com.poe.ui.window.MainWindow;
import com.poe.ui.theme.ThemeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * PoE Tool 应用主入口。
 *
 * <p>继承 JavaFX {@link Application}，在 {@link #start(Stage)} 中：
 * <ol>
 *   <li>创建 {@link MainWindow}（组合 Sidebar + ContentArea + StatusBar）</li>
 *   <li>加载自定义 PoE 暗黑主题 CSS（AtlantaFX 保留备用）</li>
 *   <li>显示主窗口</li>
 * </ol>
 */
public class PoeApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(PoeApplication.class);

    /**
     * 应用初始化回调，在 JavaFX 线程启动前执行。
     * 输出当前 Java 和 JavaFX 版本信息，用于调试和日志记录。
     */
    @Override
    public void init() {
        log.info("PoE Tool 正在启动...");
        log.info("Java 版本: {}", System.getProperty("java.version"));
        log.info("JavaFX 版本: {}", System.getProperty("javafx.version"));
    }

    /**
     * JavaFX 应用启动入口。
     * 创建主窗口、加载自定义 PoE 暗黑主题样式表并显示窗口。
     *
     * @param primaryStage JavaFX 主舞台，由框架注入
     */
    @Override
    public void start(Stage primaryStage) {
        // 集成 AtlantaFX 暗黑主题
        // Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        MainWindow mainWindow = new MainWindow(primaryStage);

        // 加载自定义 PoE 暗黑样式
        mainWindow.getScene().getStylesheets().add(
            Objects.requireNonNull(getClass().getResource(ThemeConstants.DARK_THEME_CSS)).toExternalForm()
        );

        mainWindow.show();
    }

    /**
     * 程序主入口，委托给 JavaFX {@link Application#launch(String...)}。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        launch(args);
    }
}
