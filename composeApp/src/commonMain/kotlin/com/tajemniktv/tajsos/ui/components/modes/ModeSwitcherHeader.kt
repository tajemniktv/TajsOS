/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.modes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

/**
 * Renders a header labeled "OPERATING MODE" with a horizontally scrollable row of selectable mode chips.
 *
 * Each chip shows a mode's name (uppercase), highlights the currently selected mode using the mode's theme color,
 * and invokes the selection callback when clicked.
 *
 * @param currentMode The currently active mode, or `null` if none is selected.
 * @param allModes The list of modes to display as chips.
 * @param onModeSelect Callback invoked with the selected mode's `id` when a chip is clicked.
 */
@Composable
fun ModeSwitcherHeader(
    currentMode: ModeEntity?,
    allModes: List<ModeEntity>,
    onModeSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "OPERATING MODE",
            style = MaterialTheme.typography.labelSmall,
            color = TajsOSTheme.Muted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontSize = 10.sp,
        )
        Spacer(Modifier.height(TajsOSTheme.SpacingSm))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(allModes, key = { it.id }) { mode ->
                val isSelected = mode.id == currentMode?.id
                val color = mode.themeColor?.let { Color(it) } ?: TajsOSTheme.Primary
                Surface(
                    onClick = { onModeSelect(mode.id) },
                    color = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent,
                    shape = RoundedCornerShape(TajsOSTheme.RadiusSm),
                    border =
                        BorderStroke(
                            1.dp,
                            if (isSelected) color else TajsOSTheme.Border.copy(alpha = 0.3f),
                        ),
                ) {
                    Text(
                        mode.name.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) color else TajsOSTheme.Muted,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}
