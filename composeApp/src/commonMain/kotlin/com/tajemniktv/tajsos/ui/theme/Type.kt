/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import tajsos.composeapp.generated.resources.*

@Composable
fun spaceGroteskFontFamily() =
        FontFamily(
            Font(Res.font.SpaceGrotesk_Light, FontWeight.Light),
            Font(Res.font.SpaceGrotesk_Regular, FontWeight.Normal),
            Font(Res.font.SpaceGrotesk_Medium, FontWeight.Medium),
            Font(Res.font.SpaceGrotesk_Bold, FontWeight.Bold),
        )

@Composable
fun outfitFontFamily() =
        FontFamily(
            Font(Res.font.Outfit_Thin, FontWeight.Thin),
            Font(Res.font.Outfit_ExtraLight, FontWeight.ExtraLight),
            Font(Res.font.Outfit_Light, FontWeight.Light),
            Font(Res.font.Outfit_Regular, FontWeight.Normal),
            Font(Res.font.Outfit_Medium, FontWeight.Medium),
            Font(Res.font.Outfit_SemiBold, FontWeight.SemiBold),
            Font(Res.font.Outfit_Bold, FontWeight.Bold),
            Font(Res.font.Outfit_ExtraBold, FontWeight.ExtraBold),
            Font(Res.font.Outfit_Black, FontWeight.Black),
        )

@Composable
fun jetBrainsMonoFontFamily() =
        FontFamily(
            Font(Res.font.JetBrainsMono_Thin, FontWeight.Thin),
            Font(Res.font.JetBrainsMono_ExtraLight, FontWeight.ExtraLight),
            Font(Res.font.JetBrainsMono_Light, FontWeight.Light),
            Font(Res.font.JetBrainsMono_Regular, FontWeight.Normal),
            Font(Res.font.JetBrainsMono_Medium, FontWeight.Medium),
            Font(Res.font.JetBrainsMono_SemiBold, FontWeight.SemiBold),
            Font(Res.font.JetBrainsMono_Bold, FontWeight.Bold),
            Font(Res.font.JetBrainsMono_ExtraBold, FontWeight.ExtraBold),
        )

/**
 * Typography defines the text styles used throughout TajsOS.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun tajsOSTypography(): Typography
{
    val spaceGrotesk = spaceGroteskFontFamily()
    val outfit = outfitFontFamily()
    val jetBrainsMono = jetBrainsMonoFontFamily()

    return Typography(
        displayLarge = TextStyle(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
        ),
        displayMedium = TextStyle(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
        ),
        displaySmall = TextStyle(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Bold,
        ),
        headlineLarge = TextStyle(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Bold,
        ),
        headlineMedium = TextStyle(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Bold,
        ),
        headlineSmall = TextStyle(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Bold,
        ),
        titleLarge = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.SemiBold,
        ),
        titleMedium = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.SemiBold,
        ),
        titleSmall = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.SemiBold,
        ),
        bodyLarge = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.Normal,
        ),
        bodySmall = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.Normal,
        ),
        labelLarge = TextStyle(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Medium,
        ),
        labelSmall = TextStyle(
            fontFamily = jetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
        ),
        // Expressive variants
        displayLargeEmphasized = TextStyle(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Bold,
        ),
        displayMediumEmphasized = TextStyle(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Bold,
        ),
        displaySmallEmphasized = TextStyle(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Bold,
        ),
        headlineLargeEmphasized = TextStyle(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Bold,
        ),
        headlineMediumEmphasized = TextStyle(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Bold,
        ),
        headlineSmallEmphasized = TextStyle(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Bold,
        ),
        titleLargeEmphasized = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.Bold,
        ),
        titleMediumEmphasized = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.Bold,
        ),
        titleSmallEmphasized = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.Bold,
        ),
        bodyLargeEmphasized = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.Bold,
        ),
        bodyMediumEmphasized = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.Bold,
        ),
        bodySmallEmphasized = TextStyle(
            fontFamily = outfit,
            fontWeight = FontWeight.Bold,
        ),
        labelLargeEmphasized = TextStyle(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Bold,
        ),
        labelMediumEmphasized = TextStyle(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Bold,
        ),
        labelSmallEmphasized = TextStyle(
            fontFamily = jetBrainsMono,
            fontWeight = FontWeight.Bold,
        ),
    )
}
