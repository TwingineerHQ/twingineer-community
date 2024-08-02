pluginManagement {
    includeBuild("../gradle-convention-community")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }
}

// IMPORTANT order matters here - all dependencies, transitively, have to precede the depender
listOf(
    "mof",
).forEach { includeBuild("../$it") }