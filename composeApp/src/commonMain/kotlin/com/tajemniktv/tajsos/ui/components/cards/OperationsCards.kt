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
import com.tajemniktv.tajsos.ui.theme.TactileTheme
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
        when (item.urgency)
        {
            "critical" -> TactileTheme.Error
            "high" -> TactileTheme.Accent
            "medium" -> TactileTheme.Primary
            else -> TactileTheme.Success
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, urgencyColor.copy(alpha = 0.25f)),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                item.node.node.title,
                style = MaterialTheme.typography.titleMedium,
                color = TactileTheme.Text,
                fontWeight = FontWeight.Bold,
            )
            if (item.node.node.content
                    .isNotBlank()
            ) {
                Text(
                    item.node.node.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted,
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
                color = TactileTheme.Muted,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                openLoopTypes.forEach { type ->
                    FilterChip(
                        selected = item.node.node.openLoopType == type,
                        onClick = { onSetType(type) },
                        label = { Text(type.replace("_", " ").uppercase()) },
                    )
                }
            }
            HorizontalDivider(color = TactileTheme.Border)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
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
        when (item.urgency)
        {
            "critical" -> TactileTheme.Error
            "high" -> TactileTheme.Accent
            "medium" -> TactileTheme.Primary
            else -> TactileTheme.Success
        }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, urgencyColor.copy(alpha = 0.25f)),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                item.node.node.title,
                style = MaterialTheme.typography.titleMedium,
                color = TactileTheme.Text,
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
                color = TactileTheme.Muted,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
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
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
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
            HorizontalDivider(color = TactileTheme.Border)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
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
        color = TactileTheme.Surface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                item.node.node.title
                    .uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = TactileTheme.Text,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Checklist ${item.checklistDone}/${item.checklistTotal} • Runs ${item.triggerCount}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            item.lastTriggeredAt?.let {
                Text(
                    "Last run ${formatTimestamp(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted,
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
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                AssistChip(onClick = { onEditNode(item.node.node.id) }, label = { Text("OPEN") })
                AssistChip(onClick = onRun, label = { Text("RUN") })
                Button(onClick = onArchive) { Text("ARCHIVE") }
            }
        }
    }
}

@Composable
fun DistinctionQuestionCard(item: DistinctionQuestionState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TactileTheme.RadiusMd),
        border =
            BorderStroke(
                1.dp,
                if (item.answered) TactileTheme.Border else TactileTheme.Error.copy(alpha = 0.5f),
            ),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                item.question.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                item.answer,
                style = MaterialTheme.typography.bodySmall,
                color = if (item.answered) TactileTheme.Text else TactileTheme.Error,
            )
        }
    }
}

@Composable
fun DirectionCommitmentCard(item: DirectionCommitmentStatus) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TactileTheme.RadiusMd),
        border =
            BorderStroke(
                1.dp,
                if (item.satisfied) {
                    TactileTheme.Success.copy(alpha = 0.5f)
                } else {
                    TactileTheme.Error.copy(
                        alpha = 0.5f,
                    )
                },
            ),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                item.commitment.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                if (item.satisfied) "Satisfied" else "Needs work",
                style = MaterialTheme.typography.bodySmall,
                color = if (item.satisfied) TactileTheme.Success else TactileTheme.Error,
                fontWeight = FontWeight.Bold,
            )
            Text(
                item.evidence,
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
        }
    }
}

@Composable
fun CoreShiftCriterionCard(item: CoreLifeOSShiftItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TactileTheme.RadiusMd),
        border =
            BorderStroke(
                1.dp,
                if (item.satisfied) {
                    TactileTheme.Success.copy(alpha = 0.5f)
                } else {
                    TactileTheme.Error.copy(
                        alpha = 0.5f,
                    )
                },
            ),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                item.criterion.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                if (item.satisfied) "Satisfied" else "Needs attention",
                style = MaterialTheme.typography.bodySmall,
                color = if (item.satisfied) TactileTheme.Success else TactileTheme.Error,
                fontWeight = FontWeight.Bold,
            )
            Text(
                item.evidence,
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
        }
    }
}

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
        color = TactileTheme.Surface,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(TactileTheme.RadiusMd),
        border =
            BorderStroke(
                1.dp,
                if (item.isImportant) TactileTheme.Primary else TactileTheme.Border,
            ),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                person.title,
                style = MaterialTheme.typography.titleMedium,
                color = TactileTheme.Text,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Context ${(item.relationshipType ?: "general").uppercase()} • Last contact ${item.daysSinceLastContact ?: "?"}d • Follow-up ${item.followUpDueInDays ?: "none"}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            Text(
                "Linked ${item.linkedItemsCount} • Replies ${item.pendingReplyCount} • Shared plans ${item.sharedPlansCount} • Ask-next-time ${item.askAboutNextTimeCount}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
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
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
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
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
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
