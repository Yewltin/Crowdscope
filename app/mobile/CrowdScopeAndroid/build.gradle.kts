// build.gradle.kts (Project: CrowdScopeAndroid)

plugins {
    // These apply the plugins globally, but don't configure repositories.
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
}

// Task to clean the build directory
// Corrected to use layout.buildDirectory for modern Gradle
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

tasks.register("printOffline") {
    doLast {
        println("Gradle offline? " + gradle.startParameter.isOffline)
    }
}