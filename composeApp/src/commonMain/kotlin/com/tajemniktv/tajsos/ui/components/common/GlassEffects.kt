/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi

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
val LocalGlassSystem = compositionLocalOf { GlassSystemConfig(enabled = false, hazeState = null) }

/**
 * Provides the current glass system configuration to descendants.
 */
@Composable
fun ProvideGlassSystem(
    enabled: Boolean,
    hazeState: HazeState?,
    content: @Composable () -> Unit,
) {
    val config = remember(enabled, hazeState) { GlassSystemConfig(enabled = enabled, hazeState = hazeState) }
    CompositionLocalProvider(LocalGlassSystem provides config) {
        content()
    }
}

/**
 * Resolves a container color that turns transparent when glass is enabled.
 */
@Composable
fun glassContainerColor(fallback: Color): Color =
    if (LocalGlassSystem.current.enabled) {
        Color.Transparent
    } else {
        fallback
    }

/**
 * Whether glass is enabled in the current composition.
 */
@Composable
fun isGlassEnabled(): Boolean = LocalGlassSystem.current.enabled

/**
 * Shared glass-style chrome for shell and reusable cards.
 */
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun Modifier.glassChrome(
    shape: Shape = RoundedCornerShape(16.dp),
    material: GlassMaterial = GlassMaterial.REGULAR,
    edgeWidth: Dp = 1.dp,
    progressive: HazeProgressive? =
        HazeProgressive.verticalGradient(
            startIntensity = 0.35f,
            endIntensity = 1f,
            preferPerformance = true,
        ),
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
        when (material)
        {
            GlassMaterial.ULTRA_THIN -> CupertinoMaterials.ultraThin(TajsOSTheme.SurfaceHigh)
            GlassMaterial.THIN -> CupertinoMaterials.thin(TajsOSTheme.SurfaceHigh)
            GlassMaterial.REGULAR -> CupertinoMaterials.regular(TajsOSTheme.SurfaceHigh)
            GlassMaterial.THICK -> CupertinoMaterials.thick(TajsOSTheme.SurfaceHigh)
        }

    val hazeModifier =
        Modifier.hazeEffect(
            state = hazeState,
            style = style,
            block = {
                noiseFactor = 0.22f
                if (progressive != null) {
                    this.progressive = progressive
                }
                tints =
                    listOf(
                        HazeTint(TajsOSTheme.Text.copy(alpha = 0.08f), blendMode = BlendMode.Screen),
                        HazeTint(TajsOSTheme.SurfaceHigh.copy(alpha = 0.18f)),
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
        )
        .drawWithContent {
            drawContent()
            drawRect(
                brush =
                    Brush.verticalGradient(
                        colors = listOf(topGlow, bottomShadow),
                    ),
            )
        }
        .border(
            width = edgeWidth,
            color = edgeColor,
            shape = shape,
        )
}
