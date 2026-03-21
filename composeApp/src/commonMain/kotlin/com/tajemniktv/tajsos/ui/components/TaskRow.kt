/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskRow(
    node: NodeEntity,
    onToggleDone: (String) -> Unit,
    onUnpin: () -> Unit,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
    onArchive: () -> Unit = {}
) {
    val isDone = node.status == "done"
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .drawBehind {
                drawRect(
                    color = TactileTheme.Primary,
                    topLeft = Offset.Zero,
                    size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height)
                )
            }
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = TactileTheme.Surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = TactileTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isDone,
                onCheckedChange = { onToggleDone(if (it) "done" else "active") },
                colors = CheckboxDefaults.colors(checkedColor = TactileTheme.Primary)
            )
            Text(
                text = node.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (isDone) TextDecoration.LineThrough else null
                ),
                modifier = Modifier.weight(1f),
                color = if (isDone) TactileTheme.Muted else TactileTheme.Text
            )
            if (isDone) {
                IconButton(onClick = onArchive) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Archive",
                        tint = TactileTheme.Muted
                    )
                }
            }
            IconButton(onClick = onUnpin) {
                Icon(Icons.Default.Close, contentDescription = "Unpin", tint = TactileTheme.Muted)
            }
        }
    }
}
