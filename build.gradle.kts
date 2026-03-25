/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.sonarqube)
}

sonar {
    properties {
        property("sonar.projectKey", "tajemniktv_TajsOS")
        property("sonar.organization", "tajemniktv")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

subprojects {
    apply(plugin = "org.sonarqube")

    sonarqube {
        properties {
            property(
                "sonar.sources",
                "src/commonMain/kotlin,src/androidMain/kotlin,src/jvmMain/kotlin",
            )
            property(
                "sonar.tests",
                "src/commonTest/kotlin,src/androidTest/kotlin,src/jvmTest/kotlin",
            )
        }
    }
}
