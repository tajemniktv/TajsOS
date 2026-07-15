/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.tasks.detail

import com.tajemniktv.tajsos.ui.components.TactileOutlinedTextField
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.taskStateOrNull
import com.tajemniktv.tajsos.ui.components.common.mouseClickable
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.capture_interval_daily
import tajsos.composeapp.generated.resources.capture_interval_monthly
import tajsos.composeapp.generated.resources.capture_interval_weekly
import tajsos.composeapp.generated.resources.common_add
import tajsos.composeapp.generated.resources.common_cancel
import tajsos.composeapp.generated.resources.common_edit
import tajsos.composeapp.generated.resources.common_save
import tajsos.composeapp.generated.resources.dash_system_status
import tajsos.composeapp.generated.resources.detail_area
import tajsos.composeapp.generated.resources.detail_attachments
import tajsos.composeapp.generated.resources.detail_due_at
import tajsos.composeapp.generated.resources.detail_estimate_mins
import tajsos.composeapp.generated.resources.detail_no_due_date
import tajsos.composeapp.generated.resources.detail_not_set
import tajsos.composeapp.generated.resources.detail_one_time
import tajsos.composeapp.generated.resources.detail_project
import tajsos.composeapp.generated.resources.detail_split_subtasks
import tajsos.composeapp.generated.resources.detail_status
import tajsos.composeapp.generated.resources.detail_tag_new_placeholder
import tajsos.composeapp.generated.resources.protocol_no_steps
import tajsos.composeapp.generated.resources.protocol_steps_complete
import tajsos.composeapp.generated.resources.screen_history
import tajsos.composeapp.generated.resources.screen_tasks
import tajsos.composeapp.generated.resources.screen_today
import tajsos.composeapp.generated.resources.task_detail_active
import tajsos.composeapp.generated.resources.task_detail_blocked
import tajsos.composeapp.generated.resources.task_detail_complete
import tajsos.composeapp.generated.resources.task_detail_complete_action
import tajsos.composeapp.generated.resources.task_detail_completed_status
import tajsos.composeapp.generated.resources.task_detail_critical
import tajsos.composeapp.generated.resources.task_detail_directives_section
import tajsos.composeapp.generated.resources.task_detail_id_label
import tajsos.composeapp.generated.resources.task_detail_no_attachments
import tajsos.composeapp.generated.resources.task_detail_no_notes
import tajsos.composeapp.generated.resources.task_detail_on_hold
import tajsos.composeapp.generated.resources.task_detail_queued
import tajsos.composeapp.generated.resources.task_detail_snooze
import tajsos.composeapp.generated.resources.tasks_detail_updated
import tajsos.composeapp.generated.resources.track_empty

private val detailPanelShape = RoundedCornerShape(TajsOSTheme.RadiusMd)
private val detailPanelBorder = TajsOSTheme.GhostBorder.copy(alpha = 0.22f)

private enum class HeaderBadgeTone {
    Critical,
    Stable,
    Neutral,
}

object TaskDetailBlocks {
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
        when {
            task.isHardDeadline -> HeaderBadgeTone.Critical
            state == TaskState.DONE -> HeaderBadgeTone.Stable
            else -> HeaderBadgeTone.Neutral
        }

    val badgeLabel =
        when {
            task.isHardDeadline -> stringResource(Res.string.task_detail_critical)
            state == TaskState.DONE -> stringResource(Res.string.task_detail_complete)
            state == TaskState.BLOCKED -> stringResource(Res.string.task_detail_blocked)
            state == TaskState.ON_HOLD -> stringResource(Res.string.task_detail_on_hold)
            state == TaskState.SOMEDAY -> stringResource(Res.string.task_detail_queued)
            else -> stringResource(Res.string.task_detail_active)
        }

    Surface(
        color = TajsOSTheme.SurfaceLow,
        shape = detailPanelShape,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, detailPanelBorder),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
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
                        horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TaskStateBadge(label = badgeLabel, tone = badgeTone)
                        Text(
                            text =
                                stringResource(
                                    Res.string.task_detail_id_label,
                                    task.id.toString(),
                                ),
                            style = MaterialTheme.typography.labelSmall,
                            color = TajsOSTheme.Muted,
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
                            color = TajsOSTheme.Text,
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
                                    tint = TajsOSTheme.Primary,
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
            text = stringResource(Res.string.screen_tasks),
            style = MaterialTheme.typography.labelSmall,
            color = TajsOSTheme.Muted,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = TajsOSTheme.Muted,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = areaName ?: "UNSCOPED",
            style = MaterialTheme.typography.labelSmall,
            color = TajsOSTheme.Primary,
        )
        projectName?.let {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = TajsOSTheme.Muted,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = it.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Text,
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
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        color = TajsOSTheme.SurfaceHighest,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            textStyle = MaterialTheme.typography.displaySmall.copy(color = TajsOSTheme.Text),
            cursorBrush = SolidColor(TajsOSTheme.Primary),
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
                OutlinedButton(modifier = Modifier.pointerHoverIcon(PointerIcon.Hand), onClick = onCancel) {
                    Text(stringResource(Res.string.common_cancel))
                }
                Button(onClick = onSave) {
                    Text(stringResource(Res.string.common_save))
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(modifier = Modifier.pointerHoverIcon(PointerIcon.Hand), onClick = onToggleEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(Res.string.common_edit))
                }
                OutlinedButton(modifier = Modifier.pointerHoverIcon(PointerIcon.Hand), onClick = onSnooze) {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(Res.string.task_detail_snooze))
                }
                Button(
                    onClick = onComplete,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = TajsOSTheme.Primary,
                            contentColor = TajsOSTheme.Background,
                            disabledContainerColor = TajsOSTheme.SurfaceHighest,
                            disabledContentColor = TajsOSTheme.Muted,
                        ),
                    enabled = canComplete,
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (canComplete) {
                            stringResource(Res.string.task_detail_complete_action)
                        } else {
                            stringResource(Res.string.task_detail_completed_status)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun renderTaskDescription(context: TaskDetailContext) {
    Surface(
        color = TajsOSTheme.SurfaceLow,
        shape = detailPanelShape,
        border = BorderStroke(1.dp, detailPanelBorder),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionTitle(stringResource(Res.string.task_detail_directives_section))
            if (context.isEditing) {
                Surface(
                    shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                    color = TajsOSTheme.SurfaceHighest,
                ) {
                    BasicTextField(
                        value = context.draftDescription,
                        onValueChange = context.onDraftDescriptionChange,
                        modifier = Modifier.fillMaxWidth().height(160.dp).padding(12.dp),
                        textStyle =
                            MaterialTheme.typography.bodyLarge.copy(
                                color = TajsOSTheme.Text,
                            ),
                        cursorBrush = SolidColor(TajsOSTheme.Primary),
                    )
                }
            } else {
                val clean = context.task.content.trim()
                if (clean.isBlank()) {
                    Text(
                        text = stringResource(Res.string.task_detail_no_notes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TajsOSTheme.Muted,
                    )
                } else {
                    Text(
                        text = clean,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TajsOSTheme.Text,
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
                            color = TajsOSTheme.Primary.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, TajsOSTheme.GhostBorder),
                        ) {
                            Text(
                                text = chip,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = TajsOSTheme.Primary,
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
    var newChecklistItem by remember(context.task.id) { mutableStateOf("") }
    Surface(
        color = TajsOSTheme.SurfaceLow,
        shape = detailPanelShape,
        border = BorderStroke(1.dp, detailPanelBorder),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
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
                        text =
                            stringResource(
                                Res.string.protocol_steps_complete,
                                context.doneSubtasksCount.toString(),
                                context.totalSubtasksCount.toString(),
                            ),
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Muted,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(modifier = Modifier.pointerHoverIcon(PointerIcon.Hand), onClick = context.onSplitIntoSubtasks) {
                    Text(stringResource(Res.string.detail_split_subtasks))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TactileOutlinedTextField(
                    value = newChecklistItem,
                    onValueChange = { newChecklistItem = it },
                    containerModifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text(stringResource(Res.string.detail_tag_new_placeholder)) },
                )
                Button(
                    onClick = {
                        context.onAddInlineSubtask(newChecklistItem)
                        newChecklistItem = ""
                    },
                ) {
                    Text(stringResource(Res.string.common_add))
                }
            }

            val progressVal = context.subtaskProgress
            if (progressVal != null) {
                LinearProgressIndicator(
                    progress = { progressVal },
                    modifier = Modifier.fillMaxWidth(),
                    color = TajsOSTheme.Primary,
                    trackColor = TajsOSTheme.SurfaceHighest,
                )
            }

            if (context.subtasks.isEmpty()) {
                Text(
                    text = stringResource(Res.string.protocol_no_steps),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TajsOSTheme.Muted,
                )
            } else {
                context.subtasks.forEach { subtask ->
                    SubtaskRow(
                        subtask = subtask,
                        onToggle = { context.onToggleSubtask(subtask) },
                        onRemove = {
                            if (subtask.source == TaskSubtaskSource.InlineChecklist) {
                                context.onRemoveInlineSubtask(subtask)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SubtaskRow(
    subtask: TaskSubtaskUi,
    onToggle: () -> Unit,
    onRemove: (() -> Unit)? = null,
) {
    val (icon, tint, background) =
        when (subtask.state) {
            TaskSubtaskState.COMPLETE -> {
                Triple(
                    Icons.Outlined.CheckCircleOutline,
                    TajsOSTheme.Primary,
                    TajsOSTheme.Primary.copy(alpha = 0.12f),
                )
            }

            TaskSubtaskState.ACTIVE -> {
                Triple(
                    Icons.Default.RadioButtonChecked,
                    TajsOSTheme.AccentBlue,
                    TajsOSTheme.AccentBlue.copy(alpha = 0.1f),
                )
            }

            TaskSubtaskState.QUEUED -> {
                Triple(
                    Icons.Outlined.Circle,
                    TajsOSTheme.Muted,
                    TajsOSTheme.SurfaceHighest,
                )
            }
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        color = background,
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
                        .mouseClickable(
                            enabled = subtask.source == TaskSubtaskSource.Node,
                            onClick = onToggle,
                            onSecondaryClick = onToggle,
                            middleClickFallbackToPrimary = true,
                        ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, tint = tint)
                Text(
                    text = subtask.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (subtask.state ==
                            TaskSubtaskState.COMPLETE
                        ) {
                            TajsOSTheme.Muted
                        } else {
                            TajsOSTheme.Text
                        },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text =
                    when (subtask.state) {
                        TaskSubtaskState.COMPLETE -> "Done"
                        TaskSubtaskState.ACTIVE -> "In Progress"
                        TaskSubtaskState.QUEUED -> "Queued"
                    },
                style = MaterialTheme.typography.labelSmall,
                color = tint,
            )
            if (subtask.source == TaskSubtaskSource.InlineChecklist && onRemove != null) {
                IconButton(onClick = onRemove, modifier = Modifier.size(48.dp).pointerHoverIcon(PointerIcon.Hand)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = TajsOSTheme.Muted,
                    )
                }
            }
        }
    }
}

@Composable
private fun renderTaskAttachments(context: TaskDetailContext) {
    Surface(
        color = TajsOSTheme.SurfaceLow,
        shape = detailPanelShape,
        border = BorderStroke(1.dp, detailPanelBorder),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionTitle(stringResource(Res.string.detail_attachments))
            if (context.attachmentItems.isEmpty()) {
                Text(
                    text = stringResource(Res.string.task_detail_no_attachments),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TajsOSTheme.Muted,
                )
            } else {
                context.attachmentItems.forEachIndexed { index, attachment ->
                    AttachmentRow(
                        attachment = attachment,
                        onRemove = { context.onRemoveAttachment(attachment.id) },
                    )
                    if (index < context.attachmentItems.lastIndex) {
                        HorizontalDivider(color = TajsOSTheme.GhostBorder.copy(alpha = 0.15f))
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
                tint = TajsOSTheme.Primary,
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = attachment.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TajsOSTheme.Text,
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
                    color = TajsOSTheme.Muted,
                )
            }
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(48.dp).pointerHoverIcon(PointerIcon.Hand)) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = TajsOSTheme.Muted)
        }
    }
}

@Composable
private fun renderTaskHistory(context: TaskDetailContext) {
    Surface(
        color = TajsOSTheme.SurfaceLow,
        shape = detailPanelShape,
        border = BorderStroke(1.dp, detailPanelBorder),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionTitle(stringResource(Res.string.screen_history))
            if (context.historyItems.isEmpty()) {
                Text(
                    text = stringResource(Res.string.track_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TajsOSTheme.Muted,
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
                tint = TajsOSTheme.Primary.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp).size(8.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TajsOSTheme.Text,
                    fontWeight = FontWeight.SemiBold,
                )
                item.subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = TajsOSTheme.Muted,
                    )
                }
            }
        }
        Text(
            text = item.timestampLabel,
            style = MaterialTheme.typography.labelSmall,
            color = TajsOSTheme.Muted,
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
        color = TajsOSTheme.SurfaceLow,
        shape = detailPanelShape,
        border = BorderStroke(1.dp, detailPanelBorder),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.dash_system_status),
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Muted,
                )
                StatusPill(state)
            }

            HorizontalDivider(color = TajsOSTheme.GhostBorder.copy(alpha = 0.15f))

            RenderTaskMetadataProperties(context, state, areaName, projectName)

            val progressVal = context.subtaskProgress
            if (progressVal != null) {
                HorizontalDivider(color = TajsOSTheme.GhostBorder.copy(alpha = 0.15f))
                Text(
                    text = stringResource(Res.string.tasks_detail_updated),
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Muted,
                )
                LinearProgressIndicator(
                    progress = { progressVal },
                    modifier = Modifier.fillMaxWidth(),
                    color = TajsOSTheme.Primary,
                    trackColor = TajsOSTheme.SurfaceHighest,
                )
                Text(
                    text =
                        stringResource(
                            Res.string.protocol_steps_complete,
                            context.doneSubtasksCount.toString(),
                            context.totalSubtasksCount.toString(),
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                )
            }
        }
    }
}

@Composable
private fun RenderTaskMetadataProperties(
    context: TaskDetailContext,
    state: TaskState,
    areaName: String?,
    projectName: String?,
) {
    val task = context.task

    TaskEditablePropertyRow(
        icon = Icons.Default.Info,
        label = stringResource(Res.string.detail_status),
        selected = state.storageKey.replace("_", " ").uppercase(),
        options =
            listOf(
                TaskPropertyOption(
                    TaskState.ACTIVE.storageKey,
                    stringResource(Res.string.task_detail_active),
                ),
                TaskPropertyOption(
                    TaskState.BLOCKED.storageKey,
                    stringResource(Res.string.task_detail_blocked),
                ),
                TaskPropertyOption(
                    TaskState.ON_HOLD.storageKey,
                    stringResource(Res.string.task_detail_on_hold),
                ),
                TaskPropertyOption(
                    TaskState.SOMEDAY.storageKey,
                    stringResource(Res.string.task_detail_queued),
                ),
                TaskPropertyOption(
                    TaskState.DONE.storageKey,
                    stringResource(Res.string.task_detail_complete),
                ),
            ),
        onSelect = context.onStatusChange,
    )
    TaskEditablePropertyRow(
        icon = Icons.Default.Info,
        label = stringResource(Res.string.detail_area),
        selected = areaName ?: stringResource(Res.string.detail_not_set),
        options =
            listOf(
                TaskPropertyOption(
                    "__none__",
                    stringResource(Res.string.detail_not_set),
                ),
            ) +
                context.areas.map {
                    TaskPropertyOption(
                        it.first.toString(),
                        it.second,
                    )
                },
        onSelect = { value ->
            context.onAreaChange(value.toLongOrNull())
        },
    )
    TaskEditablePropertyRow(
        icon = Icons.Default.Tag,
        label = stringResource(Res.string.detail_project),
        selected = projectName ?: stringResource(Res.string.detail_not_set),
        options =
            listOf(
                TaskPropertyOption(
                    "__none__",
                    stringResource(Res.string.detail_not_set),
                ),
            ) +
                context.projects.map {
                    TaskPropertyOption(
                        it.first.toString(),
                        it.second,
                    )
                },
        onSelect = { value ->
            context.onProjectChange(value.toLongOrNull())
        },
    )
    TaskEditablePropertyRow(
        icon = Icons.Default.CalendarMonth,
        label = stringResource(Res.string.detail_due_at),
        selected =
            task.dueAt?.let(::formatDateTime)
                ?: stringResource(Res.string.detail_no_due_date),
        options =
            listOf(
                TaskPropertyOption(
                    TaskDuePreset.None.name,
                    stringResource(Res.string.detail_no_due_date),
                ),
                TaskPropertyOption(
                    TaskDuePreset.Today.name,
                    stringResource(Res.string.screen_today),
                ),
                TaskPropertyOption(TaskDuePreset.Tomorrow.name, "Tomorrow"),
                TaskPropertyOption(TaskDuePreset.InSevenDays.name, "In 7 days"),
            ),
        onSelect = { value ->
            try {
                context.onDuePresetChange(TaskDuePreset.valueOf(value))
            } catch (e: IllegalArgumentException) {
                // Ignore invalid values to prevent crashes
            }
        },
    )
    TaskEditablePropertyRow(
        icon = Icons.Default.Schedule,
        label = "Frequency",
        selected =
            if (task.isRecurring) {
                task.recurringInterval?.replace("_", " ")?.uppercase() ?: "Recurring"
            } else {
                stringResource(Res.string.detail_one_time)
            },
        options =
            listOf(
                TaskPropertyOption("__none__", stringResource(Res.string.detail_one_time)),
                TaskPropertyOption(
                    "daily",
                    stringResource(Res.string.capture_interval_daily),
                ),
                TaskPropertyOption(
                    "weekly",
                    stringResource(Res.string.capture_interval_weekly),
                ),
                TaskPropertyOption(
                    "monthly",
                    stringResource(Res.string.capture_interval_monthly),
                ),
            ),
        onSelect = { value ->
            context.onRecurrenceChange(if (value == "__none__") null else value)
        },
    )
    TaskEditablePropertyRow(
        icon = Icons.Default.HourglassBottom,
        label = "Effort",
        selected =
            task.estimatedMinutes?.let {
                stringResource(
                    Res.string.detail_estimate_mins,
                    it.toString(),
                )
            } ?: stringResource(Res.string.detail_not_set),
        options =
            listOf(
                TaskPropertyOption("__none__", stringResource(Res.string.detail_not_set)),
                TaskPropertyOption(
                    "15",
                    stringResource(Res.string.detail_estimate_mins, "15"),
                ),
                TaskPropertyOption(
                    "30",
                    stringResource(Res.string.detail_estimate_mins, "30"),
                ),
                TaskPropertyOption(
                    "60",
                    stringResource(Res.string.detail_estimate_mins, "60"),
                ),
                TaskPropertyOption(
                    "120",
                    stringResource(Res.string.detail_estimate_mins, "120"),
                ),
            ),
        onSelect = { value ->
            context.onEstimateChange(value.toIntOrNull())
        },
    )
    TaskPropertyRow(
        icon = Icons.Default.AlarmOn,
        label = "Automation",
        value = if (task.isRecurring) "Active" else "Off",
    )
    TaskEditablePropertyRow(
        icon = Icons.Default.RadioButtonChecked,
        label = "Priority",
        selected = if (task.isHardDeadline) "Critical" else derivePriorityLabel(task),
        options =
            listOf(
                TaskPropertyOption("false", "Standard"),
                TaskPropertyOption("true", "Critical"),
            ),
        onSelect = { value -> context.onCriticalityChange(value == "true") },
    )
}

private data class TaskPropertyOption(
    val key: String,
    val label: String,
)

@Composable
private fun StatusPill(state: TaskState) {
    val (label, tint) =
        when (state) {
            TaskState.ACTIVE -> "ACTIVE" to TajsOSTheme.AccentBlue
            TaskState.DONE -> "DONE" to TajsOSTheme.AccentGreen
            TaskState.BLOCKED -> "BLOCKED" to TajsOSTheme.Error
            TaskState.ON_HOLD -> "ON HOLD" to TajsOSTheme.AccentAmber
            TaskState.SOMEDAY -> "QUEUED" to TajsOSTheme.Muted
            TaskState.ARCHIVED -> "ARCHIVED" to TajsOSTheme.Muted
        }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tint.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, TajsOSTheme.GhostBorder),
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
                tint = TajsOSTheme.Muted,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Muted,
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = TajsOSTheme.Text,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TaskEditablePropertyRow(
    icon: ImageVector,
    label: String,
    selected: String,
    options: List<TaskPropertyOption>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
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
                tint = TajsOSTheme.Muted,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Muted,
            )
        }
        Box {
            TextButton(modifier = Modifier.pointerHoverIcon(PointerIcon.Hand), onClick = { expanded = true }) {
                Text(selected)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            onSelect(option.key)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = TajsOSTheme.Primary,
    )
}

private val HASHTAG_REGEX = "#([A-Za-z0-9_\\-]+)".toRegex()
private val MENTION_REGEX = "@([A-Za-z0-9_\\-]+)".toRegex()
private fun extractDescriptionChips(text: String): List<String> {
    val hashTags = HASHTAG_REGEX.findAll(text).map { "#${it.groupValues[1]}" }
    val mentions = MENTION_REGEX.findAll(text).map { "@${it.groupValues[1]}" }
    return (hashTags + mentions).distinct().take(8).toList()
}

private fun derivePriorityLabel(task: com.tajemniktv.tajsos.data.NodeEntity): String {
    if (task.isHardDeadline) return "Critical"
    if (task.energyLevel == 3) return "High intensity"
    if (task.friction == "mentally_heavy") return "High friction"
    val dueAt = task.dueAt
    if (dueAt != null &&
        dueAt <
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
        kotlin.time.Instant
            .fromEpochMilliseconds(epochMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
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
        when (tone) {
            HeaderBadgeTone.Critical -> TajsOSTheme.Error
            HeaderBadgeTone.Stable -> TajsOSTheme.AccentGreen
            HeaderBadgeTone.Neutral -> TajsOSTheme.Primary
        }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tint.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, TajsOSTheme.GhostBorder),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = tint,
        )
    }
}
