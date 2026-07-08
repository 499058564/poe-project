pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
    }
}

rootProject.name = "poe-project"

include(
    "common",
    "app-ui",
    "app-core",
    "data-cache",
    "data-provider",
    "poecharm2",
    "pob-runtime",
    "pob-adapter",
    "pob-ipc"
)
