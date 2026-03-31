/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

/**
 * Displays a two-line status header: an uppercased status line prefixed with "TAJSOS // STATUS:" and an optional uppercased subtitle.
 *
 * The status line is styled to stand out and uses the provided `color`. When `subtitle` is non-null, a second line is rendered using `subtitleStyle`.
 *
 * @param status The status text shown in the primary line (will be uppercased). Defaults to `"OK"`.
 * @param color Color applied to the primary status line. Defaults to `TactileTheme.Primary`.
 * @param subtitle Optional secondary line text; when present it will be uppercased and displayed below the status.
 * @param subtitleStyle Text style applied to the subtitle when shown. Defaults to `MaterialTheme.typography.titleMedium`.
 */
@Composable
fun StatusHeader(
    modifier: Modifier = Modifier,
    status: String = "OK",
    color: Color = TajsOSTheme.Primary,
    subtitle: String? = null,
    subtitleStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium,
) {
    Column(modifier = modifier) {
        Text(
            text = "TAJSOS // STATUS: ${status.uppercase()}",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
        )
        if (subtitle != null) {
            Text(
                text = subtitle.uppercase(),
                style = subtitleStyle,
                color = TajsOSTheme.Text,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}
