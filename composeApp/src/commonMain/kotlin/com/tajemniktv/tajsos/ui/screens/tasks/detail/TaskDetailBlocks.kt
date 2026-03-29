/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.tasks.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.taskStateOrNull
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val detailPanelShape = RoundedCornerShape(12.dp)
private val detailPanelBorder = TactileTheme.GhostBorder.copy(alpha = 0.22f)

private enum class HeaderBadgeTone {
    Critical,
    Stable,
    Neutral,
}

object TaskDetailBlockRegistry {
    private val renderers: Map<String, TaskDetailBlockRenderer> =
        mapOf(
            "task_header" to ::renderTaskHeader,
            "task_description" to ::renderTaskDescription,
            "task_metadata" to ::renderTaskMetadata,
            "task_subtasks" to ::renderTaskSubtasks,
            "task_attachments" to ::renderTaskAttachments,
            "task_history" to ::renderTaskHistory,
        )

    fun resolve(id: String): TaskDetailBlockRenderer? = renderers[id]
}

@Composable
private fun renderTaskHeader(context: TaskDetailContext) {
    val task = context.task
    val state = task.taskStateOrNull() ?: TaskState.ACTIVE
    val badgeTone =
        when
            {
                task.isHardDeadline -> HeaderBadgeTone.Critical
                state == TaskState.DONE -> HeaderBadgeTone.Stable
                else -> HeaderBadgeTone.Neutral
            }

    val badgeLabel =
        when
            {
                task.isHardDeadline -> "CRITICAL"
                state == TaskState.DONE -> "COMPLETE"
                state == TaskState.BLOCKED -> "BLOCKED"
                state == TaskState.ON_HOLD -> "ON HOLD"
                state == TaskState.SOMEDAY -> "QUEUED"
                else -> "ACTIVE"
            }

    Surface(
        color = TactileTheme.SurfaceLow,
        shape = detailPanelShape,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, detailPanelBorder),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TaskPathRow(
                        areaName = context.areaById[task.areaId ?: -1],
                        projectName = context.projectById[task.projectId ?: -1],
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TaskStateBadge(label = badgeLabel, tone = badgeTone)
                        Text(
                            text = "TASK-${task.id}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Muted,
                        )
                    }
                    if (context.isEditing) {
                        HeaderTitleEditor(
                            value = context.draftTitle,
                            onValueChange = context.onDraftTitleChange,
                        )
                    } else {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.displayMedium,
                            color = TactileTheme.Text,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                TaskActionBar(
                    isEditing = context.isEditing,
                    canComplete = state != TaskState.DONE,
                    onToggleEdit = context.onToggleEdit,
                    onSnooze = context.onSnooze,
                    onComplete = context.onComplete,
                    onSave = context.onSaveEdits,
                    onCancel = context.onCancelEdits,
                )
            }

            if (context.tags.isNotEmpty()) {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    context.tags.take(6).forEach { tag ->
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Tag,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = TactileTheme.Primary,
                                )
                            },
                            label = { Text(tag.name) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskPathRow(
    areaName: String?,
    projectName: String?,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "TASKS",
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = TactileTheme.Muted,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = areaName ?: "UNSCOPED",
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Primary,
        )
        projectName?.let {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = TactileTheme.Muted,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = it.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HeaderTitleEditor(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = TactileTheme.SurfaceHighest,
        border = BorderStroke(1.dp, TactileTheme.GhostBorder.copy(alpha = 0.25f)),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            textStyle = MaterialTheme.typography.displaySmall.copy(color = TactileTheme.Text),
            cursorBrush = SolidColor(TactileTheme.Primary),
            singleLine = true,
        )
    }
}

@Composable
private fun TaskActionBar(
    isEditing: Boolean,
    canComplete: Boolean,
    onToggleEdit: () -> Unit,
    onSnooze: () -> Unit,
    onComplete: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (isEditing) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCancel) {
                    Text("Cancel")
                }
                Button(onClick = onSave) {
                    Text("Save")
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onToggleEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Edit")
                }
                OutlinedButton(onClick = onSnooze) {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Snooze")
                }
                Button(
                    onClick = onComplete,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = TactileTheme.Primary,
                            contentColor = TactileTheme.Background,
                            disabledContainerColor = TactileTheme.SurfaceHighest,
                            disabledContentColor = TactileTheme.Muted,
                        ),
                    enabled = canComplete,
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (canComplete) "Complete Task" else "Completed")
                }
            }
        }
    }
}

@Composable
private fun renderTaskDescription(context: TaskDetailContext) {
    Surface(
        color = TactileTheme.SurfaceLow,
        shape = detailPanelShape,
        border = BorderStroke(1.dp, detailPanelBorder),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionTitle("Technical Directives")
            if (context.isEditing) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = TactileTheme.SurfaceHighest,
                    border = BorderStroke(1.dp, TactileTheme.GhostBorder.copy(alpha = 0.22f)),
                ) {
                    BasicTextField(
                        value = context.draftDescription,
                        onValueChange = context.onDraftDescriptionChange,
                        modifier = Modifier.fillMaxWidth().height(160.dp).padding(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TactileTheme.Text),
                        cursorBrush = SolidColor(TactileTheme.Primary),
                    )
                }
            } else {
                val clean = context.task.content.trim()
                if (clean.isBlank()) {
                    Text(
                        text = "No execution notes yet. Add constraints, dependencies, or operator notes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TactileTheme.Muted,
                    )
                } else {
                    Text(
                        text = clean,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TactileTheme.Text,
                    )
                }
            }

            @OptIn(ExperimentalLayoutApi::class)
            val chips =
                extractDescriptionChips(if (context.isEditing) context.draftDescription else context.task.content)
            if (chips.isNotEmpty()) {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    chips.take(6).forEach { chip ->
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = TactileTheme.Primary.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, TactileTheme.Primary.copy(alpha = 0.35f)),
                        ) {
                            Text(
                                text = chip,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = TactileTheme.Primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun renderTaskSubtasks(context: TaskDetailContext) {
    Surface(
        color = TactileTheme.SurfaceLow,
        shape = detailPanelShape,
        border = BorderStroke(1.dp, detailPanelBorder),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionTitle("Subtask Matrix")
                if (context.totalSubtasksCount > 0) {
                    Text(
                        text = "${context.doneSubtasksCount} of ${context.totalSubtasksCount} complete",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                    )
                }
            }

            if (context.subtaskProgress != null) {
                LinearProgressIndicator(
                    progress = { context.subtaskProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = TactileTheme.Primary,
                    trackColor = TactileTheme.SurfaceHighest,
                )
            }

            if (context.subtasks.isEmpty()) {
                Text(
                    text = "No subtasks yet. Add checklist lines in description or split this task into subtasks.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TactileTheme.Muted,
                )
            } else {
                context.subtasks.forEach { subtask ->
                    SubtaskRow(subtask = subtask, onToggle = { context.onToggleSubtask(subtask) })
                }
            }
        }
    }
}

@Composable
private fun SubtaskRow(
    subtask: TaskSubtaskUi,
    onToggle: () -> Unit,
) {
    val (icon, tint, background) =
        when (subtask.state)
        {
            TaskSubtaskState.COMPLETE -> {
                Triple(
                    Icons.Outlined.CheckCircleOutline,
                    TactileTheme.Primary,
                    TactileTheme.Primary.copy(alpha = 0.12f),
                )
            }

            TaskSubtaskState.ACTIVE -> {
                Triple(
                    Icons.Default.RadioButtonChecked,
                    TactileTheme.AccentBlue,
                    TactileTheme.AccentBlue.copy(alpha = 0.1f),
                )
            }

            TaskSubtaskState.QUEUED -> {
                Triple(
                    Icons.Outlined.Circle,
                    TactileTheme.Muted,
                    TactileTheme.SurfaceHighest,
                )
            }
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = background,
        border = BorderStroke(1.dp, TactileTheme.GhostBorder.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier =
                    Modifier
                        .weight(1f)
                        .clickable(enabled = subtask.source == TaskSubtaskSource.Node) { onToggle() },
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, tint = tint)
                Text(
                    text = subtask.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (subtask.state == TaskSubtaskState.COMPLETE) TactileTheme.Muted else TactileTheme.Text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text =
                    when (subtask.state)
                    {
                        TaskSubtaskState.COMPLETE -> "Done"
                        TaskSubtaskState.ACTIVE -> "In Progress"
                        TaskSubtaskState.QUEUED -> "Queued"
                    },
                style = MaterialTheme.typography.labelSmall,
                color = tint,
            )
        }
    }
}

@Composable
private fun renderTaskAttachments(context: TaskDetailContext) {
    Surface(
        color = TactileTheme.SurfaceLow,
        shape = detailPanelShape,
        border = BorderStroke(1.dp, detailPanelBorder),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionTitle("Attachments")
            if (context.attachmentItems.isEmpty()) {
                Text(
                    text = "No attachments linked to this task.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TactileTheme.Muted,
                )
            } else {
                context.attachmentItems.forEachIndexed { index, attachment ->
                    AttachmentRow(
                        attachment = attachment,
                        onRemove = { context.onRemoveAttachment(attachment.id) },
                    )
                    if (index < context.attachmentItems.lastIndex) {
                        HorizontalDivider(color = TactileTheme.GhostBorder.copy(alpha = 0.15f))
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentRow(
    attachment: TaskAttachmentUi,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Attachment,
                contentDescription = null,
                tint = TactileTheme.Primary,
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = attachment.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TactileTheme.Text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text =
                        listOfNotNull(
                            attachment.typeLabel,
                            attachment.sizeLabel,
                        ).joinToString(" • "),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted,
                )
            }
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = TactileTheme.Muted)
        }
    }
}

@Composable
private fun renderTaskHistory(context: TaskDetailContext) {
    Surface(
        color = TactileTheme.SurfaceLow,
        shape = detailPanelShape,
        border = BorderStroke(1.dp, detailPanelBorder),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionTitle("History")
            if (context.historyItems.isEmpty()) {
                Text(
                    text = "No tracked activity yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TactileTheme.Muted,
                )
            } else {
                context.historyItems.forEach { item ->
                    HistoryRow(item)
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(item: TaskHistoryUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Default.Circle,
                contentDescription = null,
                tint = TactileTheme.Primary.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp).size(8.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TactileTheme.Text,
                    fontWeight = FontWeight.SemiBold,
                )
                item.subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted,
                    )
                }
            }
        }
        Text(
            text = item.timestampLabel,
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted,
        )
    }
}

@Composable
private fun renderTaskMetadata(context: TaskDetailContext) {
    val task = context.task
    val areaName = context.areaById[task.areaId ?: -1]
    val projectName = context.projectById[task.projectId ?: -1]
    val state = task.taskStateOrNull() ?: TaskState.ACTIVE

    Surface(
        color = TactileTheme.SurfaceLow,
        shape = detailPanelShape,
        border = BorderStroke(1.dp, detailPanelBorder),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "System Status",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted,
                )
                StatusPill(state)
            }

            HorizontalDivider(color = TactileTheme.GhostBorder.copy(alpha = 0.15f))

            TaskPropertyRow(
                icon = Icons.Default.Info,
                label = "Domain",
                value = areaName ?: "Not set",
            )
            TaskPropertyRow(
                icon = Icons.Default.Tag,
                label = "Project",
                value = projectName ?: "Not set",
            )
            TaskPropertyRow(
                icon = Icons.Default.CalendarMonth,
                label = "Deadline",
                value = task.dueAt?.let(::formatDateTime) ?: "No due date",
            )
            TaskPropertyRow(
                icon = Icons.Default.Schedule,
                label = "Frequency",
                value =
                    if (task.isRecurring) {
                        task.recurringInterval?.replace("_", " ")?.uppercase() ?: "Recurring"
                    } else {
                        "One-off"
                    },
            )
            TaskPropertyRow(
                icon = Icons.Default.HourglassBottom,
                label = "Effort",
                value = task.estimatedMinutes?.let { "$it min" } ?: "Unestimated",
            )
            TaskPropertyRow(
                icon = Icons.Default.AlarmOn,
                label = "Automation",
                value = if (task.isRecurring) "Active" else "Off",
            )
            TaskPropertyRow(
                icon = Icons.Default.RadioButtonChecked,
                label = "Priority",
                value = if (task.isHardDeadline) "Critical" else derivePriorityLabel(task),
            )

            if (context.subtaskProgress != null) {
                HorizontalDivider(color = TactileTheme.GhostBorder.copy(alpha = 0.15f))
                Text(
                    text = "Execution Progress",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted,
                )
                LinearProgressIndicator(
                    progress = { context.subtaskProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = TactileTheme.Primary,
                    trackColor = TactileTheme.SurfaceHighest,
                )
                Text(
                    text = "${context.doneSubtasksCount}/${context.totalSubtasksCount} subtasks complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted,
                )
            }
        }
    }
}

@Composable
private fun StatusPill(state: TaskState) {
    val (label, tint) =
        when (state)
        {
            TaskState.ACTIVE -> "ACTIVE" to TactileTheme.AccentBlue
            TaskState.DONE -> "DONE" to TactileTheme.AccentGreen
            TaskState.BLOCKED -> "BLOCKED" to TactileTheme.Error
            TaskState.ON_HOLD -> "ON HOLD" to TactileTheme.AccentAmber
            TaskState.SOMEDAY -> "QUEUED" to TactileTheme.Muted
            TaskState.ARCHIVED -> "ARCHIVED" to TactileTheme.Muted
        }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tint.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = tint,
        )
    }
}

@Composable
private fun TaskPropertyRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = TactileTheme.Muted,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted,
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Text,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = TactileTheme.Primary,
    )
}

private fun extractDescriptionChips(text: String): List<String> {
    val hashTags = "#([A-Za-z0-9_\\-]+)".toRegex().findAll(text).map { "#${it.groupValues[1]}" }
    val mentions = "@([A-Za-z0-9_\\-]+)".toRegex().findAll(text).map { "@${it.groupValues[1]}" }
    return (hashTags + mentions).distinct().take(8).toList()
}

private fun derivePriorityLabel(task: com.tajemniktv.tajsos.data.NodeEntity): String {
    if (task.isHardDeadline) return "Critical"
    if (task.energyLevel == 3) return "High intensity"
    if (task.friction == "mentally_heavy") return "High friction"
    val dueAt = task.dueAt
    if (dueAt != null && dueAt <
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds()
    ) {
        return "Overdue"
    }
    return "Standard"
}

private fun formatDateTime(epochMillis: Long): String {
    val local =
        Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    val date = local.date.toString()
    val hour =
        local.time.hour
            .toString()
            .padStart(2, '0')
    val minute =
        local.time.minute
            .toString()
            .padStart(2, '0')
    return "$date $hour:$minute"
}

@Composable
private fun TaskStateBadge(
    label: String,
    tone: HeaderBadgeTone,
) {
    val tint =
        when (tone)
        {
            HeaderBadgeTone.Critical -> TactileTheme.Error
            HeaderBadgeTone.Stable -> TactileTheme.AccentGreen
            HeaderBadgeTone.Neutral -> TactileTheme.Primary
        }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tint.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = tint,
        )
    }
}
