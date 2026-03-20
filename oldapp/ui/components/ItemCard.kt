/*
 * Copyright (c) TajemnikTV 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.ItemWithPin

@Composable
fun ItemCard(
    itemWithPin: ItemWithPin,
    modifier: Modifier = Modifier,
    onToggleDone: (String) -> Unit,
    onTogglePin: (Boolean) -> Unit,
    onArchive: () -> Unit = {}
) {
    val item = itemWithPin.item
    val isPinnedToToday = itemWithPin.isPinnedToToday
    val isDone = item.status == "done"
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isDone) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ItemScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale),
        color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Surface,
        shape = RoundedCornerShape(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, if (isPinnedToToday) _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary else _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Border)
    ) {
        Row(
            modifier = Modifier
                .padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isDone,
                onCheckedChange = { onToggleDone(if (it) "done" else "active") },
                colors = CheckboxDefaults.colors(checkedColor = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = if (isDone) TextDecoration.LineThrough else null
                        ),
                        color = if (isDone) _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted else _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (item.isPinned) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Pinned knowledge",
                            tint = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary,
                            modifier = Modifier.padding(start = 4.dp).size(12.dp)
                        )
                    }
                }
                Text(
                    text = item.type.uppercase() + if (item.isRecurring) " // RECURRING" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary
                )
            }
            if (isDone) {
                IconButton(onClick = onArchive) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Archive",
                        tint = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted
                    )
                }
            }
            IconButton(onClick = { onTogglePin(!isPinnedToToday) }) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Pin to today",
                    tint = if (isPinnedToToday) _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary else _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted.copy(alpha = 0.5f)
                )
            }
        }
    }
}
