rootProject.name = "twingineer-community"

// IMPORTANT order matters here - all dependencies, transitively, have to precede the depender
listOf(
    "gradle-convention-community",
    "mof",
    "sysml-ref-generator",
).forEach { includeBuild("module/$it") }
