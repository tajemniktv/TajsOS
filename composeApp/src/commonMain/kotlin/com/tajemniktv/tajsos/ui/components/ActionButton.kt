/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

/**
 * Renders a Material3 button with a fixed height, rounded corners, an optional leading icon, and an uppercase bold label.
 *
 * @param text The button label.
 * @param onClick Callback invoked when the button is clicked.
 * @param modifier External modifier applied to the button; a fixed height constraint is appended.
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
    val isPrimary = containerColor == TajsOSTheme.Primary
    val isGhost = containerColor == Color.Transparent
    val buttonBorder =
        if (!isPrimary && !isGhost) {
            androidx.compose.foundation.BorderStroke(1.dp, TajsOSTheme.GhostBorder)
        } else {
            null
        }

    val finalModifier =
        modifier.height(48.dp).then(
            if (isPrimary && enabled) {
                Modifier.background(
                    brush =
                        Brush.linearGradient(
                            colors = listOf(TajsOSTheme.Primary, TajsOSTheme.PrimaryDim),
                            start = Offset(0f, 0f),
                            end = Offset.Infinite,
                        ),
                    shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                )
            } else {
                Modifier
            },
        )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = finalModifier,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = if (isPrimary && enabled) Color.Transparent else containerColor,
                contentColor = if (isPrimary && contentColor == TajsOSTheme.Text) TajsOSTheme.Background else contentColor,
                disabledContainerColor = if (isGhost) Color.Transparent else TajsOSTheme.SurfaceHighest,
                disabledContentColor = TajsOSTheme.Muted,
            ),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        border = buttonBorder,
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
