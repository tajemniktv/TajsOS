/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.layout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

/**
 * Displays a 32.dp rounded square with a centered bold "T" glyph.
 *
 * @param tintColor Color used for the "T" glyph.
 */
@Composable
fun TBoxIcon(tintColor: Color) {
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
}

/**
 * Displays a circular status chip showing a colored status dot and a bold "SYSTEM: ONLINE" label.
 *
 * The chip is a semi-transparent black surface with a 1.dp border and centered row content.
 *
 * @param tintColor Color used for the status dot; when equal to `TactileTheme.Primary` the dot is rendered using `TactileTheme.Success` instead.
 */
@Composable
fun SystemOnlineStatus(tintColor: Color) {
    Surface(
        color = Color.Black.copy(alpha = 0.5f),
        shape = CircleShape,
        border = BorderStroke(1.dp, TajsOSTheme.Border),
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
                "SYSTEM: ONLINE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TajsOSTheme.Text,
            )
        }
    }
}

/**
 * Displays a rounded, bordered search placeholder surface containing a search icon and label.
 *
 * The surface uses theme tokens for background, border, and muted content; it contains a leading
 * search icon (18.dp) and the placeholder text "SEARCH YOUR LIFE..." laid out horizontally with padding.
 */
@Composable
fun DesktopSearchSurface() {
    Surface(
        color = TajsOSTheme.Surface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        border = BorderStroke(1.dp, TajsOSTheme.Border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = TajsOSTheme.Muted,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "SEARCH YOUR LIFE...",
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
        }
    }
}
