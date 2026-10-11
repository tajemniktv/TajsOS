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
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeInput
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
 * Resolves a container color that turns transparent when glass is enabled.
 */
@Composable
fun glassContainerColor(fallback: Color): Color =
    if (LocalGlassSystem.current.enabled && LocalGlassSystem.current.hazeState != null) {
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
 * Shared glass-style chrome using source-backed Haze blur for shell and reusable cards.
 * Disabled glass bypasses the effect entirely; styles preserve the preset blur and theme tints.
 */
@Composable
fun Modifier.glassChrome(
    shape: Shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
    material: GlassMaterial = GlassMaterial.REGULAR,
    edgeWidth: Dp = 1.dp,
): Modifier {
    val glass = LocalGlassSystem.current
    if (!glass.enabled || glass.hazeState == null) return this

    val hazeState = glass.hazeState
    val topGlow = TajsOSTheme.Text.copy(alpha = 0.14f)
    val bottomShadow = Color.Transparent
    val topTint = TajsOSTheme.SurfaceHigh.copy(alpha = 0.42f)
    val bottomTint = TajsOSTheme.SurfaceLow.copy(alpha = 0.36f)
    val edgeColor = TajsOSTheme.GhostBorder.copy(alpha = 0.2f)
    val style =
        when (material) {
            GlassMaterial.ULTRA_THIN -> CupertinoMaterials.ultraThin(TajsOSTheme.SurfaceHigh)
            GlassMaterial.THIN -> CupertinoMaterials.thin(TajsOSTheme.SurfaceHigh)
            GlassMaterial.REGULAR -> CupertinoMaterials.regular(TajsOSTheme.SurfaceHigh)
            GlassMaterial.THICK -> CupertinoMaterials.thick(TajsOSTheme.SurfaceHigh)
        }

    val hazeModifier =
        Modifier.hazeBlur(
            input = HazeInput.Sources(hazeState),
            style = style.then {
                noiseFactor(0.22f)
                colorEffects(
                    listOf(
                        HazeColorEffect.tint(
                            TajsOSTheme.Text.copy(alpha = 0.08f),
                            blendMode = BlendMode.Screen,
                        ),
                        HazeColorEffect.tint(TajsOSTheme.SurfaceHigh.copy(alpha = 0.18f)),
                    ),
                )
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
