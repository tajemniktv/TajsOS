/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.ui.components.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
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
import com.tajemniktv.tajsos.ui.design.theme.TactileTheme

@Composable
fun ModuleCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    status: String,
    onClick: () -> Unit,
    color: Color = TactileTheme.Primary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.98f else 1f

    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        color = TactileTheme.Surface,
        border = BorderStroke(1.dp, TactileTheme.Border.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingXs)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color.copy(alpha = 0.1f),
                        RoundedCornerShape(TactileTheme.RadiusSm)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = color
                )
            }

            Spacer(Modifier.height(TactileTheme.SpacingSm))

            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Text(
                text = status,
                style = MaterialTheme.typography.titleMedium,
                color = TactileTheme.Text,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
