/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.tasks.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.AttachmentEntity
import com.tajemniktv.tajsos.data.EventLogEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.isTaskItem
import com.tajemniktv.tajsos.data.taskStateOrNull
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.screens.notes.detail.NoteDetailScreen
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.tasks_no_results
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Instant

private const val TaskDetailWideLayoutBreakpoint = 1120
private const val OneDayMillis = 24L * 60L * 60L * 1000L

/**
 * Typed task detail entrypoint with a desktop-first operator layout.
 */
@Composable
fun TaskDetailScreen(
    viewModel: MainViewModel,
    taskId: Long,
    onBack: () -> Unit,
    onNavigateToNode: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    isDesktop: Boolean = false,
) {
    val nodes by viewModel.allNodes.collectAsState()
    val areas by viewModel.allAreas.collectAsState()
    val projects by viewModel.allProjects.collectAsState()

    val nodeWithPin = remember(nodes, taskId) { nodes.find { it.node.id == taskId } }

    if (nodeWithPin == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(message = stringResource(Res.string.tasks_no_results))
        }
        return
    }

    val task = nodeWithPin.node
    if (!task.isTaskItem()) {
        NoteDetailScreen(
            viewModel = viewModel,
            noteId = taskId,
            onBack = onBack,
            onNavigateToNode = onNavigateToNode,
            onNavigateToSearch = onNavigateToSearch,
            isDesktop = isDesktop,
        )
        return
    }

    val tags by viewModel.getTagsForNode(taskId).collectAsState(initial = emptyList())
    val relations by viewModel.getRelationsForNode(taskId).collectAsState(initial = emptyList())
    val logs by viewModel.getLogsForNode(taskId).collectAsState(initial = emptyList())
    val attachments by viewModel.getAttachmentsForNode(taskId).collectAsState(initial = emptyList())

    val allNodeById = remember(nodes) { nodes.associateBy { it.node.id } }
    val areaById = remember(areas) { areas.associate { it.id to it.title } }
    val projectById = remember(projects) { projects.associate { it.id to it.title } }

    val subtasks =
        remember(task.content, nodes, relations) {
            val linkedSubtasks =
                relations
                    .asSequence()
                    .filter {
                        it.fromNodeId == task.id &&
                            it.relationType == "DEPENDS_ON"
                    }.mapNotNull { relation -> allNodeById[relation.toNodeId]?.node }
                    .toList()

            val childSubtasks =
                nodes
                    .map { it.node }
                    .filter { node ->
                        node.parentNodeId == task.id &&
                            node.isTaskItem()
                    }

            val subtaskNodes =
                (linkedSubtasks + childSubtasks)
                    .filterNot { it.status == TaskState.ARCHIVED.storageKey }
                    .distinctBy { it.id }
                    .sortedBy { it.createdAt }

            if (subtaskNodes.isNotEmpty()) {
                subtaskNodes.map { it.toSubtaskUi() }
            } else {
                parseInlineChecklist(task.content)
            }
        }

    val historyItems = remember(logs) { logs.take(12).map { it.toHistoryUi() } }
    val attachmentItems = remember(attachments) { attachments.map { it.toAttachmentUi() } }

    val doneSubtasks = subtasks.count { it.state == TaskSubtaskState.COMPLETE }
    val subtaskProgress =
        if (subtasks.isNotEmpty()) doneSubtasks.toFloat() / subtasks.size.toFloat() else null

    var isEditing by remember(task.id) { mutableStateOf(false) }
    var draftTitle by remember(task.id) { mutableStateOf(task.title) }
    var draftDescription by remember(task.id) { mutableStateOf(task.content) }

    LaunchedEffect(task.id, task.title, task.content, isEditing) {
        if (!isEditing) {
            draftTitle = task.title
            draftDescription = task.content
        }
    }

    val onSaveEdits = {
        val newTitle = draftTitle.trim()
        val updatedTask =
            task.copy(
                title = newTitle.ifBlank { task.title },
                content = draftDescription.trim(),
            )
        viewModel.updateNode(updatedTask)
        isEditing = false
    }

    val context =
        TaskDetailContext(
            viewModel = viewModel,
            task = task,
            tags = tags,
            relations = relations,
            attachments = attachments,
            allNodeById = allNodeById,
            areaById = areaById,
            projectById = projectById,
            subtasks = subtasks,
            historyItems = historyItems,
            attachmentItems = attachmentItems,
            doneSubtasksCount = doneSubtasks,
            totalSubtasksCount = subtasks.size,
            subtaskProgress = subtaskProgress,
            isEditing = isEditing,
            draftTitle = draftTitle,
            draftDescription = draftDescription,
            onDraftTitleChange = { draftTitle = it },
            onDraftDescriptionChange = { draftDescription = it },
            onToggleEdit = {
                if (isEditing) {
                    draftTitle = task.title
                    draftDescription = task.content
                }
                isEditing = !isEditing
            },
            onSaveEdits = onSaveEdits,
            onCancelEdits = {
                draftTitle = task.title
                draftDescription = task.content
                isEditing = false
            },
            onSnooze = {
                val baseDue =
                    task.dueAt ?: Clock.System
                        .now()
                        .toEpochMilliseconds()
                viewModel.updateNode(task.copy(dueAt = baseDue + OneDayMillis))
            },
            onComplete = {
                val newStatus =
                    if (task.taskStateOrNull() == TaskState.DONE) {
                        TaskState.ACTIVE.storageKey
                    } else {
                        TaskState.DONE.storageKey
                    }
                viewModel.updateNodeStatus(task, newStatus)
            },
            onToggleSubtask = { subtask ->
                if (subtask.source == TaskSubtaskSource.Node) {
                    subtask.node?.let { child ->
                        val newState =
                            if (child.taskStateOrNull() == TaskState.DONE) {
                                TaskState.ACTIVE.storageKey
                            } else {
                                TaskState.DONE.storageKey
                            }
                        viewModel.updateNodeStatus(child, newState)
                    }
                }
            },
            onRemoveAttachment = { attachmentId ->
                attachments.firstOrNull { it.id == attachmentId }?.let(viewModel::deleteAttachment)
            },
        )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (isDesktop && maxWidth >= TaskDetailWideLayoutBreakpoint.dp) {
                TaskDetailSurface.DESKTOP
            } else {
                TaskDetailSurface.MOBILE
            }

        val plan = remember(surface) { buildTaskDetailPlan(surface) }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = TactileTheme.SpacingLg,
                        vertical = TactileTheme.SpacingMd,
                    ),
            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingLg),
        ) {
            if (surface == TaskDetailSurface.DESKTOP) {
                // Header is first in primary, but we want it full width above the split
                TaskDetailBlockRegistry.resolve("task_header")?.invoke(context)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(2f),
                        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                    ) {
                        plan.primary.filterNot { it.id == "task_header" }.forEach { block ->
                            TaskDetailBlockRegistry.resolve(block.id)?.invoke(context)
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                    ) {
                        plan.secondary.forEach { block ->
                            TaskDetailBlockRegistry.resolve(block.id)?.invoke(context)
                        }
                    }
                }
            } else {
                plan.primary.forEach { block ->
                    TaskDetailBlockRegistry.resolve(block.id)?.invoke(context)
                }
            }
        }
    }
}

private fun NodeEntity.toSubtaskUi(): TaskSubtaskUi {
    val state =
        when (taskStateOrNull())
        {
            TaskState.DONE -> TaskSubtaskState.COMPLETE
            TaskState.ACTIVE -> TaskSubtaskState.ACTIVE
            else -> TaskSubtaskState.QUEUED
        }

    return TaskSubtaskUi(
        id = id,
        title = title,
        state = state,
        source = TaskSubtaskSource.Node,
        node = this,
    )
}

private fun parseInlineChecklist(content: String): List<TaskSubtaskUi> {
    val checklistLines =
        content.lines().mapNotNull { line ->
            val value = line.trim()
            when
                {
                    value.startsWith("- [x] ", ignoreCase = true) -> {
                        TaskSubtaskUi(
                            id = value.hashCode().toLong(),
                            title = value.removePrefix("- [x] ").trim(),
                            state = TaskSubtaskState.COMPLETE,
                            source = TaskSubtaskSource.InlineChecklist,
                        )
                    }

                    value.startsWith("- [ ] ") -> {
                        TaskSubtaskUi(
                            id = value.hashCode().toLong(),
                            title = value.removePrefix("- [ ] ").trim(),
                            state = TaskSubtaskState.QUEUED,
                            source = TaskSubtaskSource.InlineChecklist,
                        )
                    }

                    else -> {
                        null
                    }
                }
        }

    if (checklistLines.isEmpty()) return emptyList()

    val firstQueuedIndex = checklistLines.indexOfFirst { it.state == TaskSubtaskState.QUEUED }
    if (firstQueuedIndex < 0) return checklistLines

    return checklistLines.mapIndexed { index, item ->
        if (index == firstQueuedIndex) {
            item.copy(state = TaskSubtaskState.ACTIVE)
        } else {
            item
        }
    }
}

private fun AttachmentEntity.toAttachmentUi(): TaskAttachmentUi {
    val displayTitle =
        title?.takeIf { it.isNotBlank() } ?: uriOrPath
            .substringAfterLast('/')
            .substringAfterLast('\\')
    val metadata = metadataJson?.let(::parseAttachmentMetadata).orEmpty()
    val size = metadata["size"] ?: metadata["bytes"]?.toLongOrNull()?.let(::formatBytes)
    val normalizedType = mimeType ?: metadata["mimeType"] ?: assetType.uppercase()

    return TaskAttachmentUi(
        id = id,
        title = displayTitle.ifBlank { "Attachment $id" },
        typeLabel = normalizedType,
        sizeLabel = size,
    )
}

private fun EventLogEntity.toHistoryUi(): TaskHistoryUi {
    val normalized =
        eventType
            .replace('_', ' ')
            .lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    return TaskHistoryUi(
        id = id,
        title = normalized,
        subtitle = null,
        timestampLabel = formatTime(timestamp),
    )
}

private fun formatTime(epochMillis: Long): String {
    val local =
        Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    val hour =
        local.time.hour
            .toString()
            .padStart(2, '0')
    val minute =
        local.time.minute
            .toString()
            .padStart(2, '0')
    return "$hour:$minute"
}

private fun parseAttachmentMetadata(payload: String): Map<String, String> =
    runCatching {
        Json.parseToJsonElement(payload).jsonObject.mapValues { (_, value) -> value.jsonPrimitive.content }
    }.getOrDefault(emptyMap())

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = 1024.0
    val mb = kb * 1024.0
    val gb = mb * 1024.0
    return when
        {
            bytes >= gb -> "${(bytes / gb * 10).roundToInt() / 10.0} GB"
            bytes >= mb -> "${(bytes / mb * 10).roundToInt() / 10.0} MB"
            bytes >= kb -> "${(bytes / kb * 10).roundToInt() / 10.0} KB"
            else -> "$bytes B"
        }
}
