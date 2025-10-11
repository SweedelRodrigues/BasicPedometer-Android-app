// Top-level build file
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.8.1") // AGP version
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0") // match your Kotlin version
    }
}

// Optional: For settings plugins using alias if using version catalog
plugins {
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.application) apply false
}
