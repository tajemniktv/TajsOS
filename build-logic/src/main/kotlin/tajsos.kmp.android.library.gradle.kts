/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

import com.tajemniktv.tajsos.buildlogic.intConfig
import com.tajemniktv.tajsos.buildlogic.stringConfig
import com.tajemniktv.tajsos.buildlogic.tajsosBuild
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

val tajsos = tajsosBuild()

kotlin {
    android {
        compileSdk = intConfig("android.compileSdk", tajsos.compileSdk)
        minSdk = intConfig("android.minSdk", tajsos.minSdk)

        compilerOptions {
            jvmTarget.set(
                JvmTarget.fromTarget(stringConfig("kotlin.jvmTarget", tajsos.jvmTarget)),
            )
        }
    }
}

val jvmTarget = stringConfig("kotlin.jvmTarget", tajsos.jvmTarget)
tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(jvmTarget))
}
