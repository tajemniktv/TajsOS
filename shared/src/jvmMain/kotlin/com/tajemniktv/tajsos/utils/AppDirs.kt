/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.utils

import java.io.File

object AppDirs {
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
