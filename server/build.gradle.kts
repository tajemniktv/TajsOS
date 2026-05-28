/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinx.serialization)
    application
}

group = "com.tajemniktv.tajsos"
version = "1.0.0"
application {
    mainClass.set("com.tajemniktv.tajsos.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf(
        "-Dio.ktor.development=$isDevelopment",
        "-Dktor.application.version=${project.version}"
    )
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    val ktorVersion = libs.versions.ktor.get()

    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverAuth)
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation(libs.ktor.serialization.kotlinx.json)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.ktor.client.content.negotiation)
}


tasks.withType<Tar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Zip> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
