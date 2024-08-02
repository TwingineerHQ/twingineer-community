group = "com.twingineer"
version = "0.1.0"

plugins {
    id("com.twingineer.gradle.convention.community.kotlin-jvm-sans-kmp")
}

dependencies {
    implementation(platform(libs.jackson.bom))
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}