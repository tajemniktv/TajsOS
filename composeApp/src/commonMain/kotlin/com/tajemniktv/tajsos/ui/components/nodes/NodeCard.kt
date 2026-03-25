/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.nodes

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*
import kotlin.time.Clock

/**
 * Displays a card representing a node with badges, status indicators, and action controls.
 *
 * The card shows the node title, type/recurrence badge, optional due date (with hard-deadline styling),
 * stale indicator, energy and friction badges, status badge for non-active/non-done states, and an
 * optional next-smallest-step line. A checkbox toggles the node's done state and the card animates
 * slightly when done. Right-side icons provide archive (when done) and pin/today toggle actions.
 *
 * @param nodeWithPin The node data and its pin-to-today state.
 * @param modifier Optional Compose modifier applied to the root Surface.
 * @param onToggleDone Called when the checkbox is toggled; receives the new status string (`"done"` or `"active"`).
 * @param onTogglePin Called with the new pinned-to-today boolean when the star icon is pressed.
 * @param onLongClick Invoked on a long press of the card.
 * @param onClick Invoked on a regular click of the card.
 * @param onArchive Invoked when the archive/delete icon is pressed (visible when the node is done).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NodeCard(
    nodeWithPin: NodeWithPin,
    modifier: Modifier = Modifier,
    onToggleDone: (String) -> Unit,
    onTogglePin: (Boolean) -> Unit,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
    onArchive: () -> Unit = {},
)
{
    val node = nodeWithPin.node
    val isPinnedToToday = nodeWithPin.isPinnedToToday
    val isDone = node.status == "done"

    val animatedScale by animateFloatAsState(
        targetValue = if (isDone) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "NodeScale",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(
            1.dp,
            if (isPinnedToToday) TactileTheme.Primary else TactileTheme.Border,
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(TactileTheme.SpacingMd)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = isDone,
                onCheckedChange = { onToggleDone(if (it) "done" else "active") },
                colors = CheckboxDefaults.colors(checkedColor = TactileTheme.Primary),
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = node.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = if (isDone) TextDecoration.LineThrough else null,
                        ),
                        color = if (isDone) TactileTheme.Muted else TactileTheme.Text,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (node.isPinned)
                    {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = stringResource(Res.string.detail_favorite),
                            tint = TactileTheme.Primary,
                            modifier = Modifier.padding(start = 4.dp).size(12.dp),
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val typeLabel = when (node.type)
                    {
                        "task"    -> stringResource(Res.string.type_task)
                        "note"    -> stringResource(Res.string.type_note)
                        "idea"    -> stringResource(Res.string.type_idea)
                        "project" -> stringResource(Res.string.type_project)
                        "area"    -> stringResource(Res.string.type_area)
                        else      -> node.type
                    }
                    NodeBadge(
                        text = typeLabel.uppercase() + if (node.isRecurring) " // ${
                            stringResource(
                                Res.string.node_recurring,
                            )
                        }" else "",
                        color = TactileTheme.Primary,
                    )
                    val dueAt = node.dueAt
                    if (dueAt != null)
                    {
                        val due = kotlin.time.Instant.fromEpochMilliseconds(dueAt)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = if (node.isHardDeadline) TactileTheme.Error else TactileTheme.Accent,
                            modifier = Modifier.size(10.dp),
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "${due.day}/${due.month.number}${if (node.isHardDeadline) "!" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (node.isHardDeadline) TactileTheme.Error else TactileTheme.Accent,
                        )
                    }
                    val staleTime =
                            Clock.System.now().toEpochMilliseconds() - (14 * 24 * 60 * 60 * 1000L)
                    if (node.status == "active" && node.updatedAt < staleTime)
                    {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.node_stale),
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Error.copy(alpha = 0.5f),
                        )
                    }
                    if (node.energyLevel != null)
                    {
                        Spacer(Modifier.width(8.dp))
                        NodeBadge(
                            text = "⚡".repeat(node.energyLevel!!),
                            color = when (node.energyLevel)
                            {
                                1    -> TactileTheme.Success
                                2    -> TactileTheme.Primary
                                3    -> TactileTheme.Error
                                else -> TactileTheme.Muted
                            },
                        )
                    }
                    if (node.friction != null)
                    {
                        Spacer(Modifier.width(8.dp))
                        val frictionLabel = when (node.friction)
                        {
                            "easy"           -> stringResource(Res.string.dash_overwhelmed) // Using existing "Easy wins only" or similar
                            "annoying"       -> "ANNOYING"
                            "mentally_heavy" -> "HEAVY"
                            "unclear"        -> "UNCLEAR"
                            else             -> node.friction!!
                        }
                        NodeBadge(
                            text = frictionLabel.uppercase(),
                            color = TactileTheme.Primary,
                        )
                    }
                    if (node.status != "active" && node.status != "done")
                    {
                        Spacer(Modifier.width(8.dp))
                        val statusColor = when (node.status)
                        {
                            "blocked" -> TactileTheme.Error
                            "on_hold" -> TactileTheme.Accent
                            "someday" -> TactileTheme.Muted
                            else      -> TactileTheme.Primary
                        }
                        NodeBadge(
                            text = node.status.uppercase().replace("_", " "),
                            color = statusColor,
                        )
                    }
                }
                if (!node.nextSmallestStep.isNullOrEmpty())
                {
                    Text(
                        text = "↳ ${node.nextSmallestStep}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Accent,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                }
            }
            if (isDone)
            {
                IconButton(onClick = onArchive) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.detail_archive),
                        tint = TactileTheme.Muted,
                    )
                }
            }
            IconButton(onClick = { onTogglePin(!isPinnedToToday) }) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = stringResource(Res.string.node_pin_today_desc),
                    tint = if (isPinnedToToday) TactileTheme.Primary else TactileTheme.Muted.copy(
                        alpha = 0.5f,
                    ),
                )
            }
        }
    }
}


/**
 * Renders a compact rounded badge displaying the given text using the provided color.
 *
 * The badge has a subtle primary-tinted background, small corner radius, and internal padding to fit alongside other UI elements.
 *
 * @param text The label shown inside the badge.
 * @param color The color applied to the badge text.
 */
@Composable
fun NodeBadge(text: String, color: androidx.compose.ui.graphics.Color)
{
    Surface(
        color = TactileTheme.Primary.copy(alpha = 0.1f),
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
