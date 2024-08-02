package com.twingineer.gradle.convention.community

plugins {
    id("org.jetbrains.kotlin.jvm")
}

repositories {
    mavenCentral()
}

val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

dependencies {
    implementation(platform(libs.kotlin.bom))
    testImplementation(libs.kotlin.test)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.language.get()))
    }
}

tasks.withType<Test> {
    enableAssertions = true
}
