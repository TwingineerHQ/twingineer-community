pluginManagement {
    includeBuild("../gradle-convention-community")
    repositories {
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }
}

// IMPORTANT order matters here - all dependencies, transitively, have to precede the depender
//listOf(
//).forEach { includeBuild("../$it") }