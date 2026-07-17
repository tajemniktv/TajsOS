/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.TemplateEntity
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.capture_assign_to_area
import tajsos.composeapp.generated.resources.capture_brain_dump_active
import tajsos.composeapp.generated.resources.capture_dump
import tajsos.composeapp.generated.resources.capture_interval_daily
import tajsos.composeapp.generated.resources.capture_interval_monthly
import tajsos.composeapp.generated.resources.capture_interval_weekly
import tajsos.composeapp.generated.resources.capture_label_capture
import tajsos.composeapp.generated.resources.capture_multi
import tajsos.composeapp.generated.resources.capture_no_reminder
import tajsos.composeapp.generated.resources.capture_placeholder_area
import tajsos.composeapp.generated.resources.capture_placeholder_dump_thought
import tajsos.composeapp.generated.resources.capture_placeholder_next_thought
import tajsos.composeapp.generated.resources.capture_placeholder_project
import tajsos.composeapp.generated.resources.capture_process_later
import tajsos.composeapp.generated.resources.capture_quick_capture
import tajsos.composeapp.generated.resources.capture_recurring
import tajsos.composeapp.generated.resources.capture_save_continue
import tajsos.composeapp.generated.resources.capture_save_type
import tajsos.composeapp.generated.resources.capture_sticky
import tajsos.composeapp.generated.resources.capture_type
import tajsos.composeapp.generated.resources.capture_use_template
import tajsos.composeapp.generated.resources.capture_voice
import tajsos.composeapp.generated.resources.screen_area
import tajsos.composeapp.generated.resources.screen_project
import tajsos.composeapp.generated.resources.type_area
import tajsos.composeapp.generated.resources.type_note
import tajsos.composeapp.generated.resources.type_project
import tajsos.composeapp.generated.resources.type_record
import tajsos.composeapp.generated.resources.type_task
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

/**
 * Renders a modal bottom sheet that collects capture text and related metadata, then submits it via the provided callback.
 *
 * The sheet supports selecting a capture type, optional project/area assignment, templates, recurring/reminder settings,
 * sticky flag, multi-capture and brain-dump modes, and an optional voice-capture action.
 *
 * @param onDismiss Called to close the sheet.
 * @param onCapture Called when the user submits a capture. Arguments (in order):
 *  1. `text` — the entered capture text.
 *  2. `type` — the selected capture type (for example "inbox", "task", "note", "record", "project", "area").
 *  3. `projectId` — selected project id or `null`.
 *  4. `areaId` — selected area id or `null`.
 *  5. `isRecurring` — `true` if the capture is marked recurring.
 *  6. `recurringInterval` — recurrence interval string (e.g. "DAILY", "WEEKLY", "MONTHLY") or `null`.
 *  7. `reminderTime` — scheduled reminder time as epoch milliseconds or `null`.
 *  8. `contextScreen` — optional calling-screen identifier passed through.
 *  9. `isSticky` — `true` if the capture is marked sticky.
 *  10. Reserved legacy metadata slot, currently passed as `null`.
 * @param projects List of available project nodes for assignment (defaults to empty).
 * @param areas List of available area nodes for assignment (defaults to empty).
 * @param templates List of templates that can prefill the capture text (defaults to empty).
 * @param defaultProjectId Optional project id to preselect when the sheet opens.
 * @param defaultAreaId Optional area id to preselect when the sheet opens.
 * @param initialText Initial input text to populate the field (e.g., from voice capture).
 * @param onVoiceCaptureClick Optional callback invoked when the voice capture button is clicked.
 * @param contextScreen Optional identifier describing the originating screen; forwarded to `onCapture`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureSheet(
    onDismiss: () -> Unit,
    onCapture: (String, String, Long?, Long?, Boolean, String?, Long?, String?, Boolean, String?) -> Unit,
    projects: List<NodeEntity> = emptyList(),
    areas: List<NodeEntity> = emptyList(),
    templates: List<TemplateEntity> = emptyList(),
    defaultProjectId: Long? = null,
    defaultAreaId: Long? = null,
    initialText: String = "",
    onVoiceCaptureClick: (() -> Unit)? = null,
    contextScreen: String? = null,
) {
    var text by remember { mutableStateOf(initialText) }

    // Update text if initialText changes (e.g. from voice capture)
    LaunchedEffect(initialText) {
        if (initialText.isNotEmpty()) {
            text = initialText
        }
    }
    var selectedType by remember { mutableStateOf("inbox") } // NON-NLS
    var selectedProjectId by remember { mutableStateOf<Long?>(defaultProjectId) }
    var selectedAreaId by remember { mutableStateOf<Long?>(defaultAreaId) }

    var isRecurring by remember { mutableStateOf(false) }
    var recurringInterval by remember { mutableStateOf<String?>(null) }
    var reminderTime by remember { mutableStateOf<Long?>(null) }
    var isSticky by remember { mutableStateOf(false) }

    var multiCaptureMode by remember { mutableStateOf(false) }
    var brainDumpMode by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = TajsOSTheme.CardSurface,
        shape =
            RoundedCornerShape(
                topStart = TajsOSTheme.RadiusLg,
                topEnd = TajsOSTheme.RadiusLg,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(TajsOSTheme.SpacingMd)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (brainDumpMode) {
                        stringResource(Res.string.capture_brain_dump_active)
                    } else {
                        stringResource(
                            Res.string.capture_quick_capture,
                        )
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (brainDumpMode) TajsOSTheme.Primary else TajsOSTheme.Muted,
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(Res.string.capture_multi),
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Muted,
                    )
                    Switch(
                        checked = multiCaptureMode || brainDumpMode,
                        onCheckedChange = { multiCaptureMode = it },
                        enabled = !brainDumpMode,
                        modifier = Modifier.scale(0.7f),
                    )
                    Spacer(Modifier.width(TajsOSTheme.SpacingSm))
                    FilterChip(
                        selected = brainDumpMode,
                        onClick = {
                            brainDumpMode = !brainDumpMode
                            if (brainDumpMode) multiCaptureMode = true
                        },
                        label = { Text(stringResource(Res.string.capture_dump)) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(vertical = TajsOSTheme.SpacingMd),
                    textStyle =
                        MaterialTheme.typography.displayMedium.copy(
                            color = TajsOSTheme.Text,
                        ),
                    cursorBrush = SolidColor(TajsOSTheme.Primary),
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            val placeholder =
                                when (selectedType) {
                                    "project" -> {
                                        stringResource(Res.string.capture_placeholder_project)
                                    }

                                    "area" -> {
                                        stringResource(Res.string.capture_placeholder_area)
                                    }

                                    else -> {
                                        if (brainDumpMode) {
                                            stringResource(Res.string.capture_placeholder_next_thought)
                                        } else {
                                            stringResource(
                                                Res.string.capture_placeholder_dump_thought,
                                            )
                                        }
                                    }
                                }
                            Text(
                                placeholder,
                                style = MaterialTheme.typography.displayMedium,
                                color = TajsOSTheme.Muted,
                            )
                        }
                        innerTextField()
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions =
                        KeyboardActions(
                            onDone = {
                                if (text.isNotBlank()) {
                                    onCapture(
                                        text,
                                        selectedType,
                                        selectedProjectId,
                                        selectedAreaId,
                                        isRecurring,
                                        recurringInterval,
                                        reminderTime,
                                        contextScreen,
                                        isSticky,
                                        null,
                                    )
                                    if (multiCaptureMode || brainDumpMode) {
                                        text = ""
                                    } else {
                                        onDismiss()
                                    }
                                }
                            },
                        ),
                )

                if (onVoiceCaptureClick != null) {
                    IconButton(onClick = onVoiceCaptureClick, modifier = Modifier.size(48.dp).pointerHoverIcon(PointerIcon.Hand)) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = stringResource(Res.string.capture_voice),
                            tint = TajsOSTheme.Primary,
                        )
                    }
                }
            }

            if (!brainDumpMode) {
                Text(
                    stringResource(Res.string.capture_type),
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Primary,
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                ) {
                    items(
                        listOf(
                            "inbox", // NON-NLS
                            "task", // NON-NLS
                            "note", // NON-NLS
                            "record", // NON-NLS
                            "project", // NON-NLS
                            "area", // NON-NLS
                        ),
                        key = { it },
                    ) { type ->
                        val typeLabel =
                            when (type) {
                                "inbox" -> stringResource(Res.string.capture_label_capture)

                                // NON-NLS
                                "task" -> stringResource(Res.string.type_task)

                                // NON-NLS
                                "note" -> stringResource(Res.string.type_note)

                                // NON-NLS
                                "record" -> stringResource(Res.string.type_record)

                                // NON-NLS
                                "project" -> stringResource(Res.string.type_project)

                                // NON-NLS
                                "area" -> stringResource(Res.string.type_area)

                                // NON-NLS
                                else -> type
                            }
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(typeLabel.uppercase()) },
                        )
                    }
                }
            }

            val relevantTemplates =
                remember(templates, selectedType) {
                    templates.filter { it.nodeType == selectedType }
                }
            if (relevantTemplates.isNotEmpty() && !brainDumpMode) {
                Text(
                    stringResource(Res.string.capture_use_template),
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Primary,
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                ) {
                    items(relevantTemplates, key = { it.id }) { template ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                template.defaultTitle?.let { text = it }
                                // Apply other template defaults if needed
                            },
                            label = { Text(template.name) },
                        )
                    }
                }
            }

            if (selectedType != "area" && selectedType != "project") {
                if (areas.isNotEmpty() && !brainDumpMode) {
                    Text(
                        stringResource(Res.string.screen_area).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Primary,
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
                        items(areas, key = { it.id }) { area ->
                            FilterChip(
                                selected = selectedAreaId == area.id,
                                onClick = {
                                    selectedAreaId =
                                        if (selectedAreaId == area.id) null else area.id
                                },
                                label = { Text(area.title) },
                            )
                        }
                    }
                }

                if (projects.isNotEmpty() && !brainDumpMode) {
                    Text(
                        stringResource(Res.string.screen_project).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Primary,
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
                        items(projects, key = { it.id }) { project ->
                            FilterChip(
                                selected = selectedProjectId == project.id,
                                onClick = {
                                    selectedProjectId =
                                        if (selectedProjectId == project.id) null else project.id
                                },
                                label = { Text(project.title) },
                            )
                        }
                    }
                }

                if (selectedType != "inbox" && !brainDumpMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = isRecurring,
                                onCheckedChange = { isRecurring = it },
                                colors =
                                    CheckboxDefaults.colors(
                                        checkedColor = TajsOSTheme.Primary,
                                    ),
                            )
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                tint = TajsOSTheme.Muted,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                stringResource(Res.string.capture_recurring),
                                style = MaterialTheme.typography.labelSmall,
                                color = TajsOSTheme.Muted,
                            )
                        }

                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = {
                                    reminderTime =
                                        if (reminderTime == null) {
                                            val now =
                                                kotlin.time.Clock.System
                                                    .now()
                                            val tz =
                                                kotlinx.datetime.TimeZone.currentSystemDefault()
                                            val localDateTime = now.toLocalDateTime(tz)
                                            val evening =
                                                kotlinx.datetime.LocalDateTime(
                                                    localDateTime.year,
                                                    localDateTime.month,
                                                    localDateTime.day,
                                                    20,
                                                    0,
                                                )
                                            if (localDateTime.hour >= 20) {
                                                evening
                                                    .toInstant(tz)
                                                    .plus(
                                                        1,
                                                        kotlinx.datetime.DateTimeUnit.DAY,
                                                        tz,
                                                    ).toEpochMilliseconds()
                                            } else {
                                                evening.toInstant(tz).toEpochMilliseconds()
                                            }
                                        } else {
                                            null
                                        }
                                },
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint =
                                        if (reminderTime !=
                                            null
                                        ) {
                                            TajsOSTheme.Primary
                                        } else {
                                            TajsOSTheme.Muted
                                        },
                                )
                            }
                            Text(
                                if (reminderTime == null) {
                                    stringResource(Res.string.capture_no_reminder)
                                } else {
                                    stringResource(
                                        Res.string.capture_process_later,
                                    )
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (reminderTime != null) TajsOSTheme.Primary else TajsOSTheme.Muted,
                            )
                        }
                    }

                    if (isRecurring) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
                            items(listOf("DAILY", "WEEKLY", "MONTHLY"), key = { it }) { interval ->
                                // NON-NLS
                                val intervalLabel =
                                    when (interval) {
                                        "DAILY" -> stringResource(Res.string.capture_interval_daily)

                                        // NON-NLS
                                        "WEEKLY" -> stringResource(Res.string.capture_interval_weekly)

                                        // NON-NLS
                                        "MONTHLY" -> stringResource(Res.string.capture_interval_monthly)

                                        // NON-NLS
                                        else -> interval
                                    }
                                FilterChip(
                                    selected = recurringInterval == interval,
                                    onClick = {
                                        recurringInterval =
                                            if (recurringInterval == interval) null else interval
                                    },
                                    label = { Text(intervalLabel) },
                                )
                            }
                        }
                    }

                    // Sticky Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = isSticky,
                                onCheckedChange = { isSticky = it },
                                colors = CheckboxDefaults.colors(checkedColor = TajsOSTheme.Primary),
                            )
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = null,
                                tint = TajsOSTheme.Muted,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                stringResource(Res.string.capture_sticky),
                                style = MaterialTheme.typography.labelSmall,
                                color = TajsOSTheme.Muted,
                            )
                        }

                        Spacer(Modifier.weight(1f))
                    }
                }
            } else if (selectedType == "project" && areas.isNotEmpty()) {
                Text(
                    stringResource(Res.string.capture_assign_to_area),
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Primary,
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
                    items(areas, key = { it.id }) { area ->
                        FilterChip(
                            selected = selectedAreaId == area.id,
                            onClick = {
                                selectedAreaId = if (selectedAreaId == area.id) null else area.id
                            },
                            label = { Text(area.title) },
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onCapture(
                            text,
                            selectedType,
                            selectedProjectId,
                            selectedAreaId,
                            isRecurring,
                            recurringInterval,
                            reminderTime,
                            contextScreen,
                            isSticky,
                            null,
                        )
                        if (multiCaptureMode || brainDumpMode) {
                            text = ""
                        } else {
                            onDismiss()
                        }
                    }
                },
                enabled = text.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            ) {
                val saveLabel =
                    if (multiCaptureMode || brainDumpMode) {
                        stringResource(Res.string.capture_save_continue)
                    } else {
                        val typeLabel =
                            when (selectedType) {
                                "task" -> stringResource(Res.string.type_task)
                                "note" -> stringResource(Res.string.type_note)
                                "record" -> stringResource(Res.string.type_record)
                                "project" -> stringResource(Res.string.type_project)
                                "area" -> stringResource(Res.string.type_area)
                                else -> selectedType
                            }
                        stringResource(Res.string.capture_save_type, typeLabel.uppercase())
                    }
                Text(
                    saveLabel,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
