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
    implementation(project(":app-core"))

    // JavaFX 主题库
    implementation("io.github.mkpaz:atlantafx-base:2.0.1")

    // SLF4J API
    implementation("org.slf4j:slf4j-api:2.0.9")

    // 日志实现
    runtimeOnly("ch.qos.logback:logback-classic:1.4.14")

    // 测试依赖
    testImplementation("org.testfx:testfx-junit5:4.0.18")
}

// 确保 seed.db 被打包到 jar 资源中（如果存在）
tasks.processResources {
    val seedDb = rootProject.projectDir.resolve("app-ui/src/main/resources/seed.db")
    if (seedDb.exists()) {
        from(seedDb)
    }
}
