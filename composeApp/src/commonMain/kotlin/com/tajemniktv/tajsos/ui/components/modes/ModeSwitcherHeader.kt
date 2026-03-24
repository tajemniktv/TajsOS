/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.ui.components.modes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.ui.design.theme.TactileTheme

@Composable
fun ModeSwitcherHeader(
    currentMode: ModeEntity?,
    allModes: List<ModeEntity>,
    onModeSelect: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "OPERATING MODE",
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontSize = 10.sp
        )
        Spacer(Modifier.height(TactileTheme.SpacingSm))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(allModes) { mode ->
                val isSelected = mode.id == currentMode?.id
                val color = mode.themeColor?.let { Color(it) } ?: TactileTheme.Primary
                Surface(
                    onClick = { onModeSelect(mode.id) },
                    color = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent,
                    shape = RoundedCornerShape(TactileTheme.RadiusSm),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) color else TactileTheme.Border.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        mode.name.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) color else TactileTheme.Muted,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
