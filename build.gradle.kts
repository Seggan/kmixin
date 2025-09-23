plugins {
    kotlin("jvm") version "2.1.0"
    `maven-publish`
    signing
    id("com.gradleup.nmcp.aggregation") version "1.1.0"
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
}

group = "io.github.seggan"
version = "0.1.0"

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:2.2.10-2.0.2")
}

kotlin {
    jvmToolchain(21)
}

val emptyJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks.kotlinSourcesJar)
            artifact(emptyJavadocJar)

            groupId = project.group.toString()
            artifactId = "kmixin"
            version = project.version.toString()
            pom {
                name = artifactId
                description = "A Kotlin annotation processor for generating Mixins."
                url = "https://github.com/Seggan/kmixin"
                licenses {
                    license {
                        name = "GNU Lesser General Public License Version 3"
                        url = "https://www.gnu.org/licenses/lgpl-3.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "Seggan"
                        name = "Seggan"
                        organizationUrl = "https://github.com/Seggan"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/Seggan/kmixin.git"
                    developerConnection = "scm:git:ssh://github.com:Seggan/kmixin.git"
                    url = "https://github.com/Seggan/kmixin"
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["maven"])
}

nmcpAggregation {
    centralPortal {
        username = project.property("centralPortal.username")?.toString()
        password = project.property("centralPortal.password")?.toString()
        // publish manually from the portal
        publishingType = "USER_MANAGED"
        // or if you want to publish automatically
        publishingType = "AUTOMATIC"
    }

    // Publish all projects that apply the 'maven-publish' plugin
    publishAllProjectsProbablyBreakingProjectIsolation()
}