/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components

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
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureSheet(
    onDismiss: () -> Unit,
    onCapture: (String, String, Long?, Long?, Boolean, String?, Long?, String?, Boolean) -> Unit,
    projects: List<NodeEntity> = emptyList(),
    areas: List<NodeEntity> = emptyList(),
    templates: List<com.tajemniktv.tajsos.data.TemplateEntity> = emptyList(),
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
    var selectedType by remember { mutableStateOf("task") }
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
        containerColor = TactileTheme.Surface,
        shape = RoundedCornerShape(
            topStart = TactileTheme.RadiusLg,
            topEnd = TactileTheme.RadiusLg
        ),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(TactileTheme.SpacingMd)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (brainDumpMode) stringResource(Res.string.capture_brain_dump_active) else stringResource(
                        Res.string.capture_quick_capture
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (brainDumpMode) TactileTheme.Primary else TactileTheme.Muted,
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(Res.string.capture_multi),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                    )
                    Switch(
                        checked = multiCaptureMode || brainDumpMode,
                        onCheckedChange = { multiCaptureMode = it },
                        enabled = !brainDumpMode,
                        modifier = Modifier.scale(0.7f),
                    )
                    Spacer(Modifier.width(TactileTheme.SpacingSm))
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
                            .padding(vertical = TactileTheme.SpacingMd),
                    textStyle = MaterialTheme.typography.displayMedium.copy(color = TactileTheme.Text),
                    cursorBrush = SolidColor(TactileTheme.Primary),
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            val placeholder =
                                when (selectedType) {
                                    "project" -> stringResource(Res.string.capture_placeholder_project)
                                    "area" -> stringResource(Res.string.capture_placeholder_area)
                                    else -> if (brainDumpMode) stringResource(Res.string.capture_placeholder_next_thought) else stringResource(
                                        Res.string.capture_placeholder_dump_thought
                                    )
                                }
                            Text(
                                placeholder,
                                style = MaterialTheme.typography.displayMedium,
                                color = TactileTheme.Muted,
                            )
                        }
                        innerTextField()
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions =
                        KeyboardActions(onDone = {
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
                                    isSticky
                                )
                                if (multiCaptureMode || brainDumpMode) {
                                    text = ""
                                } else {
                                    onDismiss()
                                }
                            }
                        }),
                )

                if (onVoiceCaptureClick != null) {
                    IconButton(onClick = onVoiceCaptureClick) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = stringResource(Res.string.capture_voice),
                            tint = TactileTheme.Primary,
                        )
                    }
                }
            }

            if (!brainDumpMode) {
                Text(
                    stringResource(Res.string.capture_type),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                ) {
                    items(
                        listOf(
                            "task",
                            "note",
                            "idea",
                            "resource",
                            "open_loop",
                            "decision",
                            "maintenance",
                            "project",
                            "area"
                        )
                    ) { type ->
                        val typeLabel = when (type) {
                            "task" -> stringResource(Res.string.type_task)
                            "note" -> stringResource(Res.string.type_note)
                            "idea" -> stringResource(Res.string.type_idea)
                            "resource" -> stringResource(Res.string.type_resource)
                            "open_loop" -> stringResource(Res.string.dash_open_loops)
                            "decision" -> stringResource(Res.string.dash_decisions)
                            "maintenance" -> stringResource(Res.string.dash_maintenance)
                            "project" -> stringResource(Res.string.type_project)
                            "area" -> stringResource(Res.string.type_area)
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

            if (templates.isNotEmpty() && !brainDumpMode) {
                Text(
                    stringResource(Res.string.capture_use_template),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                ) {
                    items(templates.filter { it.nodeType == selectedType }) { template ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                if (template.defaultTitle != null) text = template.defaultTitle!!
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
                        color = TactileTheme.Primary
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                        items(areas) { area ->
                            FilterChip(
                                selected = selectedAreaId == area.id,
                                onClick = { selectedAreaId = if (selectedAreaId == area.id) null else area.id },
                                label = { Text(area.title) },
                            )
                        }
                    }
                }

                if (projects.isNotEmpty() && !brainDumpMode) {
                    Text(
                        stringResource(Res.string.screen_project).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Primary
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                        items(projects) { project ->
                            FilterChip(
                                selected = selectedProjectId == project.id,
                                onClick = { selectedProjectId = if (selectedProjectId == project.id) null else project.id },
                                label = { Text(project.title) },
                            )
                        }
                    }
                }

                if (!brainDumpMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = isRecurring,
                                onCheckedChange = { isRecurring = it },
                                colors = CheckboxDefaults.colors(checkedColor = TactileTheme.Primary),
                            )
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                tint = TactileTheme.Muted,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                stringResource(Res.string.capture_recurring),
                                style = MaterialTheme.typography.labelSmall,
                                color = TactileTheme.Muted,
                            )
                        }

                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = {
                                reminderTime =
                                    if (reminderTime == null) {
                                        val now =
                                            kotlin.time.Clock.System
                                                .now()
                                        val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
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
                                                .plus(1, kotlinx.datetime.DateTimeUnit.DAY, tz)
                                                .toEpochMilliseconds()
                                        } else {
                                            evening.toInstant(tz).toEpochMilliseconds()
                                        }
                                    } else {
                                        null
                                    }
                            }) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = if (reminderTime != null) TactileTheme.Primary else TactileTheme.Muted,
                                )
                            }
                            Text(
                                if (reminderTime == null) stringResource(Res.string.capture_no_reminder) else stringResource(
                                    Res.string.capture_process_later
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (reminderTime != null) TactileTheme.Primary else TactileTheme.Muted,
                            )
                        }
                    }

                    if (isRecurring) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                            items(listOf("DAILY", "WEEKLY", "MONTHLY")) { interval ->
                                val intervalLabel = when (interval) {
                                    "DAILY" -> stringResource(Res.string.capture_interval_daily)
                                    "WEEKLY" -> stringResource(Res.string.capture_interval_weekly)
                                    "MONTHLY" -> stringResource(Res.string.capture_interval_monthly)
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
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = isSticky,
                                onCheckedChange = { isSticky = it },
                                colors = CheckboxDefaults.colors(checkedColor = TactileTheme.Primary),
                            )
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = null,
                                tint = TactileTheme.Muted,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "STICKY",
                                style = MaterialTheme.typography.labelSmall,
                                color = TactileTheme.Muted,
                            )
                        }
                        Spacer(Modifier.weight(1f))
                    }
                }
            } else if (selectedType == "project" && areas.isNotEmpty()) {
                Text(
                    stringResource(Res.string.capture_assign_to_area),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                    items(areas) { area ->
                        FilterChip(
                            selected = selectedAreaId == area.id,
                            onClick = { selectedAreaId = if (selectedAreaId == area.id) null else area.id },
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
                            isSticky
                        )
                        if (multiCaptureMode || brainDumpMode) {
                            text = ""
                        } else {
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
            ) {
                val saveLabel = if (multiCaptureMode || brainDumpMode) {
                    stringResource(Res.string.capture_save_continue)
                } else {
                    val typeLabel = when (selectedType) {
                        "task" -> stringResource(Res.string.type_task)
                        "note" -> stringResource(Res.string.type_note)
                        "idea" -> stringResource(Res.string.type_idea)
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
