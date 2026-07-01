/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.common_system_online

/**
 * Renders the dashboard header with a left menu badge and status block and a right-side system indicator with a settings button.
 *
 * @param vibe Subtitle text shown under the status label.
 * @param onMenuClick Callback invoked when the left menu area is clicked.
 * @param onSettingsClick Callback invoked when the settings icon is clicked.
 * @param tintColor Color used for the badge and status accent; defaults to `TactileTheme.Primary`.
 */
@Composable
fun DashHeader(
    vibe: String,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit,
    tintColor: Color = TajsOSTheme.Primary,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onMenuClick() }.pointerHoverIcon(PointerIcon.Hand),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(TajsOSTheme.Surface),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "T",
                    color = tintColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }
            Spacer(Modifier.width(12.dp))
            StatusHeader(
                color = tintColor,
                subtitle = vibe,
                subtitleStyle = MaterialTheme.typography.titleSmall,
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = CircleShape,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (tintColor ==
                                        TajsOSTheme.Primary
                                    ) {
                                        TajsOSTheme.Success
                                    } else {
                                        tintColor
                                    },
                                ),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(Res.string.common_system_online),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TajsOSTheme.Text,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            IconButton(onClick = onSettingsClick, modifier = Modifier.size(48.dp).pointerHoverIcon(PointerIcon.Hand)) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = TajsOSTheme.Muted)
            }
        }
    }
}
