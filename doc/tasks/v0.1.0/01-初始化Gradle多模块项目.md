# T-001 初始化 Gradle 多模块项目

**版本**：v0.1.0  
**模块**：根项目  
**预估**：0.5 天  
**前置**：无  
**状态**：已完成

---

## 任务描述

创建 Gradle 多模块项目骨架，定义所有子模块和公共依赖。

## 详细步骤

### 1. 创建根 `settings.gradle.kts`

```kotlin
rootProject.name = "poe-project"

include(
    "common",
    "app-ui",
    "app-core",
    "data-cache",
    "data-provider",
    "pob-runtime",
    "pob-adapter",
    "pob-ipc"
)
```

> 初始阶段仅启用 `common` 和 `app-ui`，其余模块后续逐步激活。

### 2. 创建根 `build.gradle.kts`

统一配置所有子模块的公共属性：

```kotlin
plugins {
    java
    id("org.openjfx.javafxplugin") version "0.1.0" apply false
}

allprojects {
    group = "com.poe"
    version = "0.1.0"
}

subprojects {
    apply(plugin = "java")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        // 所有模块公共依赖
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
```

### 3. 创建 `gradle.properties`

```properties
org.gradle.jvmargs=-Xmx2g --add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED
projectVersion=0.1.0
javaVersion=17
```

### 4. 创建 Gradle Wrapper

```bash
gradle wrapper --gradle-version 8.5
```

## 验收标准

- [ ] `./gradlew projects` 列出所有子模块
- [ ] `./gradlew build` 构建成功（即使模块为空）
- [ ] `.gitignore` 包含 `build/`, `.gradle/`, `.idea/`
- [ ] Git 提交项目骨架
