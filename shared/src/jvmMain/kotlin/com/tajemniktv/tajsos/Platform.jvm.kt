/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

/**
 * JVM-specific implementation of the generic [Platform] interface.
 *
 * Provides runtime information specific to desktop/JVM environments, exposing the Java version.
 */
class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()