/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.ui.components.layout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
fun StatusHeader(
    modifier: Modifier = Modifier,
    status: String = "OK",
    color: Color = TactileTheme.Primary,
    subtitle: String? = null,
    subtitleStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium,
)
{
    Column(modifier = modifier) {
        Text(
            text = "TAJSOS // STATUS: ${status.uppercase()}",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
        )
        if (subtitle != null)
        {
            Text(
                text = subtitle.uppercase(),
                style = subtitleStyle,
                color = TactileTheme.Text,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}
