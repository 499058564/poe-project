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

    // SLF4J API
    implementation("org.slf4j:slf4j-api:2.0.9")

    // 日志实现
    runtimeOnly("ch.qos.logback:logback-classic:1.4.14")
}
