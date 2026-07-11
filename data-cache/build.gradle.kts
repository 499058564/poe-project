plugins {
    java
}

dependencies {
    implementation(project(":common"))

    // SQLite JDBC 驱动
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")

    // HikariCP 连接池
    implementation("com.zaxxer:HikariCP:5.1.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
