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
    val PrimaryDim = PrimaryPurpleDim
    val Background = DeepBackground
    val Surface = SurfaceDark
    val SurfaceLowest = SurfaceContainerLowest
    val SurfaceLow = SurfaceContainerLow
    val SurfaceHigh = SurfaceContainerHigh
    val SurfaceHighest = SurfaceContainerHighest
    val Text = TextPrimary
    val Muted = TextMuted
    val Success = AccentSuccess
    val Error = AccentError
    val Accent = AccentCyan
    val Border = SubtleBorder
    val GhostBorder = OutlineVariant
    val VaultShell = com.tajemniktv.tajsos.ui.theme.VaultShell
    val VaultGradientStart = com.tajemniktv.tajsos.ui.theme.VaultGradientStart
    val VaultGradientMid = com.tajemniktv.tajsos.ui.theme.VaultGradientMid
    val VaultGradientEnd = com.tajemniktv.tajsos.ui.theme.VaultGradientEnd
    val VaultBorder = com.tajemniktv.tajsos.ui.theme.VaultBorder
    val VaultSoft = com.tajemniktv.tajsos.ui.theme.VaultSoft
    val VaultTextStrong = com.tajemniktv.tajsos.ui.theme.VaultTextStrong
    val VaultTextSubtle = com.tajemniktv.tajsos.ui.theme.VaultTextSubtle
    val VaultTextAccent = com.tajemniktv.tajsos.ui.theme.VaultTextAccent
    val CalendarGradientStart = SurfaceContainerLowest
    val CalendarGradientMid = SurfaceContainerLow
    val CalendarGradientEnd = SurfaceContainerLowest
    val CalendarPanel = SurfaceContainerHigh.copy(alpha = 0.72f)
    val CalendarPanelSoft = SurfaceContainerHigh.copy(alpha = 0.62f)
    val CalendarPanelStrong = SurfaceContainerHighest.copy(alpha = 0.78f)
    val CalendarHeaderText = Primary.copy(alpha = 0.9f)
    val CalendarSelectedDay = Primary.copy(alpha = 0.2f)
    val CalendarTodayDay = PrimaryDim.copy(alpha = 0.16f)
    val CalendarIdleDay = SurfaceContainerLow.copy(alpha = 0.85f)
    val CalendarSelectedText = Text
    val SidebarBackground = com.tajemniktv.tajsos.ui.theme.SidebarBackground
    val SidebarWidth = 280.dp

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
        primaryContainer = TactileTheme.PrimaryDim,
        secondary = TactileTheme.PrimaryDim,
        background = TactileTheme.Background,
        surface = TactileTheme.Surface,
        surfaceDim = TactileTheme.SurfaceLow,
        surfaceBright = TactileTheme.SurfaceHigh,
        surfaceContainerLowest = TactileTheme.SurfaceLowest,
        surfaceContainerLow = TactileTheme.SurfaceLow,
        surfaceContainer = TactileTheme.Surface,
        surfaceContainerHigh = TactileTheme.SurfaceHigh,
        surfaceContainerHighest = TactileTheme.SurfaceHighest,
        onPrimary = TactileTheme.Background,
        onBackground = TactileTheme.Text,
        onSurface = TactileTheme.Text,
        surfaceVariant = TactileTheme.Muted,
        onSurfaceVariant = TactileTheme.Text,
        outlineVariant = TactileTheme.GhostBorder,
        outline = TactileTheme.Border,
        error = TactileTheme.Error,
    )

private val TajsLightColorScheme =
    lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40,
    )

@Composable
internal fun TajsOSTheme(
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
