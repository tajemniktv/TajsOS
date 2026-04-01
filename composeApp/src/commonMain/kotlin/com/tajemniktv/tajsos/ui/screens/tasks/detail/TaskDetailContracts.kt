/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.tasks.detail

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.data.AttachmentEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.RelationEntity
import com.tajemniktv.tajsos.data.TagEntity
import com.tajemniktv.tajsos.ui.MainViewModel

/**
 * Defines the supported surfaces for task detail layout planning.
 */
enum class TaskDetailSurface {
    MOBILE,
    DESKTOP,
}

/**
 * Identifies a logical task detail block.
 */
data class TaskDetailBlock(
    val id: String,
)

/**
 * Structured layout plan for the task detail screen.
 */
data class TaskDetailPlan(
    val primary: List<TaskDetailBlock> = emptyList(),
    val secondary: List<TaskDetailBlock> = emptyList(),
)

/**
 * Shared state and actions for task detail block renderers.
 */
data class TaskDetailContext(
    val viewModel: MainViewModel,
    val task: NodeEntity,
    val tags: List<TagEntity>,
    val relations: List<RelationEntity>,
    val attachments: List<AttachmentEntity>,
    val allNodeById: Map<Long, NodeWithPin>,
    val areas: List<Pair<Long, String>>,
    val projects: List<Pair<Long, String>>,
    val areaById: Map<Long, String>,
    val projectById: Map<Long, String>,
    val subtasks: List<TaskSubtaskUi>,
    val historyItems: List<TaskHistoryUi>,
    val attachmentItems: List<TaskAttachmentUi>,
    val doneSubtasksCount: Int,
    val totalSubtasksCount: Int,
    val subtaskProgress: Float?,
    val isEditing: Boolean,
    val draftTitle: String,
    val draftDescription: String,
    val onDraftTitleChange: (String) -> Unit,
    val onDraftDescriptionChange: (String) -> Unit,
    val onToggleEdit: () -> Unit,
    val onSaveEdits: () -> Unit,
    val onCancelEdits: () -> Unit,
    val onSnooze: () -> Unit,
    val onComplete: () -> Unit,
    val onStatusChange: (String) -> Unit,
    val onAreaChange: (Long?) -> Unit,
    val onProjectChange: (Long?) -> Unit,
    val onDuePresetChange: (TaskDuePreset) -> Unit,
    val onRecurrenceChange: (String?) -> Unit,
    val onEstimateChange: (Int?) -> Unit,
    val onCriticalityChange: (Boolean) -> Unit,
    val onToggleSubtask: (TaskSubtaskUi) -> Unit,
    val onAddInlineSubtask: (String) -> Unit,
    val onRemoveInlineSubtask: (TaskSubtaskUi) -> Unit,
    val onSplitIntoSubtasks: () -> Unit,
    val onRemoveAttachment: (Long) -> Unit,
)

/**
 * Functional interface for rendering a task detail block.
 */
typealias TaskDetailBlockRenderer = @Composable (TaskDetailContext) -> Unit

enum class TaskSubtaskState {
    COMPLETE,
    ACTIVE,
    QUEUED,
}

enum class TaskSubtaskSource {
    Node,
    InlineChecklist,
}

data class TaskSubtaskUi(
    val id: Long,
    val title: String,
    val state: TaskSubtaskState,
    val source: TaskSubtaskSource,
    val inlineIndex: Int? = null,
    val node: NodeEntity? = null,
)

enum class TaskDuePreset {
    None,
    Today,
    Tomorrow,
    InSevenDays,
}

data class TaskAttachmentUi(
    val id: Long,
    val title: String,
    val typeLabel: String,
    val sizeLabel: String?,
)

data class TaskHistoryUi(
    val id: Long,
    val title: String,
    val subtitle: String?,
    val timestampLabel: String,
)
