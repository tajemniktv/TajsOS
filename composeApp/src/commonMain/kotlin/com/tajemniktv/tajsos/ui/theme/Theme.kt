/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Design Tokens
 */
object TactileTheme {
    private var palette: TactilePalette by mutableStateOf(TactilePalette.dark())

    val Primary: Color get() = palette.primary
    val Background: Color get() = palette.background
    val Surface: Color get() = palette.surface
    val Text: Color get() = palette.text
    val Muted: Color get() = palette.muted
    val Success: Color get() = palette.success
    val Error: Color get() = palette.error
    val Accent: Color get() = palette.accent
    val Border: Color get() = palette.border
    val SidebarBackground: Color get() = palette.sidebarBackground
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

    internal fun setDarkThemeEnabled(enabled: Boolean) {
        palette = if (enabled) TactilePalette.dark() else TactilePalette.light()
    }
}

private data class TactilePalette(
    val primary: Color,
    val background: Color,
    val surface: Color,
    val text: Color,
    val muted: Color,
    val success: Color,
    val error: Color,
    val accent: Color,
    val border: Color,
    val sidebarBackground: Color,
) {
    companion object {
        fun dark() =
            TactilePalette(
                primary = PrimaryPurple,
                background = DeepBackground,
                surface = SurfaceDark,
                text = TextPrimary,
                muted = TextMuted,
                success = AccentSuccess,
                error = AccentError,
                accent = AccentCyan,
                border = SubtleBorder,
                sidebarBackground = com.tajemniktv.tajsos.ui.theme.SidebarBackground,
            )

        fun light() =
            TactilePalette(
                primary = Purple40,
                background = LightBackground,
                surface = LightSurface,
                text = LightTextPrimary,
                muted = LightTextMuted,
                success = AccentSuccess,
                error = AccentError,
                accent = AccentCyan,
                border = LightSubtleBorder,
                sidebarBackground = LightSidebarBackground,
            )
    }
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
        background = LightBackground,
        surface = LightSurface,
        onPrimary = Color.White,
        onBackground = LightTextPrimary,
        onSurface = LightTextPrimary,
        surfaceVariant = LightSidebarBackground,
        onSurfaceVariant = LightTextMuted,
        error = AccentError,
    )

@Composable
internal fun TajsOSTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) TajsDarkColorScheme else TajsLightColorScheme
    val typography = tajsOSTypography()
    SideEffect { TactileTheme.setDarkThemeEnabled(darkTheme) }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content,
    )
}
