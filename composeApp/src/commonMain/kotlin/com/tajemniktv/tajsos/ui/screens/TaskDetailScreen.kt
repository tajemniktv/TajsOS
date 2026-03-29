/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

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
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.isTaskItem
import com.tajemniktv.tajsos.data.taskStateOrNull
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.tasks_no_results
import kotlin.time.Clock

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
            if (nodes.isEmpty()) {
                EmptyState(message = stringResource(Res.string.tasks_no_results))
            } else {
                EmptyState(message = stringResource(Res.string.tasks_no_results))
            }
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

    val linkedSubtasks =
        remember(task.id, relations, allNodeById) {
            relations
                .asSequence()
                .filter {
                    it.fromNodeId == task.id &&
                        it.relationType == "DEPENDS_ON"
                }.mapNotNull { relation -> allNodeById[relation.toNodeId]?.node }
                .toList()
        }

    val childSubtasks =
        remember(nodes, task.id) {
            nodes.map { it.node }
                .filter { node ->
                    node.parentNodeId == task.id &&
                        node.isTaskItem()
                }
        }

    val subtaskNodes =
        remember(linkedSubtasks, childSubtasks) {
            (linkedSubtasks + childSubtasks)
                .filterNot { it.status == TaskState.ARCHIVED.storageKey }
                .distinctBy { it.id }
                .sortedBy { it.createdAt }
        }

    val subtasks =
        remember(task.content, subtaskNodes) {
            if (subtaskNodes.isNotEmpty()) {
                subtaskNodes.map { it.toSubtaskUi() }
            } else {
                parseInlineChecklist(task.content)
            }
        }

    val historyItems = remember(logs) { logs.take(12).map { it.toHistoryUi() } }
    val attachmentItems = remember(attachments) { attachments.map { it.toAttachmentUi() } }

    val doneSubtasks = subtasks.count { it.state == TaskSubtaskState.COMPLETE }
    val subtaskProgress = if (subtasks.isNotEmpty()) doneSubtasks.toFloat() / subtasks.size.toFloat() else null

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

    val onSnooze = {
        val baseDue = task.dueAt ?: Clock.System.now().toEpochMilliseconds()
        viewModel.updateNode(task.copy(dueAt = baseDue + OneDayMillis))
    }

    val onComplete = {
        val newStatus =
            if (task.taskStateOrNull() == TaskState.DONE) {
                TaskState.ACTIVE.storageKey
            } else {
                TaskState.DONE.storageKey
            }
        viewModel.updateNodeStatus(task, newStatus)
    }

    val mainModifier =
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = TactileTheme.SpacingLg, vertical = TactileTheme.SpacingMd)

    BoxWithConstraints(modifier = mainModifier) {
        val useWideLayout = isDesktop && maxWidth >= TaskDetailWideLayoutBreakpoint.dp
        val railModifier = if (useWideLayout) Modifier else Modifier.fillMaxWidth()

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingLg),
        ) {
            TaskDetailsHeader(
                task = task,
                areaName = task.areaId?.let { areaById[it] },
                projectName = task.projectId?.let { projectById[it] },
                tags = tags.map { it.name },
                isEditing = isEditing,
                draftTitle = draftTitle,
                onDraftTitleChange = { draftTitle = it },
                onToggleEdit = {
                    if (isEditing) {
                        draftTitle = task.title
                        draftDescription = task.content
                    }
                    isEditing = !isEditing
                },
                onSnooze = onSnooze,
                onComplete = onComplete,
                onSave = onSaveEdits,
                onCancel = {
                    draftTitle = task.title
                    draftDescription = task.content
                    isEditing = false
                },
            )

            if (useWideLayout) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(2f),
                        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                    ) {
                        TaskDescriptionCard(
                            description = task.content,
                            isEditing = isEditing,
                            draftDescription = draftDescription,
                            onDraftDescriptionChange = { draftDescription = it },
                        )
                        TaskSubtaskSection(
                            subtasks = subtasks,
                            completedCount = doneSubtasks,
                            totalCount = subtasks.size,
                            sectionProgress = subtaskProgress,
                            onToggleSubtask = { subtask ->
                                when (subtask.source) {
                                    TaskSubtaskSource.Node -> {
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

                                    TaskSubtaskSource.InlineChecklist -> Unit
                                }
                            },
                        )
                        TaskAttachmentSection(
                            attachments = attachmentItems,
                            onRemoveAttachment = { attachmentId ->
                                attachments.firstOrNull { it.id == attachmentId }?.let(viewModel::deleteAttachment)
                            },
                        )
                        TaskHistorySection(historyItems = historyItems)
                    }

                    TaskMetadataPanel(
                        modifier = Modifier.weight(1f),
                        task = task,
                        areaName = task.areaId?.let { areaById[it] },
                        projectName = task.projectId?.let { projectById[it] },
                        subtaskProgress = subtaskProgress,
                        subtaskSummary = if (subtasks.isNotEmpty()) "$doneSubtasks/${subtasks.size}" else null,
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                ) {
                    TaskDescriptionCard(
                        description = task.content,
                        isEditing = isEditing,
                        draftDescription = draftDescription,
                        onDraftDescriptionChange = { draftDescription = it },
                    )
                    TaskMetadataPanel(
                        modifier = railModifier,
                        task = task,
                        areaName = task.areaId?.let { areaById[it] },
                        projectName = task.projectId?.let { projectById[it] },
                        subtaskProgress = subtaskProgress,
                        subtaskSummary = if (subtasks.isNotEmpty()) "$doneSubtasks/${subtasks.size}" else null,
                    )
                    TaskSubtaskSection(
                        subtasks = subtasks,
                        completedCount = doneSubtasks,
                        totalCount = subtasks.size,
                        sectionProgress = subtaskProgress,
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
                    )
                    TaskAttachmentSection(
                        attachments = attachmentItems,
                        onRemoveAttachment = { attachmentId ->
                            attachments.firstOrNull { it.id == attachmentId }?.let(viewModel::deleteAttachment)
                        },
                    )
                    TaskHistorySection(historyItems = historyItems)
                }
            }
        }
    }
}

private fun NodeEntity.toSubtaskUi(): TaskSubtaskUi {
    val state =
        when (taskStateOrNull()) {
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
            when {
                value.startsWith("- [x] ", ignoreCase = true) ->
                    TaskSubtaskUi(
                        id = value.hashCode().toLong(),
                        title = value.removePrefix("- [x] ").trim(),
                        state = TaskSubtaskState.COMPLETE,
                        source = TaskSubtaskSource.InlineChecklist,
                    )

                value.startsWith("- [ ] ") ->
                    TaskSubtaskUi(
                        id = value.hashCode().toLong(),
                        title = value.removePrefix("- [ ] ").trim(),
                        state = TaskSubtaskState.QUEUED,
                        source = TaskSubtaskSource.InlineChecklist,
                    )

                else -> null
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
