/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import platform.UIKit.UIDevice

/**
 * iOS-specific implementation of the generic [Platform] interface.
 *
 * Provides runtime information specific to iOS environments, exposing the device's system name and version.
 */
class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()