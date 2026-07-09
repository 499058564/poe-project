plugins {
    java
}

dependencies {
    implementation(project(":common"))

    // Guava EventBus (for app-core orchestration)
    implementation("com.google.guava:guava:33.0.0-jre")

    // SLF4J API
    implementation("org.slf4j:slf4j-api:2.0.9")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
