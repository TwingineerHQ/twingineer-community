import com.vanniktech.maven.publish.SonatypeHost

group = "com.twingineer"
version = "0.1.0"
description = "Gradle conventions for Twingineer Community"

plugins {
    `kotlin-dsl`
    `version-catalog`
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
}

repositories {
    gradlePluginPortal() // so that external plugins can be resolved in dependencies section
}

dependencies {
    implementation(platform(libs.kotlin.bom))

    implementation(libs.dokka.gradle)
    implementation(libs.kotlin.gradle)
    implementation(libs.maven.publish)

    // ref: https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)
val javadocJar by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

((project as ExtensionAware).extensions.getByName("mavenPublishing") as com.vanniktech.maven.publish.MavenPublishBaseExtension).run {
//mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    pom {
        name.set(project.name)
        description.set(project.description)
        url.set("https://github.com/TwingineerHQ/twingineer-community")
        licenses {
            license {
                name.set("AGPL-3.0-or-later")
                url.set("https://www.gnu.org/licenses/agpl.txt")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                name.set("Twingineer")
                url.set("https://twingineer.com")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/TwingineerHQ/twingineer-community.git")
            developerConnection.set("scm:git:ssh://github.com:TwingineerHQ/twingineer-community.git")
            url.set("https://github.com/TwingineerHQ/twingineer-community/tree/main")
        }
    }

    signAllPublications()
}