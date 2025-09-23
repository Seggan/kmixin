plugins {
    kotlin("jvm") version "2.1.0"
    `maven-publish`
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
}

group = "io.github.seggan"
version = "0.1.0-SNAPSHOT"

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:2.2.10-2.0.2")
    implementation("org.spongepowered:mixin:0.8.7")
}

kotlin {
    jvmToolchain(21)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            groupId = project.group.toString()
            artifactId = "kmixin"
            version = project.version.toString()
        }
    }
}