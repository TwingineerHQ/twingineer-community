plugins {
    id("com.twingineer.gradle.convention.community.kotlin-jvm-sans-kmp")
    alias(libs.plugins.terpal)
}

dependencies {
    implementation("com.twingineer:mof")
    implementation(libs.apache.commons.lang3)
    implementation(libs.apache.commons.text)
    implementation(libs.clikt)
    implementation(libs.kotlin.logging)
    implementation(libs.ktemplar)
    implementation(libs.slf4j.simple)

    implementation(platform(libs.jackson.bom))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}

fun checkInputParameter() {
    if (!project.properties.containsKey("input"))
        throw IllegalArgumentException("Input parameter must be specified, e.g. `./gradlew ... -Pinput=/path/to/file`")
}

tasks {
    register<JavaExec>("generateInterfaces") {
        val output = project.layout.buildDirectory.dir("gen/interfaces")
        mainClass.set("com.twingineer.sysml.ref.gen.GenerateInterfaces")

        dependsOn(jar)

        doFirst {
            checkInputParameter()
            delete(output)
        }

        classpath = sourceSets.main.get().runtimeClasspath
        args = listOf(
            "--input",
            project.properties["input"].toString(),
            "--output",
            output.get().toString(),
        )
    }

    register<JavaExec>("generateImpl") {
        val output = project.layout.buildDirectory.dir("gen/impl")
        mainClass.set("com.twingineer.sysml.ref.gen.GenerateImpl")

        dependsOn(jar)

        doFirst {
            checkInputParameter()
            delete(output)
        }

        classpath = sourceSets.main.get().runtimeClasspath
        args = listOf(
            "--input",
            project.properties["input"].toString(),
            "--output",
            output.get().toString(),
        )
    }

    register<JavaExec>("generateJsonSchema") {
        val output = project.layout.buildDirectory.dir("gen/jsonschema")
        mainClass.set("com.twingineer.sysml.ref.gen.GenerateJsonSchema")

        dependsOn(jar)

        doFirst {
            checkInputParameter()
            delete(output)
        }

        classpath = sourceSets.main.get().runtimeClasspath
        args = listOf(
            "--input",
            project.properties["input"].toString(),
            "--output",
            output.get().toString(),
        )
    }

    register<JavaExec>("generateJsonSchemaKerML") {
        val output = project.layout.buildDirectory.dir("gen/jsonschema-kerml")
        mainClass.set("com.twingineer.sysml.ref.gen.GenerateJsonSchema")

        dependsOn(jar)

        doFirst {
            checkInputParameter()
            delete(output)
        }

        classpath = sourceSets.main.get().runtimeClasspath
        args = listOf(
            "--input",
            project.properties["input"].toString(),
            "--output",
            output.get().toString(),
        )
    }

    register<JavaExec>("generateJsonLd") {
        val output = project.layout.buildDirectory.dir("gen/jsonld")
        mainClass.set("com.twingineer.sysml.ref.gen.GenerateJsonLd")

        dependsOn(jar)

        doFirst {
            checkInputParameter()
            delete(output)
        }

        classpath = sourceSets.main.get().runtimeClasspath
        args = listOf(
            "--input",
            project.properties["input"].toString(),
            "--output",
            output.get().toString(),
        )
    }
}