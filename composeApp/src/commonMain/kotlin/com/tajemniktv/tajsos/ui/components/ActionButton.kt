/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

@Composable
private fun resolveButtonColors(
    isPrimary: Boolean,
    enabled: Boolean,
    containerColor: Color,
    contentColor: Color,
    isGhost: Boolean,
): ButtonColors =
    ButtonDefaults.buttonColors(
        containerColor = if (isPrimary && enabled) Color.Transparent else containerColor,
        contentColor = if (isPrimary && contentColor == TajsOSTheme.Text) TajsOSTheme.Background else contentColor,
        disabledContainerColor = if (isGhost) Color.Transparent else TajsOSTheme.SurfaceHighest,
        disabledContentColor = TajsOSTheme.Muted,
    )

private fun resolveButtonBorder(
    isPrimary: Boolean,
    isGhost: Boolean,
): BorderStroke? =
    if (!isPrimary && !isGhost) {
        BorderStroke(1.dp, TajsOSTheme.GhostBorder)
    } else {
        null
    }

private fun Modifier.primaryBackground(
    isPrimary: Boolean,
    enabled: Boolean,
): Modifier =
    if (isPrimary && enabled) {
        this.background(
            brush =
                Brush.linearGradient(
                    colors = listOf(TajsOSTheme.Primary, TajsOSTheme.PrimaryDim),
                    start = Offset(0f, 0f),
                    end = Offset.Infinite,
                ),
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        )
    } else {
        this
    }

/**
 * Renders a Material3 button with a fixed height, rounded corners, an optional leading icon, and an uppercase bold label.
 *
 * @param text The button label.
 * @param onClick Callback invoked when the button is clicked.
 * @param modifier External modifier applied to the button; a fixed height constraint is appended.
 * @param enabled Whether the button is enabled.
 * @param containerColor Background color of the button.
 * @param contentColor Foreground color used for the text and icon.
 * @param icon Optional leading icon to display before the label.
 */
@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = TajsOSTheme.SurfaceHighest,
    contentColor: Color = TajsOSTheme.Text,
    icon: ImageVector? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f)

    val isPrimary = containerColor == TajsOSTheme.Primary
    val isGhost = containerColor == Color.Transparent

    val finalModifier =
        modifier
            .scale(scale)
            .height(48.dp)
            .primaryBackground(isPrimary, enabled)

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = finalModifier,
        colors = resolveButtonColors(isPrimary, enabled, containerColor, contentColor, isGhost),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        border = resolveButtonBorder(isPrimary, isGhost),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
    }
}
