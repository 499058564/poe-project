plugins {
    java
}

dependencies {
    implementation(project(":common"))
    implementation(project(":data-cache"))

    // Guava EventBus (for app-core orchestration)
    implementation("com.google.guava:guava:33.0.0-jre")

    // SLF4J API
    implementation("org.slf4j:slf4j-api:2.0.9")

    // Jackson (for JSON parsing in services)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")

    // SQLite JDBC (for test in-memory databases)
    testImplementation("org.xerial:sqlite-jdbc:3.44.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
