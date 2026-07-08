plugins {
    java
}

dependencies {
    // Jackson JSON
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.16.1")

    // SLF4J API (implementation provided by each module)
    implementation("org.slf4j:slf4j-api:2.0.9")
}
