/*
 * Copyright (c) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography defines the text styles used throughout TajsOS.
 * Following DESIGN.md principles.
 */
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default, // Space Grotesk equivalent
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default, // Space Grotesk equivalent
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif, // Outfit equivalent
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace, // JetBrains Mono equivalent
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 1.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default, // Space Grotesk equivalent
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    )
)
