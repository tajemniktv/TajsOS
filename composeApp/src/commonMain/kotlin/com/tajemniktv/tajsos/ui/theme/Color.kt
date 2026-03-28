/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * TajsOS Color Palette - Neural Interface
 * Source of truth: DESIGN.md
 */
val PrimaryPurple = Color(0xFFBA9EFF)
val PrimaryPurpleDim = Color(0xFF8455EF)
val DeepBackground = Color(0xFF0E0E12)
val SidebarBackground = Color(0xFF000000)
val SurfaceDark = Color(0xFF0E0E12)
val SurfaceContainerLowest = Color(0xFF000000)
val SurfaceContainerLow = Color(0xFF131317)
val SurfaceContainerHigh = Color(0xFF1F1F24)
val SurfaceContainerHighest = Color(0xFF25252B)
val TextPrimary = Color(0xFFFCF8FE)
val TextMuted = Color(0xFFA8A2B3)
val AccentSuccess = Color(0xFF10B981)
val AccentError = Color(0xFFEF4444)
val AccentCyan = PrimaryPurpleDim
val SubtleBorder = Color(0xFF4F4A5D)
val OutlineVariant = Color(0xFF6B6580)

// Vault dashboard tokens
val VaultShell = SurfaceContainerLow
val VaultGradientStart = SurfaceContainerHigh
val VaultGradientMid = SurfaceContainerLow
val VaultGradientEnd = SurfaceContainerHighest
val VaultBorder = OutlineVariant.copy(alpha = 0.15f)
val VaultSoft = SurfaceContainerHighest
val VaultTextStrong = TextPrimary
val VaultTextSubtle = TextMuted
val VaultTextAccent = PrimaryPurple

// Material 3 mappings
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
