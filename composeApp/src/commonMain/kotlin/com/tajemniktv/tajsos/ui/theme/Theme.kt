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
object TactileTheme
{
    val Primary = PrimaryPurple
    val Background = DeepBackground
    val Surface = SurfaceDark
    val Text = TextPrimary
    val Muted = TextMuted
    val Success = AccentSuccess
    val Error = AccentError
    val Accent = AccentCyan
    val Border = SubtleBorder
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

/**
 * Provides the app's Material 3 theme using the project's color tokens and typography.
 *
 * The selected color scheme depends on `darkTheme`; the composed `content` is rendered
 * within this themed Material composition.
 *
 * @param darkTheme When `true`, applies the dark color scheme; when `false`, applies the light color scheme.
 * @param content Composable content to be displayed with the applied theme.
 */
@Composable
fun TajsOSTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
)
{
    val colorScheme = if (darkTheme) TajsDarkColorScheme else TajsLightColorScheme
    val typography = tajsOSTypography()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content,
    )
}
