/*
 * Copyright (c) TajemnikTV 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
fun ModuleButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    status: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.95f else 1f

    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        shape = RoundedCornerShape(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.RadiusMd),
        color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Surface,
        border = BorderStroke(1.dp, _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted)
    ) {
        Box(modifier = Modifier.padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Center),
                tint = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary
            )
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelSmall,
                    color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary
                )
            }
        }
    }
}
