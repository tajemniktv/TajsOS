/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.ui.components.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.theme.TactileTheme

/**
 * Renders a centered single-line system status footer showing memory usage and uptime.
 *
 * The text displays the literal string "MEMORY USAGE: 42%  •  UPTIME: 14D 02H" styled with
 * MaterialTheme.typography.labelSmall, bold weight, 1.sp letter spacing, and TactileTheme.Muted color.
 */
@Composable
fun SystemFooter() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Text(
            "MEMORY USAGE: 42%  •  UPTIME: 14D 02H",
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
