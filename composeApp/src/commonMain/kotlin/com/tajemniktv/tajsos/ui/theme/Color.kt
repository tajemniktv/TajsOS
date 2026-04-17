/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * TajsOS Color Palette - The "Neural Interface"
 *
 * This file contains the raw color definitions for the TajsOS design system.
 * Avoid using these directly in UI components; prefer [TajsOSTheme] tokens
 * to ensure reactive theme switching and consistency.
 *
 * Design principles in DESIGN.md
 */

internal val PrimaryPurple = Color(0xFFBA9EFF)
internal val PrimaryPurpleDim = Color(0xFF8455EF)
internal val DeepBackground = Color(0xFF0E0E12)
internal val DarkSidebarBackground = Color(0xFF000000)
internal val SurfaceDark = Color(0xFF0E0E12)
internal val SurfaceContainerLowest = Color(0xFF000000)
internal val SurfaceContainerLow = Color(0xFF131317)
internal val SurfaceContainerHigh = Color(0xFF1F1F24)
internal val SurfaceContainerHighest = Color(0xFF25252B)
internal val TextPrimary = Color(0xFFFCF8FE)
internal val TextMuted = Color(0xFFA8A2B3)

// Accents
val AccentMagenta = Color(0xFFFF6FAE)
val AccentAmber = Color(0xFFF4B740)
val AccentGreen = Color(0xFF30D158)
val AccentRed = Color(0xFFFF5A7A)
val AccentBlue = Color(0xFF6EA8FF)
val AccentCyan = Color(0xFF22D3EE)

// Standard Accent Options (matched with Settings UI)
val PaletteAccentPurple = Color(0xFFB388FF)
val PaletteAccentBlue = Color(0xFF60A5FA)
val PaletteAccentRose = Color(0xFFFB7185)
val PaletteAccentAmber = Color(0xFFFBBF24)
val PaletteAccentGreen = Color(0xFF34D399)

// Light Theme Palette - "High-Key Industrial"
internal val LightBackground = Color(0xFFF8F9FA)
internal val LightSurface = Color(0xFFFFFFFF)
internal val LightSurfaceLowest = Color(0xFFF1F3F5)
internal val LightSurfaceLow = Color(0xFFE9ECEF)
internal val LightSurfaceHigh = Color(0xFFDEE2E6)
internal val LightSurfaceHighest = Color(0xFFCED4DA)
internal val LightTextPrimary = Color(0xFF1A1A1E)
internal val LightTextMuted = Color(0xFF6C757D)
internal val LightBorder = Color(0xFFADB5BD).copy(alpha = 0.5f)
internal val LightOutlineVariant = Color(0xFFADB5BD).copy(alpha = 0.25f)

// Legacy aliases kept for compatibility with [TajsOSColors] mapping.
internal val AccentSuccess = AccentGreen
internal val AccentError = AccentRed
internal val SubtleBorder = Color(0xFF4F4A5D)
internal val OutlineVariant = Color(0xFF6B6580)
