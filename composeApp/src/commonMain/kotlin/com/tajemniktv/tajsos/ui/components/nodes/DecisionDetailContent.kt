/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.nodes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.cards.OptionCard
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.cd_add_option
import tajsos.composeapp.generated.resources.cd_decision_decided
import tajsos.composeapp.generated.resources.common_close
import tajsos.composeapp.generated.resources.decision_1_month
import tajsos.composeapp.generated.resources.decision_1_week
import tajsos.composeapp.generated.resources.decision_action_link_person
import tajsos.composeapp.generated.resources.decision_action_unlink
import tajsos.composeapp.generated.resources.decision_add
import tajsos.composeapp.generated.resources.decision_add_option_field
import tajsos.composeapp.generated.resources.decision_add_option_title
import tajsos.composeapp.generated.resources.decision_cancel
import tajsos.composeapp.generated.resources.decision_category_label
import tajsos.composeapp.generated.resources.decision_convert_project
import tajsos.composeapp.generated.resources.decision_convert_task
import tajsos.composeapp.generated.resources.decision_current_revisit
import tajsos.composeapp.generated.resources.decision_decide
import tajsos.composeapp.generated.resources.decision_difficult_because_label
import tajsos.composeapp.generated.resources.decision_easier_if_label
import tajsos.composeapp.generated.resources.decision_finalize
import tajsos.composeapp.generated.resources.decision_info_missing_label
import tajsos.composeapp.generated.resources.decision_link_person_title
import tajsos.composeapp.generated.resources.decision_no_options
import tajsos.composeapp.generated.resources.decision_no_outcome
import tajsos.composeapp.generated.resources.decision_no_people_available
import tajsos.composeapp.generated.resources.decision_no_people_linked
import tajsos.composeapp.generated.resources.decision_options_label
import tajsos.composeapp.generated.resources.decision_outcome_label
import tajsos.composeapp.generated.resources.decision_outcome_reason
import tajsos.composeapp.generated.resources.decision_people_linked_count
import tajsos.composeapp.generated.resources.decision_related_people_label
import tajsos.composeapp.generated.resources.decision_revisit_date_label
import tajsos.composeapp.generated.resources.decision_selected_option
import tajsos.composeapp.generated.resources.decision_status_label
import tajsos.composeapp.generated.resources.decision_tap_to_add
import tajsos.composeapp.generated.resources.detail_none
import tajsos.composeapp.generated.resources.identity_clear
import kotlin.time.Clock

/**
 * Renders a detailed UI for viewing and editing a single decision node.
 *
 * Shows status and category chips, editable decision fields, a list of decision
 * options with add/delete flows, and controls to finalize the decision or
 * convert a decided decision into a project or task.
 *
 * @param viewModel The MainViewModel used to load options and perform updates (add/delete/convert/decide).
 * @param node The NodeEntity representing the decision to display and edit.
 * @param onNavigateToProject Callback invoked with a project id when navigation to a converted project is required.
 */
@Composable
fun DecisionDetailContent(
    viewModel: MainViewModel,
    node: NodeEntity,
    onNavigateToProject: (Long) -> Unit,
) {
    rememberCoroutineScope()
    val options by viewModel.getOptionsForDecision(node.id).collectAsState(initial = emptyList())
    val allPeople by viewModel.allNodes.collectAsState()
    val peopleNodes =
        remember(allPeople) { allPeople.filter { it.node.type == "person" && it.node.status == "active" } }
    val relatedPeople by viewModel
        .getRelatedPeopleForDecision(node.id)
        .collectAsState(initial = emptyList())

    var showAddOptionDialog by remember { mutableStateOf(false) }
    var showDecideDialog by remember { mutableStateOf(false) }
    var showPeopleDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)) {
        SectionTitle(stringResource(Res.string.decision_status_label))
        Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
            val statuses = listOf("pending", "parked", "expired")
            statuses.forEach { status ->
                FilterChip(
                    selected = node.decisionStatus == status,
                    onClick = {
                        viewModel.updateNode(node.copy(decisionStatus = status))
                    },
                    label = { Text(status.uppercase()) },
                )
            }
        }

        SectionTitle(stringResource(Res.string.decision_category_label))
        Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
            val categories = listOf("tiny", "major")
            categories.forEach { category ->
                FilterChip(
                    selected = node.decisionCategory == category,
                    onClick = {
                        viewModel.updateNode(node.copy(decisionCategory = category))
                    },
                    label = { Text(category.uppercase()) },
                )
            }
        }

        SectionTitle(stringResource(Res.string.decision_revisit_date_label))
        Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
            val now = Clock.System.now()
            val tz = TimeZone.currentSystemDefault()
            val revisitDateLabel =
                node.decisionRevisitAt?.let {
                    kotlin.time.Instant
                        .fromEpochMilliseconds(it)
                        .toLocalDateTime(tz)
                        .date
                        .toString()
                } ?: stringResource(Res.string.detail_none).uppercase()
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        stringResource(
                            Res.string.decision_current_revisit,
                            revisitDateLabel,
                        ),
                    )
                },
            )
            FilterChip(
                selected = false,
                onClick = {
                    viewModel.setDecisionRevisit(
                        node,
                        now.plus(1, DateTimeUnit.WEEK, tz).toEpochMilliseconds(),
                    )
                },
                label = { Text(stringResource(Res.string.decision_1_week)) },
            )
            FilterChip(
                selected = false,
                onClick = {
                    viewModel.setDecisionRevisit(
                        node,
                        now.plus(1, DateTimeUnit.MONTH, tz).toEpochMilliseconds(),
                    )
                },
                label = { Text(stringResource(Res.string.decision_1_month)) },
            )
            FilterChip(
                selected = node.decisionRevisitAt == null,
                onClick = { viewModel.setDecisionRevisit(node, null) },
                label = { Text(stringResource(Res.string.identity_clear)) },
            )
        }

        SectionTitle(stringResource(Res.string.decision_related_people_label))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (relatedPeople.isEmpty()) {
                    stringResource(Res.string.decision_no_people_linked)
                } else {
                    stringResource(
                        Res.string.decision_people_linked_count,
                        relatedPeople.size.toString(),
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
            TextButton(onClick = { showPeopleDialog = true }) {
                Text(stringResource(Res.string.decision_action_link_person))
            }
        }
        if (relatedPeople.isNotEmpty()) {
            relatedPeople.forEach { person ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = TajsOSTheme.Background,
                    shape = RoundedCornerShape(2.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(TajsOSTheme.SpacingSm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            person.node.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TajsOSTheme.Text,
                        )
                        TextButton(onClick = {
                            viewModel.unlinkDecisionFromPerson(
                                node.id,
                                person.node.id,
                            )
                        }) {
                            Text(stringResource(Res.string.decision_action_unlink))
                        }
                    }
                }
            }
        }

        DecisionField(
            label = stringResource(Res.string.decision_info_missing_label),
            value = node.decisionInfoMissing ?: "",
            onValueChange = { viewModel.updateNode(node.copy(decisionInfoMissing = it)) },
        )

        DecisionField(
            label = stringResource(Res.string.decision_difficult_because_label),
            value = node.decisionDifficultBecause ?: "",
            onValueChange = { viewModel.updateNode(node.copy(decisionDifficultBecause = it)) },
        )

        DecisionField(
            label = stringResource(Res.string.decision_easier_if_label),
            value = node.decisionEasierIf ?: "",
            onValueChange = { viewModel.updateNode(node.copy(decisionEasierIf = it)) },
        )

        HorizontalDivider(
            color = TajsOSTheme.Border,
            modifier = Modifier.padding(vertical = TajsOSTheme.SpacingSm),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionTitle(stringResource(Res.string.decision_options_label))
            IconButton(onClick = { showAddOptionDialog = true }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(Res.string.cd_add_option),
                    tint = TajsOSTheme.Primary,
                )
            }
        }

        if (options.isEmpty()) {
            Text(
                stringResource(Res.string.decision_no_options),
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Muted,
                modifier = Modifier.padding(vertical = TajsOSTheme.SpacingSm),
            )
        } else {
            options.forEach { option ->
                OptionCard(
                    option = option,
                    onUpdate = { viewModel.updateDecisionOption(it) },
                    onDelete = { viewModel.deleteDecisionOption(it) },
                )
            }
        }

        HorizontalDivider(
            color = TajsOSTheme.Border,
            modifier = Modifier.padding(vertical = TajsOSTheme.SpacingSm),
        )

        if (node.decisionStatus == "decided") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = stringResource(Res.string.cd_decision_decided),
                    tint = TajsOSTheme.Success,
                )
                Spacer(modifier = Modifier.width(TajsOSTheme.SpacingSm))
                Column {
                    Text(
                        stringResource(Res.string.decision_outcome_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Success,
                    )
                    Text(
                        node.decisionOutcome ?: stringResource(Res.string.decision_no_outcome),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TajsOSTheme.Text,
                    )
                }
            }

            Spacer(modifier = Modifier.height(TajsOSTheme.SpacingSm))

            Button(
                onClick = { viewModel.convertDecisionToProject(node.id) },
                colors = ButtonDefaults.buttonColors(containerColor = TajsOSTheme.Accent),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(2.dp),
            ) {
                Text(stringResource(Res.string.decision_convert_project))
            }

            OutlinedButton(
                onClick = { viewModel.convertDecisionToTask(node.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(2.dp),
            ) {
                Text(stringResource(Res.string.decision_convert_task), color = TajsOSTheme.Text)
            }
        } else {
            Button(
                onClick = { showDecideDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = TajsOSTheme.Primary),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(2.dp),
            ) {
                Text(stringResource(Res.string.decision_finalize))
            }
        }
    }

    if (showAddOptionDialog) {
        var optionTitle by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddOptionDialog = false },
            title = { Text(stringResource(Res.string.decision_add_option_title)) },
            text = {
                OutlinedTextField(
                    value = optionTitle,
                    onValueChange = { optionTitle = it },
                    label = { Text(stringResource(Res.string.decision_add_option_field)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    enabled = optionTitle.isNotBlank(),
                    onClick = {
                        viewModel.addDecisionOption(node.id, optionTitle)
                        showAddOptionDialog = false
                    },
                ) { Text(stringResource(Res.string.decision_add)) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddOptionDialog = false
                    },
                ) { Text(stringResource(Res.string.decision_cancel)) }
            },
            containerColor = TajsOSTheme.Background,
            titleContentColor = TajsOSTheme.Text,
            textContentColor = TajsOSTheme.Text,
        )
    }

    if (showDecideDialog) {
        var outcome by remember { mutableStateOf("") }
        var selectedOptionId by remember { mutableStateOf<Long?>(null) }

        AlertDialog(
            onDismissRequest = { showDecideDialog = false },
            title = { Text(stringResource(Res.string.decision_finalize)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)) {
                    Text(
                        stringResource(Res.string.decision_selected_option),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    options.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedOptionId = option.id },
                        ) {
                            RadioButton(
                                selected = selectedOptionId == option.id,
                                onClick = { selectedOptionId = option.id },
                            )
                            Text(option.title)
                        }
                    }
                    OutlinedTextField(
                        value = outcome,
                        onValueChange = { outcome = it },
                        label = { Text(stringResource(Res.string.decision_outcome_reason)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = outcome.isNotBlank() && (options.isEmpty() || selectedOptionId != null),
                    onClick = {
                        viewModel.decideOn(node.id, outcome, selectedOptionId)
                        showDecideDialog = false
                    },
                ) { Text(stringResource(Res.string.decision_decide)) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDecideDialog = false
                    },
                ) { Text(stringResource(Res.string.decision_cancel)) }
            },
            containerColor = TajsOSTheme.Background,
            titleContentColor = TajsOSTheme.Text,
            textContentColor = TajsOSTheme.Text,
        )
    }

    if (showPeopleDialog) {
        AlertDialog(
            onDismissRequest = { showPeopleDialog = false },
            title = { Text(stringResource(Res.string.decision_link_person_title)) },
            text = {
                if (peopleNodes.isEmpty()) {
                    Text(stringResource(Res.string.decision_no_people_available))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
                        peopleNodes.forEach { person ->
                            OutlinedButton(
                                onClick = {
                                    viewModel.linkDecisionToPerson(node.id, person.node.id)
                                    showPeopleDialog = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(person.node.title)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPeopleDialog = false }) {
                    Text(stringResource(Res.string.common_close))
                }
            },
            dismissButton = {},
            containerColor = TajsOSTheme.Background,
            titleContentColor = TajsOSTheme.Text,
            textContentColor = TajsOSTheme.Text,
        )
    }
}

/**
 * Displays a section header using the app's section-title styling.
 *
 * The text is rendered with small label typography, muted color, bold weight, and 1.sp letter spacing.
 *
 * @param text The header text to display.
 */
@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = TajsOSTheme.Muted,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
    )
}

/**
 * Renders a labeled editable text input for a decision field.
 *
 * @param label The visible label for the field.
 * @param value The current text value displayed in the field.
 * @param onValueChange Called with the new text whenever the user edits the field.
 */
@Composable
fun DecisionField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column {
        SectionTitle(label)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(vertical = TajsOSTheme.SpacingSm),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TajsOSTheme.Text),
            cursorBrush = SolidColor(TajsOSTheme.Primary),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            stringResource(Res.string.decision_tap_to_add),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TajsOSTheme.Muted,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

/**
 * Renders a card showing a decision option's title, optional description, and a delete action.
 *
 * Displays the option title prominently and, if present, its description below. Provides callbacks
 * for updating or deleting the option.
 *
 * @param option The decision option to display.
 * @param onUpdate Callback invoked to request an update to the given `option`.
 * @param onDelete Callback invoked when the user requests deletion of the given `option`.
 */

