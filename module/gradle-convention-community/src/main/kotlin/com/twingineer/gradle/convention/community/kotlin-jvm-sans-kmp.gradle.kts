package com.twingineer.gradle.convention.community

plugins {
    id("org.jetbrains.kotlin.jvm")
}

repositories {
    mavenCentral()
}

try {
    dependencies {
        implementation(platform(libs.kotlin.bom.get()))
        testImplementation(libs.kotlin.test.get())
    }

    kotlin {
        jvmToolchain {
            libs.versions.java.language.get()
                .let { languageVersion.set(JavaLanguageVersion.of(it)) }
        }
    }
// occurs when this plugin is applied from a jar in an external precompiled script plugin. it should not be instantiated
// at that point, but perhaps needs to be to extract some metadata. suppressing it appears to have no consequence.
} catch (ignored : UnknownDomainObjectException) {}

tasks.withType<Test> {
    enableAssertions = true
}
