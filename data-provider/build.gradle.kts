plugins {
    java
    application
}

application {
    mainClass.set("com.poe.provider.tool.SeedDbBuilder")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":data-cache"))
    implementation(project(":app-core"))

    // OkHttp HTTP 客户端
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Jackson JSON 解析
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")

    // Guava RateLimiter 限流
    implementation("com.google.guava:guava:33.0.0-jre")

    // SLF4J 日志
    implementation("org.slf4j:slf4j-api:2.0.9")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.14")

    // 测试依赖
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}

// 构建种子数据库的 Gradle 任务
tasks.register<JavaExec>("buildSeedDb") {
    group = "data"
    description = "构建内置种子数据库 seed.db（含 PoeCharm2 翻译 + Wiki 数据）"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.poe.provider.tool.SeedDbBuilder")
    args("--output", rootProject.projectDir.resolve("app-ui/src/main/resources/seed.db").toString())

    // 允许跳过 Wiki 同步
    if (project.hasProperty("skipWiki")) {
        args("--skip-wiki")
    }
}
