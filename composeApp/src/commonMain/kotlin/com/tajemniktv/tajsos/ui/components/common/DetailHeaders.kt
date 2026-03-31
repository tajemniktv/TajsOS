/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

/**
 * Displays a two-line header with an uppercase primary title and an uppercase secondary subtitle.
 *
 * @param title The primary header text shown prominently.
 * @param subtitle The secondary header text shown in a smaller, muted style.
 */
@Composable
fun DetailHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            color = TajsOSTheme.Primary,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
        )
        Text(
            text = subtitle.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TajsOSTheme.Muted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
    }
}

/**
 * Displays a single-row section header containing an uppercase title and an optional icon.
 *
 * The header fills the available width, aligns its contents vertically centered, and spaces
 * the icon and title consistently.
 *
 * @param title The text to display as the section title; rendered in uppercase.
 * @param modifier Modifier applied to the header container.
 * @param icon Optional vector icon shown to the start of the title when provided.
 * @param color Color used for both the icon tint (if present) and the title text.
 */
@Composable
fun DetailSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    color: Color = TajsOSTheme.Muted
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        }
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
        )
    }
}
