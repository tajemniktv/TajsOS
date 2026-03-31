/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.notes.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.tajemniktv.tajsos.data.AttachmentEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeSnapshotEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.RelationEntity
import com.tajemniktv.tajsos.data.TagEntity
import com.tajemniktv.tajsos.ui.MainViewModel

/**
 * Defines the supported surfaces for note detail layout planning.
 */
enum class NoteDetailSurface {
    MOBILE,
    DESKTOP,
}

/**
 * Identifies a logical note detail block.
 */
data class NoteDetailBlock(
    val id: String,
)

/**
 * Structured layout plan for the note detail screen.
 */
data class NoteDetailPlan(
    val primary: List<NoteDetailBlock> = emptyList(),
    val secondary: List<NoteDetailBlock> = emptyList(),
)

/**
 * Shared state and actions for note detail block renderers.
 */
data class NoteDetailContext(
    val viewModel: MainViewModel,
    val node: NodeEntity,
    val tags: List<TagEntity>,
    val allTags: List<TagEntity>,
    val relations: List<RelationEntity>,
    val attachments: List<AttachmentEntity>,
    val areas: List<NodeEntity>,
    val projects: List<NodeEntity>,
    val snapshots: List<NodeSnapshotEntity>,
    val suggestions: List<NodeEntity>,
    val nodes: List<NodeWithPin>,
    val nodesMap: Map<Long, NodeWithPin>,
    val isAtomicMode: Boolean,
    val onNavigateToNode: (Long) -> Unit,
    val onNavigateToSearch: () -> Unit,
    val onUpdateTitle: (String) -> Unit,
    val onUpdateContent: (String) -> Unit,
    val onShowTagDialog: () -> Unit,
    val onShowRelationDialog: () -> Unit,
    val onShowStatusDialog: () -> Unit,
    val onShowAreaDialog: () -> Unit,
    val onShowProjectDialog: () -> Unit,
    val onShowNoteTypeDialog: () -> Unit,
    val onShowNoteStateDialog: () -> Unit,
    val onShowSnapshotDialog: () -> Unit,
    val onShowDueDialog: () -> Unit,
    val onShowReminderDialog: () -> Unit,
    val onShowEnergyDialog: () -> Unit,
    val onShowFrictionDialog: () -> Unit,
    val onShowEstimateDialog: () -> Unit,
    val onShowMediaTypeDialog: () -> Unit,
    val onShowRatingDialog: () -> Unit,
    val onShowRecurringDialog: () -> Unit,
    val onShowMoreDialog: () -> Unit,
    val onToggleAtomicMode: () -> Unit,
)

/**
 * Functional interface for rendering a note detail block.
 */
typealias NoteDetailBlockRenderer = @Composable (NoteDetailContext) -> Unit

/**
 * Represents an action in the "More" menu.
 */
data class MoreAction(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val subtext: String = "",
)
