/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

import com.tajemniktv.tajsos.buildlogic.intConfig
import com.tajemniktv.tajsos.buildlogic.stringConfig
import com.tajemniktv.tajsos.buildlogic.tajsosBuild
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
}

val tajsos = tajsosBuild()

group = stringConfig("project.group", tajsos.groupId)
version = stringConfig("server.version", tajsos.serverVersion)

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(intConfig("java.toolchain", tajsos.javaToolchain)))
    }
}

val jvmTarget = stringConfig("kotlin.jvmTarget", tajsos.jvmTarget)
tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(jvmTarget))
}
