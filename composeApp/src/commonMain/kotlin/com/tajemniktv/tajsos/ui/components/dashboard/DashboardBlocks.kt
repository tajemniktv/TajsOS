/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.ui.components.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.components.cards.AlertCard
import com.tajemniktv.tajsos.ui.components.cards.DashCard
import com.tajemniktv.tajsos.ui.components.nodes.SuggestionGroup
import com.tajemniktv.tajsos.ui.theme.TactileTheme

/**
 * Renders the current focused task area or an empty-state alert when no active task is set.
 *
 * When `activeTask` is null, displays an alert prompting the user to assign a task. When non-null,
 * displays a "CURRENT FOCUS" header and a tappable card showing the task title and, if present,
 * its next smallest step.
 *
 * @param activeTask The currently active task to display, or `null` to show the empty-state alert.
 * @param onEdit Callback invoked with the task's id when the task card is tapped.
 */
@Composable
fun CurrentTaskBlock(
    activeTask: NodeWithPin?,
    onEdit: (Long) -> Unit
) {
    if (activeTask == null) {
        AlertCard(
            title = "NO ACTIVE TASK",
            description = "Assign a task to focus on.",
            icon = Icons.Default.Info,
            color = TactileTheme.Muted,
            onClick = {}
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
            com.tajemniktv.tajsos.ui.components.common.DetailSectionHeader(
                title = "CURRENT FOCUS",
                icon = Icons.Default.CenterFocusStrong
            )
            DashCard(onClick = { onEdit(activeTask.node.id) }) {
                Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                    Text(
                        activeTask.node.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = TactileTheme.Primary
                    )
                    if (activeTask.node.nextSmallestStep != null) {
                        Text(
                            "NEXT: ${activeTask.node.nextSmallestStep}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TactileTheme.Accent
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ErrandListBlock(
    errands: List<NodeWithPin>,
    onEdit: (Long) -> Unit
) {
    if (errands.isNotEmpty()) {
        SuggestionGroup(
            title = "ERRANDS // OUT AND ABOUT",
            icon = Icons.Default.ShoppingCart,
            color = Color(0xFF00BCD4),
            nodes = errands,
            onEditNode = onEdit
        )
    }
}

@Composable
fun TinyVictoriesBlock(
    victories: List<NodeWithPin>,
    onEdit: (Long) -> Unit
) {
    if (victories.isNotEmpty()) {
        SuggestionGroup(
            title = "TINY VICTORIES // RECENT",
            icon = Icons.Default.EmojiEvents,
            color = Color(0xFFFFD700),
            nodes = victories,
            onEditNode = onEdit
        )
    }
}
