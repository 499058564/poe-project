# T-003 搭建 app-ui 模块 + JavaFX 启动

**版本**：v0.1.0  
**模块**：`app-ui`  
**预估**：0.5 天  
**前置**：T-001  
**状态**：已完成

---

## 任务描述

创建 JavaFX 界面模块，实现主窗口启动和基础窗口配置。

## 详细步骤

### 1. 创建 `app-ui/build.gradle.kts`

```kotlin
plugins {
    java
    application
    id("org.openjfx.javafxplugin")
}

javafx {
    version = "21.0.2"
    modules("javafx.controls", "javafx.fxml", "javafx.graphics")
}

application {
    mainClass.set("com.poe.ui.PoeApplication")
    applicationDefaultJvmArgs = listOf(
        "--add-exports", "javafx.base/com.sun.javafx.event=ALL-UNNAMED"
    )
}

dependencies {
    implementation(project(":common"))
    
    // JavaFX 主题库
    implementation("io.github.mkpaz:atlantafx-base:2.0.1")
    
    // 日志实现
    runtimeOnly("ch.qos.logback:logback-classic:1.4.14")
}
```

### 2. 创建主入口 `PoeApplication.java`

```java
package com.poe.ui;

import javafx.application.Application;
import javafx.stage.Stage;
import com.poe.ui.main.MainWindow;

public class PoeApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainWindow mainWindow = new MainWindow(primaryStage);
        mainWindow.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

### 3. 创建 `MainWindow.java`

```java
package com.poe.ui.main;

public class MainWindow {
    private static final String APP_TITLE = "POE Tool - v0.1.0";
    private static final int DEFAULT_WIDTH = 1280;
    private static final int DEFAULT_HEIGHT = 800;
    private static final int MIN_WIDTH = 1024;
    private static final int MIN_HEIGHT = 600;

    public MainWindow(Stage stage) {
        stage.setTitle(APP_TITLE);
        stage.setWidth(DEFAULT_WIDTH);
        stage.setHeight(DEFAULT_HEIGHT);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.centerOnScreen();
        // ...
    }

    public void show() {
        stage.show();
    }
}
```

## 验收标准

- [ ] `./gradlew :app-ui:run` 启动成功
- [ ] 窗口标题显示 "POE Tool - v0.1.0"
- [ ] 窗口 1280×800，居中显示
- [ ] 最小尺寸限制 1024×600 生效
- [ ] 关闭窗口正常退出
