/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

/**
 * Defines the contract for providing platform-specific information in a Multiplatform environment.
 */
interface Platform {
    /**
     * The name and potentially version of the underlying platform (e.g., "Android 31", "Java 17").
     */
    val name: String
}

/**
 * Retrieves the platform implementation for the current execution environment.
 *
 * This function is expected to be implemented in platform-specific source sets (e.g., androidMain, iosMain).
 *
 * @return An instance of [Platform] representing the environment.
 */
expect fun getPlatform(): Platform
