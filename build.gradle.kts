group = rootProject.group
version = rootProject.version

plugins {
    kotlin("jvm") version "2.1.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
}

group = "ch.wesr.slidebuilder"
version = "0.0.1"

allprojects {
    repositories {
        mavenCentral()
    }
}

dependencies {
    implementation("ai.koog:koog-agents:0.1.0")
}
