allprojects {
    group = "com.twingineer"
    version = "0.1.0"
}

// used for dynamic tasks like :ijDownloadSources*
repositories {
    mavenCentral()
}

tasks {
    sequenceOf(
        "assemble",
        "build",
        "clean",
    ).forEach { name ->
        register(name) {
            group = "build"
            gradle.includedBuilds.forEach {
                dependsOn(it.task(":$name"))
            }
        }
    }
}