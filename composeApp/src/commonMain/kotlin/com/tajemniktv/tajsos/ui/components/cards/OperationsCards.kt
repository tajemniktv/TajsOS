/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.MaintenanceStatusItem
import com.tajemniktv.tajsos.ui.OpenLoopStatusItem
import com.tajemniktv.tajsos.ui.main.state.CoreLifeOSShiftItem
import com.tajemniktv.tajsos.ui.main.state.DirectionCommitmentStatus
import com.tajemniktv.tajsos.ui.main.state.DistinctionQuestionState
import com.tajemniktv.tajsos.ui.main.state.RelationshipStatusItem
import com.tajemniktv.tajsos.ui.main.state.TransitionProtocolItem
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.open_loop_action_archive
import tajsos.composeapp.generated.resources.open_loop_action_convert_decision
import tajsos.composeapp.generated.resources.open_loop_action_convert_note
import tajsos.composeapp.generated.resources.open_loop_action_convert_task
import tajsos.composeapp.generated.resources.open_loop_action_open
import tajsos.composeapp.generated.resources.open_loop_action_resolve
import tajsos.composeapp.generated.resources.open_loop_meta_line
import tajsos.composeapp.generated.resources.open_loop_meta_line_area
import tajsos.composeapp.generated.resources.open_loop_none
import tajsos.composeapp.generated.resources.open_loop_unassigned
import tajsos.composeapp.generated.resources.open_loop_untyped
import kotlin.time.Clock

/**
 * Displays a card for an open loop (unresolved thought, idea, or pending task).
 * Provides actionable interactions like resolving, archiving, or converting the loop into a concrete task or decision.
 *
 * @param item The state data representing the open loop, including its urgency and status.
 * @param areaName Optional name of the associated area for context rendering.
 * @param openLoopTypes A list of valid categorization types for open loops.
 * @param onEditNode Callback triggered when the user opts to open the full node editor.
 * @param onSetType Callback triggered when a new open loop category is selected.
 * @param onConvertTask Callback to convert the loop into an actionable task.
 * @param onConvertDecision Callback to convert the loop into a recorded decision.
 * @param onConvertNote Callback to convert the loop into a standard note.
 * @param onResolve Callback to mark the loop as completed or resolved.
 * @param onArchive Callback to hide the loop from active views without deleting it.
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
fun OpenLoopCard(
    item: OpenLoopStatusItem,
    areaName: String?,
    openLoopTypes: List<String>,
    onEditNode: (Long) -> Unit,
    onSetType: (String) -> Unit,
    onConvertTask: () -> Unit,
    onConvertDecision: () -> Unit,
    onConvertNote: () -> Unit,
    onResolve: () -> Unit,
    onArchive: () -> Unit,
) {
    val urgencyColor =
        when (item.urgency) {
            "critical" -> TajsOSTheme.Error
            "high" -> TajsOSTheme.Accent
            "medium" -> TajsOSTheme.Primary
            else -> TajsOSTheme.Success
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.CardSurface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TajsOSTheme.RadiusMd),
        border = BorderStroke(1.dp, urgencyColor.copy(alpha = 0.25f)),
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                item.node.node.title,
                style = MaterialTheme.typography.titleMedium,
                color = TajsOSTheme.Text,
                fontWeight = FontWeight.Bold,
            )
            if (item.node.node.content
                    .isNotBlank()
            ) {
                Text(
                    item.node.node.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                )
            }
            Text(
                stringResource(
                    Res.string.open_loop_meta_line,
                    (
                        item.node.node.openLoopType
                            ?: stringResource(Res.string.open_loop_untyped)
                    ).uppercase(),
                    item.urgency.uppercase(),
                    item.ageDays,
                    item.stalenessDays,
                    item.decayScore,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = urgencyColor,
            )
            Text(
                stringResource(
                    Res.string.open_loop_meta_line_area,
                    areaName ?: stringResource(Res.string.open_loop_unassigned),
                    item.relatedPersonName ?: stringResource(Res.string.open_loop_none),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                openLoopTypes.forEach { type ->
                    FilterChip(
                        selected = item.node.node.openLoopType == type,
                        onClick = { onSetType(type) },
                        label = { Text(type.replace("_", " ").uppercase()) },
                    )
                }
            }
            HorizontalDivider(color = TajsOSTheme.GhostBorder)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                AssistChip(
                    onClick = { onEditNode(item.node.node.id) },
                    label = { Text(stringResource(Res.string.open_loop_action_open)) },
                )
                if (item.node.node.status == "active") {
                    AssistChip(
                        onClick = onResolve,
                        label = { Text(stringResource(Res.string.open_loop_action_resolve)) },
                    )
                    AssistChip(
                        onClick = onConvertTask,
                        label = { Text(stringResource(Res.string.open_loop_action_convert_task)) },
                    )
                    AssistChip(
                        onClick = onConvertDecision,
                        label = { Text(stringResource(Res.string.open_loop_action_convert_decision)) },
                    )
                    AssistChip(
                        onClick = onConvertNote,
                        label = { Text(stringResource(Res.string.open_loop_action_convert_note)) },
                    )
                } else {
                    Button(onClick = onArchive) { Text(stringResource(Res.string.open_loop_action_archive)) }
                }
            }
        }
    }
}

/**
 * Renders a card for routine or scheduled maintenance tasks (e.g., bills, chores, subscriptions).
 * Highlights urgency and allows adjusting the schedule or marking the item resolved.
 *
 * @param item The state tracking the maintenance task's deadline and completion.
 * @param areaName Optional name of the associated area for contextual display.
 * @param maintenanceTypes A list of valid maintenance categories (e.g., 'subscription', 'chore').
 * @param onEditNode Callback to open the full editor for the maintenance node.
 * @param onSetType Callback to change the maintenance category.
 * @param onSetRecurring Callback to update or set the recurring schedule string (e.g., '1w', '1m').
 * @param onSetOverdue Callback to manually override the overdue timestamp, or null to clear.
 * @param onResolve Callback to record a completion event and schedule the next occurrence.
 * @param onArchive Callback to retire the maintenance item.
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
fun MaintenanceCard(
    item: MaintenanceStatusItem,
    areaName: String?,
    maintenanceTypes: List<String>,
    onEditNode: (Long) -> Unit,
    onSetType: (String) -> Unit,
    onSetRecurring: (String?) -> Unit,
    onSetOverdue: (Long?) -> Unit,
    onResolve: () -> Unit,
    onArchive: () -> Unit,
) {
    val urgencyColor =
        when (item.urgency) {
            "critical" -> TajsOSTheme.Error
            "high" -> TajsOSTheme.Accent
            "medium" -> TajsOSTheme.Primary
            else -> TajsOSTheme.Success
        }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.CardSurface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TajsOSTheme.RadiusMd),
        border = BorderStroke(1.dp, urgencyColor.copy(alpha = 0.25f)),
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                item.node.node.title,
                style = MaterialTheme.typography.titleMedium,
                color = TajsOSTheme.Text,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "${
                    (item.node.node.maintenanceType ?: "manual").replace("_", " ").uppercase()
                } • ${item.urgency.uppercase()} • Recurring ${if (item.isRecurring) "YES" else "NO"}",
                style = MaterialTheme.typography.bodySmall,
                color = urgencyColor,
            )
            Text(
                "Area ${areaName ?: "Unassigned"} • Overdue ${item.overdueDays}d${item.dueInDays?.let { " • Due in ${it}d" } ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                maintenanceTypes.forEach { type ->
                    FilterChip(
                        selected = item.node.node.maintenanceType == type,
                        onClick = { onSetType(type) },
                        label = { Text(type.replace("_", " ").uppercase()) },
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                FilterChip(
                    selected = item.node.node.recurringInterval == "DAILY",
                    onClick = { onSetRecurring("DAILY") },
                    label = { Text("DAILY") },
                )
                FilterChip(
                    selected = item.node.node.recurringInterval == "WEEKLY",
                    onClick = { onSetRecurring("WEEKLY") },
                    label = { Text("WEEKLY") },
                )
                FilterChip(
                    selected = item.node.node.recurringInterval == "MONTHLY",
                    onClick = { onSetRecurring("MONTHLY") },
                    label = { Text("MONTHLY") },
                )
                FilterChip(
                    selected = !item.isRecurring,
                    onClick = { onSetRecurring(null) },
                    label = { Text("NO RECURRING") },
                )
                AssistChip(onClick = {
                    onSetOverdue(
                        Clock.System.now().toEpochMilliseconds() + 3 * 24 * 60 * 60 * 1000L,
                    )
                }, label = { Text("DUE +3D") })
                AssistChip(onClick = {
                    onSetOverdue(
                        Clock.System.now().toEpochMilliseconds() + 7 * 24 * 60 * 60 * 1000L,
                    )
                }, label = { Text("DUE +7D") })
                AssistChip(onClick = { onSetOverdue(null) }, label = { Text("CLEAR DUE") })
            }
            HorizontalDivider(color = TajsOSTheme.GhostBorder)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                AssistChip(onClick = { onEditNode(item.node.node.id) }, label = { Text("OPEN") })
                if (item.node.node.status == "active") {
                    AssistChip(
                        onClick = onResolve,
                        label = { Text("RESOLVE") },
                    )
                } else {
                    Button(onClick = onArchive) { Text("ARCHIVE") }
                }
            }
        }
    }
}

/**
 * Displays a summary card for a repeatable protocol or checklist (e.g., Morning Startup).
 * Shows completion progress and provides inline toggle actions for checklist items.
 *
 * @param item The state tracking the protocol's history, run count, and checklist items.
 * @param checklistItems A list of pairs representing the current checklist state (isCompleted, label).
 * @param onEditNode Callback to modify the protocol definition.
 * @param onRun Callback to trigger a new run of the protocol, creating a history entry.
 * @param onToggleChecklist Callback fired when a checklist item's completion state changes. Receives the index and new boolean state.
 * @param onArchive Callback to retire the protocol.
 * @param formatTimestamp A formatting function to render timestamps cleanly.
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
fun ProtocolCard(
    item: TransitionProtocolItem,
    checklistItems: List<Pair<Boolean, String>>,
    onEditNode: (Long) -> Unit,
    onRun: () -> Unit,
    onToggleChecklist: (Int, Boolean) -> Unit,
    onArchive: () -> Unit,
    formatTimestamp: (Long) -> String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.CardSurface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                item.node.node.title
                    .uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = TajsOSTheme.Text,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Checklist ${item.checklistDone}/${item.checklistTotal} • Runs ${item.triggerCount}",
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
            item.lastTriggeredAt?.let {
                Text(
                    "Last run ${formatTimestamp(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                )
            }
            if (checklistItems.isNotEmpty()) {
                checklistItems.forEachIndexed { index, checklistItem ->
                    FilterChip(
                        selected = checklistItem.first,
                        onClick = { onToggleChecklist(index, !checklistItem.first) },
                        label = { Text(checklistItem.second) },
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                AssistChip(onClick = { onEditNode(item.node.node.id) }, label = { Text("OPEN") })
                AssistChip(onClick = onRun, label = { Text("RUN") })
                Button(onClick = onArchive) { Text("ARCHIVE") }
            }
        }
    }
}

/**
 * Renders a question card used to clarify distinctions or boundaries within the LifeOS framework.
 * Visually distinguishes between answered and unanswered states to prompt reflection.
 *
 * @param item The state containing the distinction question and the user's recorded answer.
 */
@Composable
fun DistinctionQuestionCard(item: DistinctionQuestionState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.CardSurface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TajsOSTheme.RadiusMd),
        border =
            BorderStroke(
                1.dp,
                if (item.answered) TajsOSTheme.Border else TajsOSTheme.Error.copy(alpha = 0.5f),
            ),
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                item.question.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                item.answer,
                style = MaterialTheme.typography.bodySmall,
                color = if (item.answered) TajsOSTheme.Text else TajsOSTheme.Error,
            )
        }
    }
}

/**
 * Displays a commitment or guiding principle, reflecting whether recent actions align with it.
 * Uses a traffic-light border pattern to indicate if the commitment is currently satisfied or needs work.
 *
 * @param item The state tracking the commitment's text, satisfaction status, and supporting evidence.
 */
@Composable
fun DirectionCommitmentCard(item: DirectionCommitmentStatus) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.CardSurface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TajsOSTheme.RadiusMd),
        border =
            BorderStroke(
                1.dp,
                if (item.satisfied) {
                    TajsOSTheme.Success.copy(alpha = 0.5f)
                } else {
                    TajsOSTheme.Error.copy(
                        alpha = 0.5f,
                    )
                },
            ),
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                item.commitment.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                if (item.satisfied) "Satisfied" else "Needs work",
                style = MaterialTheme.typography.bodySmall,
                color = if (item.satisfied) TajsOSTheme.Success else TajsOSTheme.Error,
                fontWeight = FontWeight.Bold,
            )
            Text(
                item.evidence,
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
        }
    }
}

/**
 * Renders an evaluation card for a core LifeOS behavior shift (e.g., transitioning from passive logging to active planning).
 * Similar to [DirectionCommitmentCard], it highlights areas requiring attention.
 *
 * @param item The evaluation state containing the criterion, its satisfaction status, and evidence.
 */
@Composable
fun CoreShiftCriterionCard(item: CoreLifeOSShiftItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.CardSurface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TajsOSTheme.RadiusMd),
        border =
            BorderStroke(
                1.dp,
                if (item.satisfied) {
                    TajsOSTheme.Success.copy(alpha = 0.5f)
                } else {
                    TajsOSTheme.Error.copy(
                        alpha = 0.5f,
                    )
                },
            ),
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                item.criterion.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                if (item.satisfied) "Satisfied" else "Needs attention",
                style = MaterialTheme.typography.bodySmall,
                color = if (item.satisfied) TajsOSTheme.Success else TajsOSTheme.Error,
                fontWeight = FontWeight.Bold,
            )
            Text(
                item.evidence,
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
        }
    }
}

/**
 * A comprehensive card managing interactions, context, and follow-ups for a specific person.
 * Exposes actions for setting contact dates, assigning relationship tiers, creating linked nodes, and storing context notes.
 *
 * @param item The CRM state for the person, tracking days since contact and follow-up schedules.
 * @param viewModel The [MainViewModel] used to dispatch relationship mutation actions (like setting importance or contact dates).
 * @param onEditNode Callback to open the full person node editor.
 * @param groupedSection A `@Composable` lambda to consistently render lists of related nodes (e.g., linked tasks or notes).
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
fun PersonRelationshipCard(
    item: RelationshipStatusItem,
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
    groupedSection: @Composable (String, List<String>) -> Unit,
) {
    val person = item.person.node
    val relatedItems by viewModel
        .getRelatedItemsForPerson(person.id)
        .collectAsState(initial = emptyList())
    var socialNotes by remember(
        person.id,
        person.socialEnergyNotes,
    ) { mutableStateOf(person.socialEnergyNotes.orEmpty()) }
    var relationshipContext by remember(person.id, person.relationshipContext) {
        mutableStateOf(
            person.relationshipContext.orEmpty(),
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.CardSurface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TajsOSTheme.RadiusMd),
        border =
            BorderStroke(
                1.dp,
                if (item.isImportant) TajsOSTheme.Primary else TajsOSTheme.Border,
            ),
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                person.title,
                style = MaterialTheme.typography.titleMedium,
                color = TajsOSTheme.Text,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Context ${(item.relationshipType ?: "general").uppercase()} • Last contact ${item.daysSinceLastContact ?: "?"}d • Follow-up ${item.followUpDueInDays ?: "none"}",
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
            Text(
                "Linked ${item.linkedItemsCount} • Replies ${item.pendingReplyCount} • Shared plans ${item.sharedPlansCount} • Ask-next-time ${item.askAboutNextTimeCount}",
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                FilterChip(
                    selected = item.isImportant,
                    onClick = { viewModel.markImportantRelationship(person, !item.isImportant) },
                    label = { Text(if (item.isImportant) "IMPORTANT" else "MARK IMPORTANT") },
                )
                FilterChip(
                    selected = item.relationshipType == "professor",
                    onClick = { viewModel.setPersonRelationshipType(person, "professor") },
                    label = { Text("PROFESSOR") },
                )
                FilterChip(
                    selected = item.relationshipType == "friend",
                    onClick = { viewModel.setPersonRelationshipType(person, "friend") },
                    label = { Text("FRIEND") },
                )
                FilterChip(
                    selected = item.relationshipType == "family",
                    onClick = { viewModel.setPersonRelationshipType(person, "family") },
                    label = { Text("FAMILY") },
                )
                FilterChip(
                    selected = item.relationshipType == null,
                    onClick = { viewModel.setPersonRelationshipType(person, null) },
                    label = { Text("GENERAL") },
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                AssistChip(
                    onClick = { viewModel.setPersonLastContactNow(person) },
                    label = { Text("CONTACT NOW") },
                )
                AssistChip(
                    onClick = { viewModel.setPersonFollowUpInDays(person, 7) },
                    label = { Text("FOLLOW-UP +7D") },
                )
                AssistChip(
                    onClick = { viewModel.setPersonFollowUpInDays(person, 14) },
                    label = { Text("FOLLOW-UP +14D") },
                )
                AssistChip(onClick = {
                    viewModel.setPersonImportantDate(
                        person,
                        Clock.System.now().toEpochMilliseconds() + (30L * 24 * 60 * 60 * 1000),
                    )
                }, label = { Text("IMPORTANT DATE +30D") })
                AssistChip(onClick = {
                    viewModel.setPersonImportantDate(
                        person,
                        Clock.System.now().toEpochMilliseconds() + (365L * 24 * 60 * 60 * 1000),
                    )
                }, label = { Text("BIRTHDAY +1Y") })
                AssistChip(
                    onClick = { viewModel.setPersonImportantDate(person, null) },
                    label = { Text("CLEAR DATE") },
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                AssistChip(onClick = {
                    viewModel.createReplyNeededForPerson(
                        person.id,
                        "Reply to ${person.title}",
                        "Gentle follow-up",
                    )
                }, label = { Text("REPLY LOOP") })
                AssistChip(onClick = {
                    viewModel.createSharedPlanForPerson(
                        person.id,
                        "Shared plan with ${person.title}",
                    )
                }, label = { Text("SHARED PLAN") })
                AssistChip(onClick = {
                    viewModel.createAskAboutNextTimeNote(
                        person.id,
                        "Ask ${person.title} about …",
                    )
                }, label = { Text("ASK NEXT TIME") })
                AssistChip(
                    onClick = { onEditNode(person.id) },
                    label = { Text("OPEN RELATIONSHIP") },
                )
            }
            OutlinedTextField(
                value = socialNotes,
                onValueChange = { socialNotes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Social energy notes") },
                minLines = 2,
            )
            OutlinedTextField(
                value = relationshipContext,
                onValueChange = { relationshipContext = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Relationship context notes") },
                minLines = 2,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { viewModel.setPersonSocialEnergyNotes(person, socialNotes) },
                    label = { Text("SAVE ENERGY NOTES") },
                )
                AssistChip(onClick = {
                    viewModel.setPersonRelationshipContext(
                        person,
                        relationshipContext,
                    )
                }, label = { Text("SAVE CONTEXT NOTES") })
            }
            if (relatedItems.isNotEmpty()) {
                groupedSection(
                    "RELATED TASKS / DECISIONS / NOTES",
                    relatedItems.take(6).map { it.node.title },
                )
            }
        }
    }
}
