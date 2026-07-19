/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.cards

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.components.common.GlassMaterial
import com.tajemniktv.tajsos.ui.components.common.MouseContextMenuHost
import com.tajemniktv.tajsos.ui.components.common.glassChrome
import com.tajemniktv.tajsos.ui.components.common.glassContainerColor
import com.tajemniktv.tajsos.ui.components.common.mouseClickable
import com.tajemniktv.tajsos.ui.components.common.rememberMouseContextMenuState
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.common_open
import tajsos.composeapp.generated.resources.detail_archive
import tajsos.composeapp.generated.resources.detail_favorite
import tajsos.composeapp.generated.resources.node_pin_today_desc
import tajsos.composeapp.generated.resources.node_recurring
import tajsos.composeapp.generated.resources.node_stale
import tajsos.composeapp.generated.resources.type_area
import tajsos.composeapp.generated.resources.type_idea
import tajsos.composeapp.generated.resources.type_note
import tajsos.composeapp.generated.resources.type_project
import tajsos.composeapp.generated.resources.type_record
import tajsos.composeapp.generated.resources.type_task
import kotlin.time.Clock
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.PointerIcon

@Composable
fun NodeCard(
    nodeWithPin: NodeWithPin,
    modifier: Modifier = Modifier,
    onToggleDone: (String) -> Unit,
    onTogglePin: (Boolean) -> Unit,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
    onArchive: () -> Unit = {},
) {
    val node = nodeWithPin.node
    val isPinnedToToday = nodeWithPin.isPinnedToToday
    val isDone = node.status == "done"

    val animatedScale by animateFloatAsState(
        targetValue = if (isDone) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "NodeScale",
    )

    val contextMenuState = rememberMouseContextMenuState()
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(TajsOSTheme.RadiusXl),
    ) {
        MouseContextMenuHost(
            state = contextMenuState,
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
                        .graphicsLayer { scaleX = animatedScale; scaleY = animatedScale }
                        .glassChrome(
                            shape = RoundedCornerShape(TajsOSTheme.RadiusXl),
                            material = GlassMaterial.REGULAR,
                        ).mouseClickable(
                            onClick = onClick,
                            onLongClick = onLongClick,
                            onSecondaryClickAt = { contextMenuState.showAt(it) },
                            middleClickFallbackToPrimary = true,
                        ),
                color = glassContainerColor(TajsOSTheme.Surface),
                shape = RoundedCornerShape(TajsOSTheme.RadiusXl),
                border =
                    BorderStroke(
                        1.dp,
                        if (isPinnedToToday) TajsOSTheme.Primary else TajsOSTheme.Border,
                    ),
            ) {
                Row(
                    modifier = Modifier.padding(TajsOSTheme.SpacingMd).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = isDone,
                        onCheckedChange = { onToggleDone(if (it) "done" else "active") },
                        colors = CheckboxDefaults.colors(checkedColor = TajsOSTheme.Primary),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = node.title,
                                style =
                                    MaterialTheme.typography.bodyLarge.copy(
                                        textDecoration = if (isDone) TextDecoration.LineThrough else null,
                                    ),
                                color = if (isDone) TajsOSTheme.Muted else TajsOSTheme.Text,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            if (node.isPinned) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = stringResource(Res.string.detail_favorite),
                                    tint = TajsOSTheme.Primary,
                                    modifier = Modifier.padding(start = 4.dp).size(12.dp),
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val typeLabel =
                                when (node.type) {
                                    "task" -> stringResource(Res.string.type_task)
                                    "note" -> stringResource(Res.string.type_note)
                                    "record" -> stringResource(Res.string.type_record)
                                    "idea" -> stringResource(Res.string.type_idea)
                                    "project" -> stringResource(Res.string.type_project)
                                    "area" -> stringResource(Res.string.type_area)
                                    else -> node.type
                                }
                            NodeBadge(
                                text =
                                    typeLabel.uppercase() +
                                        if (node.isRecurring) {
                                            " // ${
                                                stringResource(
                                                    Res.string.node_recurring,
                                                )
                                            }"
                                        } else {
                                            ""
                                        },
                                color = TajsOSTheme.Primary,
                            )
                            val dueAt = node.dueAt
                            if (dueAt != null) {
                                val due =
                                    kotlin.time.Instant
                                        .fromEpochMilliseconds(dueAt)
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = if (node.isHardDeadline) TajsOSTheme.Error else TajsOSTheme.Accent,
                                    modifier = Modifier.size(10.dp),
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text = "${due.day}/${due.month.number}${if (node.isHardDeadline) "!" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (node.isHardDeadline) TajsOSTheme.Error else TajsOSTheme.Accent,
                                )
                            }
                            val staleTime =
                                Clock.System
                                    .now()
                                    .toEpochMilliseconds() -
                                    (14 * 24 * 60 * 60 * 1000L)
                            if (node.status == "active" && node.updatedAt < staleTime) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(Res.string.node_stale),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TajsOSTheme.Error.copy(alpha = 0.5f),
                                )
                            }
                            val energyLevel = node.energyLevel
                            if (energyLevel != null) {
                                Spacer(Modifier.width(8.dp))
                                NodeBadge(
                                    text = "⚡".repeat(energyLevel),
                                    color =
                                        when (energyLevel) {
                                            1 -> TajsOSTheme.Success
                                            2 -> TajsOSTheme.Primary
                                            3 -> TajsOSTheme.Error
                                            else -> TajsOSTheme.Muted
                                        },
                                )
                            }
                            val friction = node.friction
                            if (friction != null) {
                                Spacer(Modifier.width(8.dp))
                                val frictionLabel =
                                    when (friction) {
                                        "easy" -> "EASY"
                                        "annoying" -> "ANNOYING"
                                        "mentally_heavy" -> "HEAVY"
                                        "unclear" -> "UNCLEAR"
                                        else -> friction
                                    }
                                NodeBadge(
                                    text = frictionLabel.uppercase(),
                                    color = TajsOSTheme.Primary,
                                )
                            }
                            if (node.status != "active" && node.status != "done") {
                                Spacer(Modifier.width(8.dp))
                                val statusColor =
                                    when (node.status) {
                                        "blocked" -> TajsOSTheme.Error
                                        "on_hold" -> TajsOSTheme.Accent
                                        "someday" -> TajsOSTheme.Muted
                                        else -> TajsOSTheme.Primary
                                    }
                                NodeBadge(
                                    text = node.status.uppercase().replace("_", " "),
                                    color = statusColor,
                                )
                            }
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
                    if (isDone) {
                        IconButton(onClick = onArchive, modifier = Modifier.size(48.dp).pointerHoverIcon(PointerIcon.Hand)) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(Res.string.detail_archive),
                                tint = TajsOSTheme.Muted,
                            )
                        }
                    }
                    IconButton(onClick = { onTogglePin(!isPinnedToToday) }) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = stringResource(Res.string.node_pin_today_desc),
                            tint =
                                if (isPinnedToToday) {
                                    TajsOSTheme.Primary
                                } else {
                                    TajsOSTheme.Muted.copy(
                                        alpha = 0.5f,
                                    )
                                },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NodeBadge(
    text: String,
    color: Color,
) {
    Surface(
        color = TajsOSTheme.Primary.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        )
    }
}
