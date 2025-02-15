package com.twingineer.gradle.convention.community

plugins {
    id("org.jetbrains.kotlin.jvm")
}

repositories {
    mavenCentral()
}

try {
    val javaLanguageVersion: String = libs.versions.java.language.get()

    dependencies {
        implementation(platform(libs.kotlin.bom))
        testImplementation(libs.kotlin.test)
    }

    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(javaLanguageVersion))
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaLanguageVersion))
        }

        sourceCompatibility = JavaVersion.toVersion(javaLanguageVersion)
        targetCompatibility = JavaVersion.toVersion(javaLanguageVersion)
    }

// occurs when this plugin is applied from a jar in an external precompiled script plugin. it should not be instantiated
// at that point, but perhaps needs to be to extract some metadata. suppressing it appears to have no consequence.
} catch (ignored : UnknownDomainObjectException) {}

tasks.withType<Test> {
    enableAssertions = true
}
