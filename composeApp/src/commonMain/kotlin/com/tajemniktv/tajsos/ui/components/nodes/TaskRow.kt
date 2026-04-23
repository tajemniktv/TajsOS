/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.nodes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.components.common.MouseContextMenuHost
import com.tajemniktv.tajsos.ui.components.common.mouseClickable
import com.tajemniktv.tajsos.ui.components.common.rememberMouseContextMenuState
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.common_open
import tajsos.composeapp.generated.resources.dash_annoying
import tajsos.composeapp.generated.resources.dash_heavy
import tajsos.composeapp.generated.resources.dash_unclear
import tajsos.composeapp.generated.resources.detail_archive
import tajsos.composeapp.generated.resources.task_row_unpin_desc

/**
 * Renders a single task row for the given node, showing its title, metadata and action controls.
 *
 * Displays a left completion checkbox (tied to `node.status`), the task title (with strike-through when done),
 * optional metadata labels (energy level, friction, non-standard status, next smallest step), and action buttons
 * for unpinning and, when completed, archiving. The row supports click and long-click handlers and draws a
 * colored accent bar at the left edge.
 *
 * @param node The task node to render.
 * @param onToggleDone Callback invoked when the checkbox is toggled; receives the new status string (`"done"` when checked, `"active"` when unchecked).
 * @param onUnpin Callback invoked when the unpin action is pressed.
 * @param modifier The modifier to be applied to the layout.
 * @param onLongClick Optional long-click handler for the row.
 * @param onClick Optional click handler for the row.
 * @param onArchive Optional archive handler invoked when the archive action (shown only for completed tasks) is pressed.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskRow(
    node: NodeEntity,
    onToggleDone: (String) -> Unit,
    onUnpin: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
    onArchive: () -> Unit = {},
) {
    val isDone = node.status == "done"
    val contextMenuState = rememberMouseContextMenuState()
    MouseContextMenuHost(
        state = contextMenuState,
        modifier = modifier.fillMaxWidth(),
        menuContent = {
            DropdownMenuItem(
                text = { Text(text = stringResource(Res.string.common_open)) },
                onClick = {
                    contextMenuState.dismiss()
                    onClick()
                },
            )
        },
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .drawBehind {
                        drawRect(
                            color = TajsOSTheme.Primary,
                            topLeft = Offset.Zero,
                            size =
                                androidx.compose.ui.geometry
                                    .Size(4.dp.toPx(), size.height),
                        )
                    }.mouseClickable(
                        onClick = onClick,
                        onLongClick = onLongClick,
                        onSecondaryClickAt = { contextMenuState.showAt(it) },
                        middleClickFallbackToPrimary = true,
                    ),
            color = TajsOSTheme.CardSurface,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = TajsOSTheme.SpacingMd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = isDone,
                    onCheckedChange = { onToggleDone(if (it) "done" else "active") },
                    colors = CheckboxDefaults.colors(checkedColor = TajsOSTheme.Primary),
                )
                Column(modifier = Modifier.weight(1f).alpha(if (isDone) 0.5f else 1f)) {
                    Text(
                        text = node.title,
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                textDecoration = if (isDone) TextDecoration.LineThrough else null,
                            ),
                        color = if (isDone) TajsOSTheme.Muted else TajsOSTheme.Text,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val energyLevel = node.energyLevel
                        if (energyLevel != null) {
                            Text(
                                text = "⚡".repeat(energyLevel),
                                style = MaterialTheme.typography.labelSmall,
                                color =
                                    when (energyLevel)
                                    {
                                        1 -> TajsOSTheme.Success
                                        2 -> TajsOSTheme.Primary
                                        3 -> TajsOSTheme.Error
                                        else -> TajsOSTheme.Muted
                                    },
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        val friction = node.friction
                        if (friction != null) {
                            val frictionLabel =
                                when (friction)
                                {
                                    "easy" -> "EASY"
                                    "annoying" -> stringResource(Res.string.dash_annoying)
                                    "mentally_heavy" -> stringResource(Res.string.dash_heavy)
                                    "unclear" -> stringResource(Res.string.dash_unclear)
                                    else -> friction
                                }
                            Text(
                                text = frictionLabel.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = TajsOSTheme.Primary,
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        if ((node.status != "active") && (node.status != "done")) {
                            val statusColor =
                                when (node.status)
                                {
                                    "blocked" -> TajsOSTheme.Error
                                    "on_hold" -> TajsOSTheme.Accent
                                    "someday" -> TajsOSTheme.Muted
                                    else -> TajsOSTheme.Primary
                                }
                            Text(
                                text = node.status.uppercase().replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        if (!node.nextSmallestStep.isNullOrEmpty()) {
                            Text(
                                text = "↳ ${node.nextSmallestStep}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TajsOSTheme.Accent,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                if (isDone) {
                    IconButton(onClick = onArchive) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.detail_archive),
                            tint = TajsOSTheme.Muted,
                        )
                    }
                }
                IconButton(onClick = onUnpin) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(Res.string.task_row_unpin_desc),
                        tint = TajsOSTheme.Muted,
                    )
                }
            }
        }
    }
}

