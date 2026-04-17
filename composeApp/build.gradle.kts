/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

@file:Suppress("KDocMissingDocumentation")

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.composeStabilityAnalyzer)
    alias(libs.plugins.kover)
}

composeCompiler {
    stabilityConfigurationFiles.add(layout.projectDirectory.file("../compose_compiler_config.conf"))
}

kotlin {
    jvmToolchain(25)
    // Android target configured via android block (replaces androidTarget + android{}) (Earlier: androidLibrary)
    android {
        namespace = "com.tajemniktv.tajsos.composeapp"
        compileSdk {
            version =
                release(
                    libs.versions.android.compileSdk
                        .get()
                        .toInt(),
                )
        }
        buildToolsVersion =
            libs.versions.android.buildTools
                .get()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_25)
        }

        // Required for Compose Multiplatform resources to be bundled into the AAR
        androidResources {
            enable = true
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "TajsOS"
            isStatic = true
        }
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.icons.core)
            implementation(libs.compose.icons.extended)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.haze.core)
            implementation(libs.haze.materials)
            implementation(libs.composeStabilityRuntime)
            implementation(projects.shared)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.logback)
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.firebase.auth.native)
                implementation(libs.firebase.firestore.native)
                implementation(libs.firebase.analytics.native)
                implementation(libs.firebase.common.native)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.tajemniktv.tajsos.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.tajemniktv.tajsos"
            packageVersion = "1.0.0"
            windows {
                iconFile.set(project.file("desktop-icons/tajsos-icon.ico"))
            }
            macOS {
                iconFile.set(project.file("desktop-icons/tajsos-icon.icns"))
            }
            linux {
                iconFile.set(project.file("desktop-icons/tajsos-icon.png"))
            }
        }
    }
}
