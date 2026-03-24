/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.screens.AlertCard
import com.tajemniktv.tajsos.ui.screens.SuggestionGroup
import com.tajemniktv.tajsos.ui.theme.TactileTheme

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
            DetailSectionHeader(title = "CURRENT FOCUS", icon = Icons.Default.CenterFocusStrong)
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
