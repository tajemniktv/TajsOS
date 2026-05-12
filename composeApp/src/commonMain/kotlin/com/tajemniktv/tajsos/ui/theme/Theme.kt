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
    /**
     * Internal reactive state holding the current active palette.
     */
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
            sidebarBackground = DarkSidebarBackground,
        ),
    )

    /** Primary brand color. */
    val Primary: Color get() = currentColors.primary

    /** Dimmed version of the primary color. */
    val PrimaryDim: Color get() = currentColors.primaryDim

    /** Main application background. */
    val Background: Color get() = currentColors.background

    /** Standard surface container. */
    val Surface: Color get() = currentColors.surface

    /** Lowest emphasis surface (e.g. canvas). */
    val SurfaceLowest: Color get() = currentColors.surfaceLowest

    /** Low emphasis surface. */
    val SurfaceLow: Color get() = currentColors.surfaceLow

    /** High emphasis surface. */
    val SurfaceHigh: Color get() = currentColors.surfaceHigh

    /** Highest emphasis surface. */
    val SurfaceHighest: Color get() = currentColors.surfaceHighest

    /** Primary text color. */
    val Text: Color get() = currentColors.text

    /** Muted or disabled text/icon color. */
    val Muted: Color get() = currentColors.muted

    /** Brand accent magenta. */
    val AccentMagenta: Color get() = com.tajemniktv.tajsos.ui.theme.AccentMagenta

    /** Brand accent amber. */
    val AccentAmber: Color get() = com.tajemniktv.tajsos.ui.theme.AccentAmber

    /** Brand accent green. */
    val AccentGreen: Color get() = com.tajemniktv.tajsos.ui.theme.AccentGreen

    /** Brand accent red. */
    val AccentRed: Color get() = com.tajemniktv.tajsos.ui.theme.AccentRed

    /** Brand accent blue. */
    val AccentBlue: Color get() = com.tajemniktv.tajsos.ui.theme.AccentBlue

    /** Brand accent cyan. */
    val AccentCyan: Color get() = com.tajemniktv.tajsos.ui.theme.AccentCyan

    /** Success state color. */
    val Success: Color get() = currentColors.success

    /** Error state color. */
    val Error: Color get() = currentColors.error

    /** General accent color (defaults to primary). */
    val Accent: Color get() = currentColors.primary

    /** Standard border color. */
    val Border: Color get() = currentColors.border

    /** Very subtle border or ghost outline. */
    val GhostBorder: Color get() = currentColors.ghostBorder

    /** Background for the main screen canvas. */
    val ScreenCanvas: Color get() = currentColors.surfaceLowest

    /** Surface color for standard cards. */
    val CardSurface: Color get() = currentColors.surfaceLow

    /** Surface color for nested cards. */
    val CardNestedSurface: Color get() = currentColors.surfaceHighest

    /** Stroke color for cards. */
    val CardStroke: Color get() = currentColors.ghostBorder.copy(alpha = 0.55f)

    /** Shell background for vault components. */
    val VaultShell: Color get() = currentColors.surfaceLow

    /** Start color for vault gradients. */
    val VaultGradientStart: Color get() = currentColors.surfaceHigh

    /** Mid color for vault gradients. */
    val VaultGradientMid: Color get() = currentColors.surfaceLow

    /** End color for vault gradients. */
    val VaultGradientEnd: Color get() = currentColors.surfaceHighest

    /** Border color for vault components. */
    val VaultBorder: Color get() = currentColors.ghostBorder.copy(alpha = 0.15f)

    /** Soft background for vault surfaces. */
    val VaultSoft: Color get() = currentColors.surfaceHighest

    /** Strong text in vault views. */
    val VaultTextStrong: Color get() = currentColors.text

    /** Subtle text in vault views. */
    val VaultTextSubtle: Color get() = currentColors.muted

    /** Accent text in vault views. */
    val VaultTextAccent: Color get() = currentColors.primary

    /** Start color for calendar gradients. */
    val CalendarGradientStart: Color get() = currentColors.surfaceLowest

    /** Mid color for calendar gradients. */
    val CalendarGradientMid: Color get() = currentColors.surfaceLow

    /** End color for calendar gradients. */
    val CalendarGradientEnd: Color get() = currentColors.surfaceLowest

    /** Main calendar panel color. */
    val CalendarPanel: Color get() = currentColors.surfaceHigh.copy(alpha = 0.72f)

    /** Soft calendar panel color. */
    val CalendarPanelSoft: Color get() = currentColors.surfaceHigh.copy(alpha = 0.62f)

    /** Strong calendar panel color. */
    val CalendarPanelStrong: Color get() = currentColors.surfaceHighest.copy(alpha = 0.78f)

    /** Header text in calendar views. */
    val CalendarHeaderText: Color get() = currentColors.primary.copy(alpha = 0.9f)

    /** Background for selected days in calendar. */
    val CalendarSelectedDay: Color get() = currentColors.primary.copy(alpha = 0.2f)

    /** Background for today's day in calendar. */
    val CalendarTodayDay: Color get() = currentColors.primaryDim.copy(alpha = 0.16f)

    /** Background for idle/empty days in calendar. */
    val CalendarIdleDay: Color get() = currentColors.surfaceLow.copy(alpha = 0.85f)

    /** Strong emphasis for today's day in calendar. */
    val CalendarTodayDayStrong: Color get() = currentColors.primaryDim.copy(alpha = 0.35f)

    /** Text color for selected days in calendar. */
    val CalendarSelectedText: Color get() = currentColors.text

    /** Background color for the app sidebar. */
    val SidebarBackground: Color get() = currentColors.sidebarBackground

    /** Fixed width of the app sidebar in desktop layout. */
    val SidebarWidth = 280.dp

    /** Extra small corner radius. */
    val RadiusXs = 4.dp

    /** Small corner radius. */
    val RadiusSm = 8.dp

    /** Medium corner radius. */
    val RadiusMd = 12.dp

    /** Large corner radius. */
    val RadiusLg = 16.dp

    /** Extra large corner radius. */
    val RadiusXl = 24.dp

    /** 2x extra large corner radius. */
    val Radius2Xl = 32.dp

    /** Extra small spacing. */
    val SpacingXs = 4.dp

    /** Small spacing. */
    val SpacingSm = 8.dp

    /** Medium spacing. */
    val SpacingMd = 16.dp

    /** Large spacing. */
    val SpacingLg = 24.dp

    /** Extra large spacing. */
    val SpacingXl = 32.dp
}

/**
 * Design token palette definition.
 *
 * @property primary Primary brand color.
 * @property primaryDim Dimmed primary color.
 * @property background Global background.
 * @property surface Standard container surface.
 * @property surfaceLowest Lowest elevation surface.
 * @property surfaceLow Low elevation surface.
 * @property surfaceHigh High elevation surface.
 * @property surfaceHighest Highest elevation surface.
 * @property text Primary text.
 * @property muted Secondary/muted text.
 * @property border Standard border.
 * @property ghostBorder Subtle/ghost border.
 * @property success Success indicator color.
 * @property error Error indicator color.
 * @property sidebarBackground Sidebar background color.
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
 *
 * @param isDark Whether to use the dark theme palette.
 * @param accentColor The base color to derive primary tokens from.
 * @return A configured [TajsOSColors] instance.
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
            sidebarBackground = DarkSidebarBackground,
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

/**
 * TajsOS theme implementation using Material 3.
 *
 * @param darkTheme Whether to use dark theme.
 * @param accentColor The user-preferred accent color.
 * @param content The Composable content to apply the theme to.
 */
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
