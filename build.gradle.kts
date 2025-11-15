group = "dev.luna5ama"
version = "0.0.1-SNAPSHOT"

plugins {
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.hotReload)
}

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://central.sonatype.com/repository/maven-snapshots/")
}

dependencies {
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}