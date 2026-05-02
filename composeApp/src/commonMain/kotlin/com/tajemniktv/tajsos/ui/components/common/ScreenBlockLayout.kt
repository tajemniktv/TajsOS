/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.common

/**
 * Shared simple surface model for screen-level block layouts.
 */
enum class ScreenBlockSurface {
    MOBILE,
    DESKTOP,
}

/**
 * Lightweight block plan for screen orchestrators.
 */
data class ScreenBlockPlan(
    val primary: List<String>,
    val secondary: List<String> = emptyList(),
)

/**
 * Builds a simple screen block plan from defaults.
 */
fun screenBlockPlan(
    surface: ScreenBlockSurface,
    mobile: List<String>,
    desktopPrimary: List<String> = mobile,
    desktopSecondary: List<String> = emptyList(),
): ScreenBlockPlan =
    when (surface) {
        ScreenBlockSurface.MOBILE -> {
            ScreenBlockPlan(primary = mobile)
        }

        ScreenBlockSurface.DESKTOP -> {
            ScreenBlockPlan(
                primary = desktopPrimary,
                secondary = desktopSecondary,
            )
        }
    }
