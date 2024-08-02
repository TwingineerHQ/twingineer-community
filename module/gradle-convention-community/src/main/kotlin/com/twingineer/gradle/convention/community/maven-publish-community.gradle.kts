package com.twingineer.gradle.convention.community

import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.twingineer.gradle.convention.community.maven-publish")
    id("com.twingineer.gradle.convention.community.dokka")
}

mavenPublishing {
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