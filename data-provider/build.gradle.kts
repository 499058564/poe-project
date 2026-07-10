plugins {
    java
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

    // 测试依赖
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}
