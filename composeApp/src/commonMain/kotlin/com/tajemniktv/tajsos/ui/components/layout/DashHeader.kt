/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.ui.components.layout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
fun DashHeader(
    vibe: String,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit,
    tintColor: Color = TactileTheme.Primary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onMenuClick() }) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(TactileTheme.Surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "T",
                    color = tintColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            StatusHeader(
                status = "OK",
                color = tintColor,
                subtitle = vibe,
                subtitleStyle = MaterialTheme.typography.titleSmall,
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = CircleShape,
                border = BorderStroke(1.dp, TactileTheme.Border)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(6.dp).clip(CircleShape)
                            .background(if (tintColor == TactileTheme.Primary) TactileTheme.Success else tintColor)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "SYSTEM: ONLINE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TactileTheme.Text
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = TactileTheme.Muted)
            }
        }
    }
}
