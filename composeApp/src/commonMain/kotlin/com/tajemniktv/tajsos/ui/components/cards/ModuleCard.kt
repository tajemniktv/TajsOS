/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

@Composable
fun ModuleCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    status: String,
    onClick: () -> Unit,
    color: Color = TajsOSTheme.Primary,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.98f else 1f
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier =
            modifier
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    },
                ),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        color = TajsOSTheme.Surface,
        border = BorderStroke(1.dp, TajsOSTheme.Border.copy(alpha = 0.5f)),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingXs),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .background(
                            color.copy(alpha = 0.1f),
                            RoundedCornerShape(TajsOSTheme.RadiusSm),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = color,
                )
            }

            Spacer(Modifier.height(TajsOSTheme.SpacingSm))

            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Muted,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
            )

            Text(
                text = status,
                style = MaterialTheme.typography.titleMedium,
                color = TajsOSTheme.Text,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
