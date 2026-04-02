/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Design Tokens - Reactive accessor for the current TajsOS theme colors.
 */
object TajsOSTheme {
    internal var currentColors by mutableStateOf(
        TajsOSColors(
            primary = PrimaryPurple,
            primaryDim = PrimaryPurpleDim,
            background = DeepBackground,
            surface = SurfaceDark,
            surfaceLowest = SurfaceContainerLowest,
            surfaceLow = SurfaceContainerLow,
            surfaceHigh = SurfaceContainerHigh,
            surfaceHighest = SurfaceContainerHighest,
            text = TextPrimary,
            muted = TextMuted,
            border = SubtleBorder,
            ghostBorder = OutlineVariant,
            success = AccentSuccess,
            error = AccentError,
            sidebarBackground = SidebarBackground,
        ),
    )

    val Primary: Color get() = currentColors.primary
    val PrimaryDim: Color get() = currentColors.primaryDim
    val Background: Color get() = currentColors.background
    val Surface: Color get() = currentColors.surface
    val SurfaceLowest: Color get() = currentColors.surfaceLowest
    val SurfaceLow: Color get() = currentColors.surfaceLow
    val SurfaceHigh: Color get() = currentColors.surfaceHigh
    val SurfaceHighest: Color get() = currentColors.surfaceHighest
    val Text: Color get() = currentColors.text
    val Muted: Color get() = currentColors.muted
    val AccentMagenta: Color get() = com.tajemniktv.tajsos.ui.theme.AccentMagenta
    val AccentAmber: Color get() = com.tajemniktv.tajsos.ui.theme.AccentAmber
    val AccentGreen: Color get() = com.tajemniktv.tajsos.ui.theme.AccentGreen
    val AccentRed: Color get() = com.tajemniktv.tajsos.ui.theme.AccentRed
    val AccentBlue: Color get() = com.tajemniktv.tajsos.ui.theme.AccentBlue
    val AccentCyan: Color get() = com.tajemniktv.tajsos.ui.theme.AccentCyan
    val Success: Color get() = currentColors.success
    val Error: Color get() = currentColors.error
    val Accent: Color get() = currentColors.primary
    val Border: Color get() = currentColors.border
    val GhostBorder: Color get() = currentColors.ghostBorder
    val VaultShell: Color get() = currentColors.surfaceLow
    val VaultGradientStart: Color get() = currentColors.surfaceHigh
    val VaultGradientMid: Color get() = currentColors.surfaceLow
    val VaultGradientEnd: Color get() = currentColors.surfaceHighest
    val VaultBorder: Color get() = currentColors.ghostBorder.copy(alpha = 0.15f)
    val VaultSoft: Color get() = currentColors.surfaceHighest
    val VaultTextStrong: Color get() = currentColors.text
    val VaultTextSubtle: Color get() = currentColors.muted
    val VaultTextAccent: Color get() = currentColors.primary
    val CalendarGradientStart: Color get() = currentColors.surfaceLowest
    val CalendarGradientMid: Color get() = currentColors.surfaceLow
    val CalendarGradientEnd: Color get() = currentColors.surfaceLowest
    val CalendarPanel: Color get() = currentColors.surfaceHigh.copy(alpha = 0.72f)
    val CalendarPanelSoft: Color get() = currentColors.surfaceHigh.copy(alpha = 0.62f)
    val CalendarPanelStrong: Color get() = currentColors.surfaceHighest.copy(alpha = 0.78f)
    val CalendarHeaderText: Color get() = currentColors.primary.copy(alpha = 0.9f)
    val CalendarSelectedDay: Color get() = currentColors.primary.copy(alpha = 0.2f)
    val CalendarTodayDay: Color get() = currentColors.primaryDim.copy(alpha = 0.16f)
    val CalendarIdleDay: Color get() = currentColors.surfaceLow.copy(alpha = 0.85f)
    val CalendarTodayDayStrong: Color get() = currentColors.primaryDim.copy(alpha = 0.35f)
    val CalendarSelectedText: Color get() = currentColors.text
    val SidebarBackground: Color get() = currentColors.sidebarBackground
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

/**
 * Short alias for TajsOSTheme.
 */
val TajsTheme = TajsOSTheme

/**
 * Design token palette definition.
 */
data class TajsOSColors(
    val primary: Color,
    val primaryDim: Color,
    val background: Color,
    val surface: Color,
    val surfaceLowest: Color,
    val surfaceLow: Color,
    val surfaceHigh: Color,
    val surfaceHighest: Color,
    val text: Color,
    val muted: Color,
    val border: Color,
    val ghostBorder: Color,
    val success: Color,
    val error: Color,
    val sidebarBackground: Color,
)

/**
 * Builds a [TajsOSColors] instance based on theme and accent preferences.
 */
fun buildTajsOSColors(
    isDark: Boolean,
    accentColor: Color,
): TajsOSColors =
    if (isDark) {
        TajsOSColors(
            primary = accentColor,
            primaryDim = accentColor.copy(alpha = 0.6f),
            background = DeepBackground,
            surface = SurfaceDark,
            surfaceLowest = SurfaceContainerLowest,
            surfaceLow = SurfaceContainerLow,
            surfaceHigh = SurfaceContainerHigh,
            surfaceHighest = SurfaceContainerHighest,
            text = TextPrimary,
            muted = TextMuted,
            border = SubtleBorder,
            ghostBorder = OutlineVariant,
            success = AccentSuccess,
            error = AccentError,
            sidebarBackground = SidebarBackground,
        )
    } else {
        TajsOSColors(
            primary = accentColor,
            primaryDim = accentColor.copy(alpha = 0.7f),
            background = LightBackground,
            surface = LightSurface,
            surfaceLowest = LightSurfaceLowest,
            surfaceLow = LightSurfaceLow,
            surfaceHigh = LightSurfaceHigh,
            surfaceHighest = LightSurfaceHighest,
            text = LightTextPrimary,
            muted = LightTextMuted,
            border = LightBorder,
            ghostBorder = LightOutlineVariant,
            success = AccentSuccess,
            error = AccentError,
            sidebarBackground = LightSurfaceLow,
        )
    }

@Composable
internal fun TajsOSTheme(
    darkTheme: Boolean = true,
    accentColor: Color = PrimaryPurple,
    content: @Composable () -> Unit,
) {
    val tajsOSColors = buildTajsOSColors(darkTheme, accentColor)

    // Update the reactive singleton immediately so descendants see the new colors
    // during this same composition pass.
    TajsOSTheme.currentColors = tajsOSColors

    val colorScheme =
        if (darkTheme) {
            darkColorScheme(
                primary = tajsOSColors.primary,
                primaryContainer = tajsOSColors.primaryDim,
                secondary = tajsOSColors.primaryDim,
                background = tajsOSColors.background,
                surface = tajsOSColors.surface,
                surfaceDim = tajsOSColors.surfaceLow,
                surfaceBright = tajsOSColors.surfaceHigh,
                surfaceContainerLowest = tajsOSColors.surfaceLowest,
                surfaceContainerLow = tajsOSColors.surfaceLow,
                surfaceContainer = tajsOSColors.surface,
                surfaceContainerHigh = tajsOSColors.surfaceHigh,
                surfaceContainerHighest = tajsOSColors.surfaceHighest,
                onPrimary = tajsOSColors.background,
                onBackground = tajsOSColors.text,
                onSurface = tajsOSColors.text,
                surfaceVariant = tajsOSColors.muted,
                onSurfaceVariant = tajsOSColors.text,
                outlineVariant = tajsOSColors.ghostBorder,
                outline = tajsOSColors.border,
                error = tajsOSColors.error,
            )
        } else {
            lightColorScheme(
                primary = tajsOSColors.primary,
                primaryContainer = tajsOSColors.primaryDim,
                secondary = tajsOSColors.primaryDim,
                background = tajsOSColors.background,
                surface = tajsOSColors.surface,
                surfaceDim = tajsOSColors.surfaceLow,
                surfaceBright = tajsOSColors.surfaceHigh,
                surfaceContainerLowest = tajsOSColors.surfaceLowest,
                surfaceContainerLow = tajsOSColors.surfaceLow,
                surfaceContainer = tajsOSColors.surface,
                surfaceContainerHigh = tajsOSColors.surfaceHigh,
                surfaceContainerHighest = tajsOSColors.surfaceHighest,
                onPrimary = tajsOSColors.background,
                onBackground = tajsOSColors.text,
                onSurface = tajsOSColors.text,
                surfaceVariant = tajsOSColors.muted,
                onSurfaceVariant = tajsOSColors.text,
                outlineVariant = tajsOSColors.ghostBorder,
                outline = tajsOSColors.border,
                error = tajsOSColors.error,
            )
        }
    val typography = tajsOSTypography()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content,
    )
}
