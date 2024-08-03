pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    includeBuild("../gradle-convention-community")
}

plugins {
    id("com.twingineer.gradle.convention.community.module")
}

// IMPORTANT order matters here - all dependencies, transitively, have to precede the depender
//listOf(
//).forEach { includeBuild("../$it") }