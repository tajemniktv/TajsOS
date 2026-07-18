/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import android.os.Build

/**
 * Android-specific implementation of the generic [Platform] interface.
 *
 * Provides runtime information specific to Android environments, exposing the device's SDK version.
 */
class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()
