/*
 * Copyright (c) TajemnikTV 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Design Tokens as defined in DESIGN.md.
 */
object TactileTheme {
    val Primary = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.PrimaryPurple
    val Background = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.DeepBackground
    val Surface = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.SurfaceDark
    val Text = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TextPrimary
    val Muted = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TextMuted
    val Success = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.AccentSuccess
    val Error = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.AccentError
    val Accent = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.AccentCyan
    val Border = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.SubtleBorder

    val RadiusXs = 2.dp
    val RadiusSm = 4.dp
    val RadiusMd = 8.dp
    val RadiusLg = 16.dp

    val SpacingSm = 8.dp
    val SpacingMd = 16.dp
    val SpacingLg = 24.dp
    val SpacingXl = 32.dp
}

/**
 * TajsDarkColorScheme implements the core "control center" / sci-fi aesthetic.
 */
private val TajsDarkColorScheme = darkColorScheme(
    primary = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary,
    background = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Background,
    surface = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Surface,
    onPrimary = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Background,
    onBackground = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text,
    onSurface = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text,
    surfaceVariant = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted,
    onSurfaceVariant = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text,
    error = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Error
)

/**
 * Placeholder Light Color Scheme if needed for extreme sunlight environments.
 */
private val TajsLightColorScheme = lightColorScheme(
    primary = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.Purple40,
    secondary = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.PurpleGrey40,
    tertiary = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.Pink40
)

/**
 * TajsOSTheme is the main Material 3 theme wrapper for the app.
 * It defaults to a custom Dark theme, as per our ADHD-friendly/sleek design rules.
 */
@Composable
fun TajsOSTheme(
    darkTheme: Boolean = true, // Default to dark as per AGENTS.md
    // Dynamic color is available on Android 12+ but we disable it by default
    // to maintain the specific brand identity and visual stability of TajsOS.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && true -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TajsDarkColorScheme
        else -> _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TajsLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.Typography,
        content = content
    )
}
