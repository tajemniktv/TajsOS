/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.app.distribution)
    alias(libs.plugins.firebase.perf)
}

composeCompiler {
    stabilityConfigurationFiles.add(layout.projectDirectory.file("../compose_compiler_config.conf"))
}

android {
    signingConfigs {
        create("release") {
        }
    }
    namespace = "com.tajemniktv.tajsos"
    compileSdk {
        version =
            release(
                libs.versions.android.compileSdk
                    .get()
                    .toInt(),
            ) {
                minorApiLevel =
                    libs.versions.android.compileSdkMinor
                        .get()
                        .toInt()
            }
    }

    defaultConfig {
        applicationId = "com.tajemniktv.tajsos"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0.0"
        signingConfig = signingConfigs.getByName("debug")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Firebase App Distribution configuration for release builds
            configure<com.google.firebase.appdistribution.gradle.AppDistributionExtension> {
                artifactType = "APK"
                // You can add 'testers' or 'groups' here if you have them in the Firebase console
                // groups = "qa-team"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
    buildToolsVersion =
        libs.versions.android.buildTools
            .get()
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(projects.composeApp)
    implementation(projects.shared)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.native)
    implementation(libs.firebase.firestore.native)
    implementation(libs.firebase.analytics.native)
    implementation(libs.firebase.common.native)
    implementation(libs.firebase.crashlytics.native)
    implementation(libs.firebase.perf.native)
    implementation(libs.firebase.config.native)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.datastore.preferences.core)
    implementation(libs.compose.ui)
    implementation(libs.compose.icons.core)
    implementation(libs.compose.icons.extended)
    implementation(libs.compose.material3)
    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}
