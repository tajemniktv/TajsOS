/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

import com.tajemniktv.tajsos.buildlogic.intConfig
import com.tajemniktv.tajsos.buildlogic.stringConfig
import com.tajemniktv.tajsos.buildlogic.tajsosBuild
import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val tajsos = tajsosBuild()

android {
    compileSdk = intConfig("android.compileSdk", tajsos.compileSdk)
    buildToolsVersion = stringConfig("android.buildTools", tajsos.buildToolsVersion)

    defaultConfig {
        minSdk = intConfig("android.minSdk", tajsos.minSdk)
        targetSdk = intConfig("android.targetSdk", tajsos.targetSdk)
        versionCode = intConfig("android.versionCode", tajsos.versionCode)
        versionName = stringConfig("project.versionName", tajsos.versionName)
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    val javaVersion = JavaVersion.toVersion(intConfig("java.toolchain", tajsos.javaToolchain))
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
}

composeCompiler {
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose_compiler_config.conf"))
}

val jvmTarget = stringConfig("kotlin.jvmTarget", tajsos.jvmTarget)
tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(jvmTarget))
}
