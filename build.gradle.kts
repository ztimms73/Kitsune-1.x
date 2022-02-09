plugins {
    id("com.android.application") version BuildPluginsVersion.AGP apply false
    id("com.android.library") version BuildPluginsVersion.AGP apply false
    kotlin("android") version BuildPluginsVersion.KOTLIN apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven { setUrl("https://www.jitpack.io") }
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath(kotlin("serialization", version = BuildPluginsVersion.KOTLIN))
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}