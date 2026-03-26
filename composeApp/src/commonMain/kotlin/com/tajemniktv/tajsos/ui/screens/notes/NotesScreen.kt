/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.cards.NodeCard
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*
import kotlin.time.Instant

/**
 * Renders the Notes screen: a searchable, groupable list of knowledge nodes with actions and navigation.
 *
 * The UI collects active nodes, areas, and projects from the provided view model, derives knowledge nodes
 * (types "note", "idea", "resource"), and filters them by the current search query (matching title or content,
 * case-insensitively). Results can be grouped by "TYPE", "AREA", "PROJECT", "DATE", or "MEDIA" using the top
 * filter chips. Each non-empty group shows a header and its items; when no results exist an appropriate
 * empty-state message is shown (different message for empty dataset vs. no search results).
 *
 * @param onNoteClick Callback invoked with the note id when a list item is clicked.
 */
@Composable
fun NotesScreen(
    viewModel: MainViewModel,
    onNoteClick: (Long) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth >
                900.dp
            ) {
                NotesDashboardSurface.DESKTOP
            } else {
                NotesDashboardSurface.MOBILE
            }
        val plan =
            remember(surface) {
                buildNotesDashboardPlan(
                    surface,
                )
            }
        val context =
            remember(viewModel, onNoteClick) {
                NotesDashboardContext(
                    viewModel,
                    onNoteClick,
                )
            }
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                NotesDashboardBlockRegistry
                    .resolve(block.id)
                    ?.invoke(context)
            }
        }
    }
}

/**
 * Displays a section header for grouped lists using the screen's typography, color, and vertical spacing.
 *
 * @param title The text to display as the header.
 */
@Composable
fun GroupHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = TactileTheme.Primary,
        modifier = Modifier.padding(top = TactileTheme.SpacingMd, bottom = TactileTheme.SpacingSm),
    )
}

/**
 * Displays a card for a knowledge node and connects its UI actions to the provided handlers.
 *
 * The card presents the node state and forwards user interactions: clicking opens the note via
 * `onNoteClick`, toggling done/pin updates the node through the provided view model, and archiving
 * requests the view model to archive the node.
 *
 * @param node The knowledge node together with its pinned state.
 * @param onNoteClick Callback invoked with the node's id when the card is clicked.
 */
@Composable
fun KnowledgeItem(
    node: NodeWithPin,
    viewModel: MainViewModel,
    onNoteClick: (Long) -> Unit,
) {
    NodeCard(
        nodeWithPin = node,
        onClick = { onNoteClick(node.node.id) },
        onToggleDone = { status ->
            viewModel.updateNodeStatus(
                node.node,
                status,
            )
        },
        onTogglePin = { isPinned -> viewModel.togglePin(node.node, isPinned) },
        onArchive = { viewModel.archiveNode(node.node) },
    )
}
