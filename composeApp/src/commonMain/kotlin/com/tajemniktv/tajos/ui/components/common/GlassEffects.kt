/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajos.ui.theme.TajsOSTheme
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.materials.CupertinoMaterials

/**
 * Global glass rendering configuration.
 */
@Immutable
data class GlassSystemConfig(
    val enabled: Boolean = true,
    val hazeState: HazeState? = null,
)

/**
 * Material presets for glass surfaces.
 */
enum class GlassMaterial {
    CHROME,
    ULTRA_THIN,
    THIN,
    REGULAR,
    THICK,
}

/**
 * Composition-local glass configuration used by shell and reusable components.
 */
val LocalGlassSystem = compositionLocalOf { GlassSystemConfig(enabled = false) }

/**
 * Provides the current glass system configuration to descendants.
 */
@Composable
fun ProvideGlassSystem(
    enabled: Boolean,
    hazeState: HazeState?,
    content: @Composable () -> Unit,
) {
    val config =
        remember(enabled, hazeState) { GlassSystemConfig(enabled = enabled, hazeState = hazeState) }
    CompositionLocalProvider(LocalGlassSystem provides config) {
        content()
    }
}

/**
 * Resolves a transparent container only for an intentional glass surface.
 * Ordinary cards retain their tonal surface colors.
 */
@Composable
fun glassContainerColor(fallback: Color, active: Boolean): Color =
    if (active && LocalGlassSystem.current.enabled && LocalGlassSystem.current.hazeState != null) {
        Color.Transparent
    } else {
        fallback
    }

/**
 * Whether glass is enabled in the current composition.
 */
@Composable
fun isGlassEnabled(): Boolean {
    val glass = LocalGlassSystem.current
    return glass.enabled && glass.hazeState != null
}

/**
 * Shared glass-style chrome for shell panels and overlapping surfaces.
 * Inactive or disabled glass bypasses the effect so ordinary cards retain tonal depth.
 */
@Composable
fun Modifier.glassChrome(
    shape: Shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
    material: GlassMaterial = GlassMaterial.REGULAR,
    edgeWidth: Dp = 1.dp,
    active: Boolean,
): Modifier {
    val glass = LocalGlassSystem.current
    if (!active || !glass.enabled || glass.hazeState == null) return this

    val hazeState = glass.hazeState
    val shellChrome = material == GlassMaterial.CHROME
    val topGlow =
        if (shellChrome) TajsOSTheme.Primary.copy(alpha = 0.05f)
        else TajsOSTheme.Text.copy(alpha = 0.14f)
    val bottomShadow = Color.Transparent
    val topTint =
        if (shellChrome) TajsOSTheme.PrimaryDim.copy(alpha = 0.04f)
        else TajsOSTheme.SurfaceHigh.copy(alpha = 0.20f)
    val bottomTint =
        if (shellChrome) TajsOSTheme.SurfaceLow.copy(alpha = 0.10f)
        else TajsOSTheme.SurfaceLow.copy(alpha = 0.18f)
    val edgeColor = TajsOSTheme.GhostBorder.copy(alpha = 0.2f)
    val style =
        when (material) {
            GlassMaterial.CHROME ->
                HazeBlurStyle {
                    blurRadius(24.dp)
                    noiseFactor(0.04f)
                    backgroundColor(TajsOSTheme.Background)
                    colorEffects(listOf(HazeColorEffect.tint(TajsOSTheme.Background.copy(alpha = 0.38f))))
                }
            GlassMaterial.ULTRA_THIN -> CupertinoMaterials.ultraThin(TajsOSTheme.SurfaceHigh)
            GlassMaterial.THIN -> CupertinoMaterials.thin(TajsOSTheme.SurfaceHigh)
            GlassMaterial.REGULAR -> CupertinoMaterials.regular(TajsOSTheme.SurfaceHigh)
            GlassMaterial.THICK -> CupertinoMaterials.thick(TajsOSTheme.SurfaceHigh)
        }

    val hazeModifier =
        Modifier.hazeBlur(
            input = HazeInput.Backdrop(hazeState),
            style =
                if (shellChrome) {
                    style
                } else {
                    style.then {
                        noiseFactor(0.08f)
                        colorEffects(
                            listOf(
                                HazeColorEffect.tint(
                                    TajsOSTheme.Text.copy(alpha = 0.04f),
                                    blendMode = BlendMode.Screen,
                                ),
                                HazeColorEffect.tint(TajsOSTheme.SurfaceHigh.copy(alpha = 0.10f)),
                            ),
                        )
                    }
                },
        )

    return this
        .clip(shape)
        .then(hazeModifier)
        .background(
            brush =
                Brush.verticalGradient(
                    colors = listOf(topTint, bottomTint),
                ),
        ).drawWithContent {
            drawContent()
            drawRect(
                brush =
                    Brush.verticalGradient(
                        colors = listOf(topGlow, bottomShadow),
                    ),
            )
        }.border(
            width = edgeWidth,
            color = edgeColor,
            shape = shape,
        )
}
