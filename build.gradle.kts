import org.gradle.api.JavaVersion
import org.gradle.api.tasks.testing.Test

plugins {
    java
    id("org.openjfx.javafxplugin") version "0.1.0" apply false
}

allprojects {
    group = "com.poe"
    version = providers.gradleProperty("projectVersion").getOrElse("0.1.0")
}

subprojects {
    apply(plugin = "java")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/spring") }
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
