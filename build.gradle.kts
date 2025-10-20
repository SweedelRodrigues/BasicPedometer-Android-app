buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.13.0") // AGP 8.8.1
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0") // match your Kotlin version
        classpath("com.google.gms:google-services:4.4.2")


    }
}

plugins {
    // if using alias from libs.versions.toml, make sure it points to the correct version
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false

}
