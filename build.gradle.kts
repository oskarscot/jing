plugins {
    id("gg.ginco.hygradle")
    id("com.google.devtools.ksp") version "2.3.9"
    kotlin("jvm") version "2.3.20"
}

group = "scot.oskar"
version = "0.0.1-SNAPSHOT"

val hytalePatchline = property("hytale.patchline") as String

repositories {
    mavenCentral()
    maven("https://maven.hytale.com/$hytalePatchline")
}

dependencies {
    api(kotlin("stdlib"))
    compileOnly("gg.ginco:hytale-codec-annotations:1.1.0")
    implementation("gg.ginco:hytale-codec-runtime:1.1.0")
    ksp("gg.ginco:hytale-codec-processor:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
}


hytale {
    group = "oskarscot"
    name = "Jing"
    description = "The Hytale Essencials"
    author("oskarscot")

    mainClass = "scot.oskar.jing.JingPlugin"
    serverVersion = "0.6.0-pre.5"

    includesAssetPack = false
    bundleDependencies = true
}

kotlin {
    jvmToolchain(25)
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}