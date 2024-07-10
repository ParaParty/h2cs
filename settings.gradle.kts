pluginManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "h2cs-project"

val modules = listOf(
    "h2cs",
    "h2cs-gradle",
    "h2cs-cli",
)

for (item in modules) {
    include(":$item")
}

for (item in modules) {
    project(":$item").projectDir = file("$rootDir/$item")
}
