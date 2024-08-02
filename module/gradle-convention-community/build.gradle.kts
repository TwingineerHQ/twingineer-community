group = "com.twingineer"
version = "0.1.0"

plugins {
    `kotlin-dsl`
    `version-catalog`
}

repositories {
    gradlePluginPortal() // so that external plugins can be resolved in dependencies section
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.bom)
    implementation(libs.kotlin.gradle)

    // ref: https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}