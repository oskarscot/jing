plugins {
    id("com.google.devtools.ksp") version "2.3.9"
    kotlin("jvm") version "2.3.20"
}

group = "scot.oskar"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.hytale.com/release")
}

dependencies {
    compileOnly("gg.ginco:hytale-codec-annotations:1.1.0")
    implementation("gg.ginco:hytale-codec-runtime:1.1.0")
    ksp("gg.ginco:hytale-codec-processor:1.1.0")

    compileOnly("com.hypixel.hytale:Server:0.5.5")
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}