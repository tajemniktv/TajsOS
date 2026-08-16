/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.utils

import java.io.File

/**
 * Provides access to standard platform-specific directories for storing application data.
 *
 * Used primarily for locating database storage and configuration files outside the project working
 * directory to ensure user data persists across updates.
 */
object AppDirs {
    /**
     * Resolves and creates (if absent) the canonical TajsOS application data directory based on the host OS.
     *
     * Implementations logic:
     * - Windows: Uses %APPDATA% or defaults to `AppData/Roaming`.
     * - macOS: Uses `~/Library/Application Support`.
     * - Linux/Other: Follows XDG Base Directory specs (`$XDG_DATA_HOME` or defaults to `~/.local/share`).
     */
    fun getAppDataDir(): File {
        val os = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")

        val appDataPath = when {
            os.contains("win") -> {
                System.getenv("APPDATA") ?: "$userHome\\AppData\\Roaming"
            }
            os.contains("mac") -> {
                "$userHome/Library/Application Support"
            }
            else -> { // Linux and others
                val xdgDataHome = System.getenv("XDG_DATA_HOME")
                if (!xdgDataHome.isNullOrEmpty()) {
                    xdgDataHome
                } else {
                    "$userHome/.local/share"
                }
            }
        }

        val appDir = File(appDataPath, "TajsOS")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        return appDir
    }
}
