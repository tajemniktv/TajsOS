/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Design Tokens
 */
object TactileTheme {
    val Primary = PrimaryPurple
    val Background = DeepBackground
    val Surface = SurfaceDark
    val Text = TextPrimary
    val Muted = TextMuted
    val Success = AccentSuccess
    val Error = AccentError
    val Accent = AccentCyan
    val Border = SubtleBorder

    val RadiusXs = 2.dp
    val RadiusSm = 4.dp
    val RadiusMd = 8.dp
    val RadiusLg = 16.dp

    val SpacingXs = 4.dp
    val SpacingSm = 8.dp
    val SpacingMd = 16.dp
    val SpacingLg = 24.dp
    val SpacingXl = 32.dp
}

private val TajsDarkColorScheme =
    darkColorScheme(
        primary = TactileTheme.Primary,
        background = TactileTheme.Background,
        surface = TactileTheme.Surface,
        onPrimary = TactileTheme.Background,
        onBackground = TactileTheme.Text,
        onSurface = TactileTheme.Text,
        surfaceVariant = TactileTheme.Muted,
        onSurfaceVariant = TactileTheme.Text,
        error = TactileTheme.Error,
    )

private val TajsLightColorScheme =
    lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40,
    )

@Composable
fun TajsOSTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) TajsDarkColorScheme else TajsLightColorScheme
    val typography = tajsOSTypography()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content,
    )
}
