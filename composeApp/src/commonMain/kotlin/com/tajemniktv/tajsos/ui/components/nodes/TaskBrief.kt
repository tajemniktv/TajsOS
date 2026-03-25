/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.ui.components.nodes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.theme.TactileTheme

/**
 * Renders a clickable task row with a circular completion indicator and title.
 *
 * Shows a 20.dp circular indicator that reflects `isDone` (filled and checked when true)
 * and toggles completion via `onToggle`. The whole surface is clickable and invokes `onClick`.
 *
 * @param title The task title displayed as a single-line text.
 * @param isDone Whether the task is marked complete.
 * @param onToggle Called when the circular completion indicator is tapped.
 * @param onClick Called when the task row (surface) is tapped.
 */
@Composable
fun TaskBrief(title: String, isDone: Boolean, onToggle: () -> Unit, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Black.copy(alpha = 0.2f),
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(20.dp).clip(CircleShape)
                    .background(if (isDone) TactileTheme.Primary else Color.Transparent).border(
                        1.dp,
                        if (isDone) TactileTheme.Primary else TactileTheme.Muted,
                        CircleShape
                    ).clickable { onToggle() }, contentAlignment = Alignment.Center
            ) {
                if (isDone) Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
            }
            Spacer(Modifier.width(12.dp)); Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDone) TactileTheme.Muted else TactileTheme.Text,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
        }
    }
}
