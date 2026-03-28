/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

/**
 * A simple greeting class that generates a localized platform-specific greeting message.
 */
class Greeting {
    private val platform = getPlatform()

    /**
     * Generates a greeting string incorporating the name of the current platform.
     *
     * @return A greeting message containing the platform name (e.g., "Hello, Android!").
     */
    fun greet(): String = "Hello, ${platform.name}!"
}
