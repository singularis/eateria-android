// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // AGP 9 built-in Kotlin — do not apply org.jetbrains.kotlin.android
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.google.services) apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.0" apply false
}