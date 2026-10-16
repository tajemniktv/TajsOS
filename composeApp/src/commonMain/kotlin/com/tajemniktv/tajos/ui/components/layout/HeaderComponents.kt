/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.tajemniktv.tajos.ui.theme.TajsOSTheme

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
