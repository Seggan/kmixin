plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "1.3.0"
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
}

group = "io.github.seggan"
version = "0.1.0"

dependencies {
    implementation("org.ow2.asm:asm:9.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.spongepowered:mixin:0.8.7")
    implementation("org.jetbrains.kotlin:kotlin-metadata-jvm:2.0.20")
}

kotlin {
    jvmToolchain(21)
}

gradlePlugin {
    website = "https://github.com/Seggan/kmixin"
    vcsUrl = "https://github.com/Seggan/kmixin"
    plugins {
        create("kmixin") {
            id = "io.github.seggan.kmixin"
            implementationClass = "io.github.seggan.kmixin.KMixinPlugin"
        }
    }
}