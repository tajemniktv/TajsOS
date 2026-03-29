/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.ui.components.nodes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.data.DecisionOptionEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.cards.OptionCard
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*
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
)
{
    val scope = rememberCoroutineScope()
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

    Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
        SectionTitle(stringResource(Res.string.decision_status_label))
        Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
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
        Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
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

            SectionTitle("DECISION REVISIT DATE")
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                val now = Clock.System.now()
                val tz = TimeZone.currentSystemDefault()
                val revisitDateLabel =
                    node.decisionRevisitAt?.let {
                        kotlin.time.Instant
                            .fromEpochMilliseconds(it)
                            .toLocalDateTime(tz)
                            .date
                            .toString()
                    } ?: "NONE"
                AssistChip(
                    onClick = {},
                    label = { Text("Current: $revisitDateLabel") },
                )
                FilterChip(
                    selected = false,
                    onClick = {
                        viewModel.setDecisionRevisit(
                            node,
                            now.plus(1, DateTimeUnit.WEEK, tz).toEpochMilliseconds(),
                        )
                    },
                    label = { Text("1 WEEK") },
                )
                FilterChip(
                    selected = false,
                    onClick = {
                        viewModel.setDecisionRevisit(
                            node,
                            now.plus(1, DateTimeUnit.MONTH, tz).toEpochMilliseconds(),
                        )
                    },
                    label = { Text("1 MONTH") },
                )
                FilterChip(
                    selected = node.decisionRevisitAt == null,
                    onClick = { viewModel.setDecisionRevisit(node, null) },
                    label = { Text("CLEAR") },
                )
            }

            SectionTitle("RELATED PEOPLE")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (relatedPeople.isEmpty()) "No related people linked." else "${relatedPeople.size} linked",
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted,
                )
                TextButton(onClick = { showPeopleDialog = true }) {
                    Text("LINK PERSON")
                }
            }
            if (relatedPeople.isNotEmpty()) {
                relatedPeople.forEach { person ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = TactileTheme.Background,
                        border = BorderStroke(1.dp, TactileTheme.Border),
                        shape = RoundedCornerShape(2.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(TactileTheme.SpacingSm),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                person.node.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TactileTheme.Text,
                            )
                            TextButton(onClick = {
                                viewModel.unlinkDecisionFromPerson(
                                node.id,
                                person.node.id,
                            )
                        }) {
                            Text("UNLINK")
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
            color = TactileTheme.Border,
            modifier = Modifier.padding(vertical = TactileTheme.SpacingSm),
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
                    tint = TactileTheme.Primary,
                )
            }
        }

        if (options.isEmpty())
        {
            Text(
                stringResource(Res.string.decision_no_options),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted,
                modifier = Modifier.padding(vertical = TactileTheme.SpacingSm),
            )
        } else
        {
            options.forEach { option ->
                OptionCard(
                    option = option,
                    onUpdate = { viewModel.updateDecisionOption(it) },
                    onDelete = { viewModel.deleteDecisionOption(it) },
                )
            }
        }

        HorizontalDivider(
            color = TactileTheme.Border,
            modifier = Modifier.padding(vertical = TactileTheme.SpacingSm),
        )

        if (node.decisionStatus == "decided")
        {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = stringResource(Res.string.cd_decision_decided),
                    tint = TactileTheme.Success,
                )
                Spacer(modifier = Modifier.width(TactileTheme.SpacingSm))
                Column {
                    Text(
                        stringResource(Res.string.decision_outcome_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Success,
                    )
                    Text(
                        node.decisionOutcome ?: stringResource(Res.string.decision_no_outcome),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TactileTheme.Text,
                    )
                }
            }

            Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))

            Button(
                onClick = { viewModel.convertDecisionToProject(node.id) },
                colors = ButtonDefaults.buttonColors(containerColor = TactileTheme.Accent),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(2.dp),
            ) {
                Text(stringResource(Res.string.decision_convert_project))
            }

            OutlinedButton(
                onClick = { viewModel.convertDecisionToTask(node.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(2.dp),
                border = BorderStroke(1.dp, TactileTheme.Border),
            ) {
                Text(stringResource(Res.string.decision_convert_task), color = TactileTheme.Text)
            }
        } else
        {
            Button(
                onClick = { showDecideDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = TactileTheme.Primary),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(2.dp),
            ) {
                Text(stringResource(Res.string.decision_finalize))
            }
        }
    }

    if (showAddOptionDialog)
    {
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
            containerColor = TactileTheme.Background,
            titleContentColor = TactileTheme.Text,
            textContentColor = TactileTheme.Text,
        )
    }

    if (showDecideDialog)
    {
        var outcome by remember { mutableStateOf("") }
        var selectedOptionId by remember { mutableStateOf<Long?>(null) }

        AlertDialog(
            onDismissRequest = { showDecideDialog = false },
            title = { Text(stringResource(Res.string.decision_finalize)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                    Text(
                        stringResource(Res.string.decision_selected_option),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    options.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
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
                    containerColor = TactileTheme.Background,
                    titleContentColor = TactileTheme.Text,
                    textContentColor = TactileTheme.Text,
                )
            }

        if (showPeopleDialog) {
            AlertDialog(
                onDismissRequest = { showPeopleDialog = false },
                title = { Text("LINK PERSON TO DECISION") },
                text = {
                    if (peopleNodes.isEmpty()) {
                        Text("No person nodes available.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
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
                    Text("CLOSE")
                }
            },
            dismissButton = {},
            containerColor = TactileTheme.Background,
            titleContentColor = TactileTheme.Text,
            textContentColor = TactileTheme.Text,
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
fun SectionTitle(text: String)
{
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = TactileTheme.Muted,
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
fun DecisionField(label: String, value: String, onValueChange: (String) -> Unit)
{
    Column {
        SectionTitle(label)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(vertical = TactileTheme.SpacingSm),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TactileTheme.Text),
            cursorBrush = SolidColor(TactileTheme.Primary),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty())
                    {
                        Text(
                            stringResource(Res.string.decision_tap_to_add),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TactileTheme.Muted,
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
