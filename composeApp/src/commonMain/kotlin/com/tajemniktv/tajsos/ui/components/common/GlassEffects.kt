/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

/**
 * Shared glass-style chrome for shell and floating controls.
 *
 * @param hazeState Shared haze state from the shell.
 * @param shape Shape of the glass surface.
 * @param edgeWidth Width of the ghost border.
 */
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun Modifier.glassChrome(
    hazeState: HazeState,
    shape: Shape = RoundedCornerShape(16.dp),
    edgeWidth: Dp = 1.dp,
): Modifier {
    val topGlow = TajsOSTheme.Text.copy(alpha = 0.14f)
    val bottomShadow = Color.Transparent
    val topTint = TajsOSTheme.SurfaceHigh.copy(alpha = 0.42f)
    val bottomTint = TajsOSTheme.SurfaceLow.copy(alpha = 0.36f)
    val edgeColor = TajsOSTheme.GhostBorder.copy(alpha = 0.2f)

    return this
        .clip(shape)
        .hazeEffect(
            state = hazeState,
            style = HazeMaterials.thin(TajsOSTheme.SurfaceHigh),
        )
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
