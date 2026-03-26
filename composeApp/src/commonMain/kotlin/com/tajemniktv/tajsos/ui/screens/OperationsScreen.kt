/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.CapacitySnapshot
import com.tajemniktv.tajsos.ui.CombinedDirectionSnapshot
import com.tajemniktv.tajsos.ui.CoreLifeOSShiftItem
import com.tajemniktv.tajsos.ui.CoreLifeOSShiftSnapshot
import com.tajemniktv.tajsos.ui.DirectionCommitmentStatus
import com.tajemniktv.tajsos.ui.DistinctionQuestionState
import com.tajemniktv.tajsos.ui.LifeOSSecondBrainSnapshot
import com.tajemniktv.tajsos.ui.LifeOSSignatureSnapshot
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.MaintenanceStatusItem
import com.tajemniktv.tajsos.ui.OpenLoopStatusItem
import com.tajemniktv.tajsos.ui.PersonalRulesSnapshot
import com.tajemniktv.tajsos.ui.PhysicalLogisticsSnapshot
import com.tajemniktv.tajsos.ui.PlaybookSnapshot
import com.tajemniktv.tajsos.ui.ProtocolHistoryItem
import com.tajemniktv.tajsos.ui.RelationshipSnapshot
import com.tajemniktv.tajsos.ui.RelationshipStatusItem
import com.tajemniktv.tajsos.ui.TimeArchitectureSnapshot
import com.tajemniktv.tajsos.ui.TransitionProtocolItem
import com.tajemniktv.tajsos.ui.TransitionProtocolsSnapshot
import com.tajemniktv.tajsos.ui.VaultsSnapshot
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.components.nodes.NodeCard
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

private enum class OperationsTab(
    val label: String,
) {
    OpenLoops("OPEN LOOPS"),
    Maintenance("MAINTENANCE"),
    Protocols("PROTOCOLS"),
    Places("PLACES"),
    Rules("RULES"),
    Vaults("VAULTS"),
    Capacity("CAPACITY"),
    Signature("SIGNATURE"),
    Distinction("DISTINCTION"),
    Direction("DIRECTION"),
    CoreShift("CORE SHIFT"),
    Time("TIME"),
    People("PEOPLE"),
}

private enum class OpenLoopView(
    val label: String,
) {
    Inbox("INBOX"),
    Review("REVIEW"),
    All("ALL"),
    Resolved("RESOLVED"),
}

private enum class MaintenanceView(
    val label: String,
) {
    Queue("QUEUE"),
    Recurring("RECURRING"),
    Overdue("OVERDUE"),
}

private val openLoopTypes =
    listOf(
        "reply_needed",
        "waiting_for",
        "pending_decision",
        "must_check_later",
        "follow_up",
        "unresolved_problem",
    )

private val maintenanceTypes =
    listOf(
        "med_refill",
        "prescription",
        "appointment",
        "bill",
        "subscription",
        "renewal",
        "form",
        "cleaning",
        "backup",
    )

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun OperationsScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val dashboardState by viewModel.dashboardUIState.collectAsState()
    val openLoopsSnapshot by viewModel.openLoopsSnapshot.collectAsState()
    val maintenanceSnapshot by viewModel.maintenanceSnapshot.collectAsState()
    val transitionProtocolsSnapshot by viewModel.transitionProtocolsSnapshot.collectAsState()
    val physicalLogisticsSnapshot by viewModel.physicalLogisticsSnapshot.collectAsState()
    val personalRulesSnapshot by viewModel.personalRulesSnapshot.collectAsState()
    val vaultsSnapshot by viewModel.vaultsSnapshot.collectAsState()
    val capacitySnapshot by viewModel.capacitySnapshot.collectAsState()
    val lifeOSSignatureSnapshot by viewModel.lifeOSSignatureSnapshot.collectAsState()
    val lifeOSSecondBrainSnapshot by viewModel.lifeOSSecondBrainSnapshot.collectAsState()
    val combinedDirectionSnapshot by viewModel.combinedDirectionSnapshot.collectAsState()
    val coreLifeOSShiftSnapshot by viewModel.coreLifeOSShiftSnapshot.collectAsState()
    val timeArchitectureSnapshot by viewModel.timeArchitectureSnapshot.collectAsState()
    val relationshipSnapshot by viewModel.relationshipSnapshot.collectAsState()
    val playbookSnapshot by viewModel.playbookSnapshot.collectAsState()
    val allModes by viewModel.allModes.collectAsState()
    val protocolHistoryItems by viewModel.protocolHistoryItems.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()

    var tab by remember { mutableStateOf(OperationsTab.OpenLoops) }
    var openLoopView by remember { mutableStateOf(OpenLoopView.Inbox) }
    var maintenanceView by remember { mutableStateOf(MaintenanceView.Queue) }

    val nodesForTab =
        remember(tab, dashboardState) {
            when (tab)
            {
                OperationsTab.Maintenance -> dashboardState.maintenanceQueue
                OperationsTab.People -> dashboardState.relationshipsToContact
                OperationsTab.Places -> emptyList()
                OperationsTab.Rules -> emptyList()
                OperationsTab.Vaults -> emptyList()
                OperationsTab.Capacity -> emptyList()
                OperationsTab.Signature -> emptyList()
                OperationsTab.Distinction -> emptyList()
                OperationsTab.Direction -> emptyList()
                OperationsTab.CoreShift -> emptyList()
                OperationsTab.Time -> emptyList()
                OperationsTab.Protocols -> emptyList()
                OperationsTab.OpenLoops -> emptyList()
            }
        }

    Column(
        modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        Text(
            text = "OPERATIONS WORKSPACE",
            style = MaterialTheme.typography.displaySmall,
            color = TactileTheme.Text,
        )
        Text(
            text = "Manage open loops, maintenance debt, protocols, and social follow-ups.",
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted,
        )

        OperationsTabRow(
            current = tab,
            onTabSelected = { tab = it },
        )

        if (tab == OperationsTab.OpenLoops) {
            OpenLoopsLayer(
                viewModel = viewModel,
                snapshot = openLoopsSnapshot,
                allAreas = allAreas,
                allNodes = allNodes,
                openLoopView = openLoopView,
                onOpenLoopView = { openLoopView = it },
                onEditNode = onEditNode,
            )
        } else if (tab == OperationsTab.Maintenance) {
            MaintenanceLayer(
                viewModel = viewModel,
                snapshot = maintenanceSnapshot,
                allAreas = allAreas,
                maintenanceView = maintenanceView,
                onMaintenanceView = { maintenanceView = it },
                onEditNode = onEditNode,
            )
        } else if (tab == OperationsTab.Protocols) {
            ProtocolsLayer(
                viewModel = viewModel,
                snapshot = transitionProtocolsSnapshot,
                playbookSnapshot = playbookSnapshot,
                allModes = allModes,
                allAreas = allAreas,
                history = protocolHistoryItems,
                onEditNode = onEditNode,
            )
        } else if (tab == OperationsTab.Time) {
            TimeArchitectureLayer(
                viewModel = viewModel,
                snapshot = timeArchitectureSnapshot,
                onEditNode = onEditNode,
            )
        } else if (tab == OperationsTab.Places) {
            PlacesLayer(
                viewModel = viewModel,
                snapshot = physicalLogisticsSnapshot,
                allAreas = allAreas,
                onEditNode = onEditNode,
            )
        } else if (tab == OperationsTab.Rules) {
            RulesLayer(
                viewModel = viewModel,
                snapshot = personalRulesSnapshot,
                playbookSnapshot = playbookSnapshot,
                onEditNode = onEditNode,
            )
        } else if (tab == OperationsTab.Vaults) {
            VaultsLayer(
                viewModel = viewModel,
                snapshot = vaultsSnapshot,
                onEditNode = onEditNode,
            )
        } else if (tab == OperationsTab.Capacity) {
            CapacityLayer(snapshot = capacitySnapshot, allAreas = allAreas)
        } else if (tab == OperationsTab.Signature) {
            SignatureLayer(
                viewModel = viewModel,
                snapshot = lifeOSSignatureSnapshot,
                onEditNode = onEditNode,
            )
        } else if (tab == OperationsTab.Distinction) {
            DistinctionLayer(snapshot = lifeOSSecondBrainSnapshot)
        } else if (tab == OperationsTab.Direction) {
            DirectionLayer(snapshot = combinedDirectionSnapshot)
        } else if (tab == OperationsTab.CoreShift) {
            CoreShiftLayer(snapshot = coreLifeOSShiftSnapshot)
        } else if (tab == OperationsTab.People) {
            PeopleLayer(
                viewModel = viewModel,
                snapshot = relationshipSnapshot,
                onEditNode = onEditNode,
            )
        } else if (nodesForTab.isEmpty()) {
            EmptyState(message = "No ${tab.label.lowercase()} currently active.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                items(nodesForTab, key = { it.node.id }) { node ->
                    NodeCard(
                        nodeWithPin = node,
                        onToggleDone = { status -> viewModel.updateNodeStatus(node.node, status) },
                        onTogglePin = { isPinned -> viewModel.togglePin(node.node, isPinned) },
                        onClick = { onEditNode(node.node.id) },
                        onLongClick = { onEditNode(node.node.id) },
                        onArchive = { viewModel.archiveNode(node.node) },
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ProtocolsLayer(
    viewModel: MainViewModel,
    snapshot: TransitionProtocolsSnapshot,
    playbookSnapshot: PlaybookSnapshot,
    allModes: List<ModeEntity>,
    allAreas: List<NodeEntity>,
    history: List<ProtocolHistoryItem>,
    onEditNode: (Long) -> Unit,
) {
    var customPlaybookTitle by remember { mutableStateOf("") }
    var customPlaybookSteps by remember { mutableStateOf("") }
    var customModeKey by remember { mutableStateOf<String?>(null) }
    var customAreaId by remember { mutableStateOf<Long?>(null) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "TRANSITION PROTOCOLS",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Templates ${snapshot.templates.size} • Active ${snapshot.protocols.size} • History ${history.size}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            snapshot.recommendedLabel?.let { recommended ->
                Text(
                    "Suggested now: ${recommended.uppercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Accent,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    playbookSnapshot.suggestedPlaybookLabel?.let { suggested ->
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TactileTheme.Surface,
            shape = RoundedCornerShape(TactileTheme.RadiusMd),
            border = BorderStroke(1.dp, TactileTheme.Border),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "Suggested playbook: ${suggested.uppercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Accent,
                    fontWeight = FontWeight.Bold,
                )
                AssistChip(
                    onClick = { viewModel.applyPlaybookTemplate(suggested) },
                    label = { Text("APPLY") },
                )
            }
        }
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
    ) {
        snapshot.templates.forEach { template ->
            AssistChip(
                onClick = { viewModel.triggerProtocol(template.label, source = "operations") },
                label = { Text(template.label.uppercase()) },
            )
        }
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
    ) {
        playbookSnapshot.templates.forEach { template ->
            AssistChip(
                onClick = { viewModel.applyPlaybookTemplate(template.label) },
                label = { Text("PLAYBOOK // ${template.label.uppercase()}") },
            )
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        item {
            GroupedOpenLoopSection(
                title = "PROTOCOL TEMPLATES",
                items =
                    snapshot.templates.map { template ->
                        "${template.label.uppercase()} • ${template.checklist.size} STEPS"
                    },
            )
        }

        items(snapshot.templates, key = { it.key }) { template ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = BorderStroke(1.dp, TactileTheme.Border),
            ) {
                Column(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        template.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Primary,
                        fontWeight = FontWeight.Bold,
                    )
                    template.checklist.forEach { step ->
                        Text(
                            "• $step",
                            style = MaterialTheme.typography.bodySmall,
                            color = TactileTheme.Muted,
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                    ) {
                        AssistChip(
                            onClick = { viewModel.applyProtocolTemplate(template.label) },
                            label = { Text("APPLY TEMPLATE") },
                        )
                        AssistChip(
                            onClick = {
                                viewModel.triggerProtocol(
                                    template.label,
                                    source = "operations",
                                )
                            },
                            label = { Text("RUN NOW") },
                        )
                    }
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = BorderStroke(1.dp, TactileTheme.Border),
            ) {
                Column(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "CUSTOM PLAYBOOK",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Primary,
                        fontWeight = FontWeight.Bold,
                    )
                    OutlinedTextField(
                        value = customPlaybookTitle,
                        onValueChange = { customPlaybookTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Playbook title") },
                    )
                    OutlinedTextField(
                        value = customPlaybookSteps,
                        onValueChange = { customPlaybookSteps = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Checklist (one step per line)") },
                        minLines = 3,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                    ) {
                        allModes.forEach { mode ->
                            FilterChip(
                                selected = customModeKey == mode.key,
                                onClick = {
                                    customModeKey =
                                        if (customModeKey == mode.key) null else mode.key
                                },
                                label = { Text(mode.key) },
                            )
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                    ) {
                        FilterChip(
                            selected = customAreaId == null,
                            onClick = { customAreaId = null },
                            label = { Text("NO AREA") },
                        )
                        allAreas.take(8).forEach { area ->
                            FilterChip(
                                selected = customAreaId == area.id,
                                onClick = {
                                    customAreaId = if (customAreaId == area.id) null else area.id
                                },
                                label = { Text(area.title) },
                            )
                        }
                    }
                    AssistChip(
                        onClick = {
                            val steps =
                                customPlaybookSteps
                                    .lines()
                                    .map { it.trim() }
                                    .filter { it.isNotBlank() }
                            viewModel.saveCustomPlaybook(
                                label = customPlaybookTitle,
                                checklistLines = steps,
                                modeKey = customModeKey,
                                areaId = customAreaId,
                            )
                            customPlaybookTitle = ""
                            customPlaybookSteps = ""
                            customModeKey = null
                            customAreaId = null
                        },
                        label = { Text("SAVE CUSTOM PLAYBOOK") },
                    )
                }
            }
        }

        if (playbookSnapshot.playbooks.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "PLAYBOOKS",
                    items =
                        playbookSnapshot.playbooks.map { item ->
                            "${item.node.node.title.uppercase()} • ${item.checklistDone}/${item.checklistTotal} • RUNS ${item.triggerCount}"
                        },
                )
            }
        }

        items(playbookSnapshot.playbooks, key = { it.node.node.id }) { playbook ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = BorderStroke(1.dp, TactileTheme.Border),
            ) {
                Column(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        playbook.node.node.title
                            .uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        color = TactileTheme.Text,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Type ${if (playbook.isCustom) "CUSTOM" else "TEMPLATE"} • Checklist ${playbook.checklistDone}/${playbook.checklistTotal} • Runs ${playbook.triggerCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted,
                    )
                    Text(
                        "Mode ${playbook.linkedModeKey ?: "NONE"} • Area ${
                            allAreas
                                .find {
                                    it.id == playbook.linkedAreaId
                                }?.title ?: "NONE"
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                    ) {
                        AssistChip(
                            onClick = {
                                viewModel.triggerProtocol(
                                    playbook.node.node.title,
                                    source = "playbook",
                                )
                            },
                            label = { Text("RUN PLAYBOOK") },
                        )
                        AssistChip(
                            onClick = { onEditNode(playbook.node.node.id) },
                            label = { Text("OPEN") },
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                    ) {
                        allModes.forEach { mode ->
                            FilterChip(
                                selected = playbook.linkedModeKey == mode.key,
                                onClick = {
                                    val nextMode =
                                        if (playbook.linkedModeKey == mode.key) null else mode.key
                                    viewModel.setPlaybookModeLink(playbook.node.node, nextMode)
                                },
                                label = { Text(mode.key) },
                            )
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                    ) {
                        FilterChip(
                            selected = playbook.linkedAreaId == null,
                            onClick = { viewModel.setPlaybookAreaLink(playbook.node.node, null) },
                            label = { Text("NO AREA") },
                        )
                        allAreas.take(8).forEach { area ->
                            FilterChip(
                                selected = playbook.linkedAreaId == area.id,
                                onClick = {
                                    viewModel.setPlaybookAreaLink(
                                        playbook.node.node,
                                        area.id,
                                    )
                                },
                                label = { Text(area.title) },
                            )
                        }
                    }
                }
            }
        }

        if (snapshot.protocols.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "ACTIVE PROTOCOLS",
                    items =
                        snapshot.protocols.map { item ->
                            "${item.node.node.title.uppercase()} • ${item.checklistDone}/${item.checklistTotal} • RUNS ${item.triggerCount}"
                        },
                )
            }
        }

        items(snapshot.protocols, key = { it.node.node.id }) { item ->
            ProtocolCard(
                item = item,
                onEditNode = onEditNode,
                onRun = { viewModel.triggerProtocol(item.node.node.title, source = "operations") },
                onToggleChecklist = { index, checked ->
                    viewModel.toggleProtocolChecklistStep(item.node.node, index, checked)
                },
                onArchive = { viewModel.archiveNode(item.node.node) },
            )
        }

        if (history.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "PROTOCOL HISTORY",
                    items =
                        history.take(12).map { item ->
                            "${item.protocolLabel.uppercase()} • ${formatProtocolTimestamp(item.executedAt)}"
                        },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun MaintenanceLayer(
    viewModel: MainViewModel,
    snapshot: com.tajemniktv.tajsos.ui.MaintenanceSnapshot,
    allAreas: List<NodeEntity>,
    maintenanceView: MaintenanceView,
    onMaintenanceView: (MaintenanceView) -> Unit,
    onEditNode: (Long) -> Unit,
) {
    val items =
        when (maintenanceView)
        {
            MaintenanceView.Queue -> snapshot.active
            MaintenanceView.Recurring -> snapshot.recurring
            MaintenanceView.Overdue -> snapshot.overdue
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "MAINTENANCE DASHBOARD",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Active ${snapshot.active.size} • Recurring ${snapshot.recurring.size} • Overdue ${snapshot.overdue.size}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            Text(
                "Admin debt meter: ${snapshot.adminDebtMeter}%",
                style = MaterialTheme.typography.bodySmall,
                color = if (snapshot.adminDebtMeter >= 70) TactileTheme.Error else TactileTheme.Text,
            )
            snapshot.overdueWarning?.let { warning ->
                Text(
                    warning,
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Error,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (snapshot.breakIfIgnored.isNotEmpty()) {
                Text(
                    "Things that break if ignored: ${
                        snapshot.breakIfIgnored.joinToString(", ") {
                            (it.node.node.maintenanceType ?: "manual").replace(
                                "_",
                                " ",
                            )
                        }
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted,
                )
            }
        }
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
    ) {
        MaintenanceView.entries.forEach { view ->
            FilterChip(
                selected = maintenanceView == view,
                onClick = { onMaintenanceView(view) },
                label = { Text(view.label) },
            )
        }
    }

    if (items.isEmpty()) {
        EmptyState(message = "No maintenance items in ${maintenanceView.label.lowercase()}.")
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        item {
            GroupedOpenLoopSection(
                title = "TRACKERS BY TYPE",
                items =
                    snapshot.byType.entries.map { entry ->
                        "${entry.key.replace("_", " ").uppercase()} • ${entry.value.size}"
                    },
            )
        }
        item {
            GroupedOpenLoopSection(
                title = "TRACKERS BY URGENCY",
                items =
                    snapshot.byUrgency.entries.map { entry ->
                        "${entry.key.uppercase()} • ${entry.value.size}"
                    },
            )
        }
        item {
            GroupedOpenLoopSection(
                title = "TRACKERS BY AREA",
                items =
                    snapshot.byArea.entries.map { entry ->
                        val name =
                            if (entry.key == null) {
                                "UNASSIGNED"
                            } else {
                                (
                                    allAreas.find { it.id == entry.key }?.title
                                        ?: "UNKNOWN"
                                )
                            }
                        "$name • ${entry.value.size}"
                    },
            )
        }
        if (snapshot.expirationReminders.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "EXPIRATION REMINDERS",
                    items =
                        snapshot.expirationReminders
                            .take(5)
                            .map { "${it.node.node.title} • ${it.dueInDays ?: 0}d" },
                )
            }
        }

        items(items, key = { it.node.node.id }) { item ->
            MaintenanceCard(
                item = item,
                areaName = allAreas.find { it.id == item.node.node.areaId }?.title,
                onEditNode = onEditNode,
                onSetType = { type -> viewModel.updateMaintenanceType(item.node.node, type) },
                onSetRecurring = { interval ->
                    viewModel.setMaintenanceRecurring(
                        item.node.node,
                        interval,
                    )
                },
                onSetOverdue = { timestamp ->
                    viewModel.setMaintenanceOverdueAt(
                        item.node.node,
                        timestamp,
                    )
                },
                onResolve = { viewModel.updateNodeStatus(item.node.node, "done") },
                onArchive = { viewModel.archiveNode(item.node.node) },
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun OpenLoopsLayer(
    viewModel: MainViewModel,
    snapshot: com.tajemniktv.tajsos.ui.OpenLoopsSnapshot,
    allAreas: List<NodeEntity>,
    allNodes: List<NodeWithPin>,
    openLoopView: OpenLoopView,
    onOpenLoopView: (OpenLoopView) -> Unit,
    onEditNode: (Long) -> Unit,
) {
    val loops =
        when (openLoopView)
        {
            OpenLoopView.Inbox -> snapshot.inbox
            OpenLoopView.Review -> snapshot.review
            OpenLoopView.All -> snapshot.active
            OpenLoopView.Resolved -> snapshot.resolved
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "OPEN LOOPS LAYER",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Active ${snapshot.active.size} • Inbox ${snapshot.inbox.size} • Review ${snapshot.review.size} • Resolved ${snapshot.resolved.size}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            Text(
                "Decay index: ${snapshot.averageDecayScore}%",
                style = MaterialTheme.typography.bodySmall,
                color = if (snapshot.averageDecayScore >= 60) TactileTheme.Error else TactileTheme.Text,
            )
            val overloadWarning = snapshot.overloadWarning
            if (overloadWarning != null) {
                Text(
                    overloadWarning,
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Error,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (snapshot.resolved.isNotEmpty()) {
                OutlinedButton(
                    onClick = { viewModel.archiveResolvedOpenLoops() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("ARCHIVE RESOLVED OPEN LOOPS")
                }
            }
        }
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
    ) {
        OpenLoopView.entries.forEach { view ->
            FilterChip(
                selected = openLoopView == view,
                onClick = { onOpenLoopView(view) },
                label = { Text(view.label) },
            )
        }
    }

    if (loops.isEmpty()) {
        EmptyState(message = "No open loops in ${openLoopView.label.lowercase()}.")
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        items(loops, key = { it.node.node.id }) { loop ->
            OpenLoopCard(
                item = loop,
                areaName = allAreas.find { it.id == loop.node.node.areaId }?.title,
                onEditNode = onEditNode,
                onSetType = { type -> viewModel.updateOpenLoopType(loop.node.node, type) },
                onConvertTask = { viewModel.convertOpenLoopToTask(loop.node.node.id) },
                onConvertDecision = { viewModel.convertOpenLoopToDecision(loop.node.node.id) },
                onConvertNote = { viewModel.convertOpenLoopToNote(loop.node.node.id) },
                onResolve = { viewModel.resolveOpenLoop(loop.node.node.id) },
                onArchive = { viewModel.archiveNode(loop.node.node) },
            )
        }

        if (openLoopView == OpenLoopView.All) {
            item {
                GroupedOpenLoopSection(
                    title = "BY AREA",
                    items =
                        snapshot.byArea.entries.map { entry ->
                            val areaName =
                                if (entry.key == null) {
                                    "UNASSIGNED"
                                } else {
                                    (
                                        allAreas.find { it.id == entry.key }?.title
                                            ?: "UNKNOWN"
                                    )
                                }
                            "$areaName • ${entry.value.size}"
                        },
                )
            }
            item {
                GroupedOpenLoopSection(
                    title = "BY PERSON",
                    items =
                        snapshot.byPerson.entries.map { entry ->
                            val personName =
                                allNodes.find { it.node.id == entry.key }?.node?.title ?: "UNKNOWN"
                            "$personName • ${entry.value.size}"
                        },
                )
            }
            item {
                GroupedOpenLoopSection(
                    title = "BY URGENCY",
                    items =
                        snapshot.byUrgency.entries.map { entry ->
                            "${entry.key.uppercase()} • ${entry.value.size}"
                        },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun TimeArchitectureLayer(
    viewModel: MainViewModel,
    snapshot: TimeArchitectureSnapshot,
    onEditNode: (Long) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "TIME ARCHITECTURE",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Today ${snapshot.todayLayer.size} • Week ${snapshot.weekLayer.size} • Month ${snapshot.monthLayer.size} • Semester ${snapshot.semesterLayer.size}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            Text(
                "Monthly reset: ${snapshot.monthlyResetDate}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            if (snapshot.examPeriodMode) {
                Text(
                    "EXAM PERIOD MODE ACTIVE",
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Error,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
    ) {
        AssistChip(
            onClick = { viewModel.runMonthlyReset() },
            label = { Text("RUN MONTHLY RESET") },
        )
        AssistChip(
            onClick = { viewModel.addLifePeriodMarker("Life period marker") },
            label = { Text("ADD PERIOD MARKER") },
        )
        AssistChip(
            onClick = { viewModel.applyTimeHorizonFilter("today") },
            label = { Text("HORIZON TODAY") },
        )
        AssistChip(
            onClick = { viewModel.applyTimeHorizonFilter("week") },
            label = { Text("HORIZON WEEK") },
        )
        AssistChip(
            onClick = { viewModel.applyTimeHorizonFilter("month") },
            label = { Text("HORIZON MONTH") },
        )
        AssistChip(
            onClick = { viewModel.applyTimeHorizonFilter("semester") },
            label = { Text("HORIZON SEMESTER") },
        )
        AssistChip(
            onClick = { viewModel.applyTimeHorizonFilter(null) },
            label = { Text("CLEAR HORIZON") },
        )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        item {
            GroupedOpenLoopSection(
                title = "WEEKLY MAP",
                items =
                    if (snapshot.weeklyMap.isEmpty()) {
                        listOf("No due map for this week")
                    } else {
                        snapshot.weeklyMap.map { (day, count) -> "$day • $count" }
                    },
            )
        }

        if (snapshot.countdowns.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "COUNTDOWNS",
                    items = snapshot.countdowns.map { "${it.node.node.title} • ${it.daysLeft}d" },
                )
            }
        }

        if (snapshot.shortHorizonTasks.isNotEmpty()) {
            items(snapshot.shortHorizonTasks, key = { it.node.id }) { item ->
                NodeCard(
                    nodeWithPin = item,
                    onToggleDone = { status -> viewModel.updateNodeStatus(item.node, status) },
                    onTogglePin = { isPinned -> viewModel.togglePin(item.node, isPinned) },
                    onClick = { onEditNode(item.node.id) },
                    onLongClick = { onEditNode(item.node.id) },
                    onArchive = { viewModel.archiveNode(item.node) },
                )
            }
        }

        if (snapshot.longHorizonTasks.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "LONG HORIZON",
                    items =
                        snapshot.longHorizonTasks.map {
                            val dueLabel =
                                it.node.dueAt?.let(::formatProtocolTimestamp) ?: "unscheduled"
                            "${it.node.title} • due $dueLabel"
                        },
                )
            }
        }

        if (snapshot.projectPhases.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "PROJECT PHASE MODE",
                    items =
                        snapshot.projectPhases.map { item ->
                            "${item.project.title} • ${
                                item.phaseLabel.replace("_", " ").uppercase()
                            }"
                        },
                )
            }
            items(snapshot.projectPhases, key = { it.project.id }) { phase ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = TactileTheme.Surface,
                    shape = RoundedCornerShape(TactileTheme.RadiusMd),
                    border = BorderStroke(1.dp, TactileTheme.Border),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                phase.project.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = TactileTheme.Text,
                            )
                            Text(
                                phase.phaseLabel.replace("_", " ").uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (phase.isActivePhase) TactileTheme.Success else TactileTheme.Muted,
                            )
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = phase.isActivePhase,
                                onClick = { viewModel.setProjectActivePhase(phase.project, true) },
                                label = { Text("ACTIVE") },
                            )
                            FilterChip(
                                selected = !phase.isActivePhase,
                                onClick = { viewModel.setProjectActivePhase(phase.project, false) },
                                label = { Text("INACTIVE") },
                            )
                        }
                    }
                }
            }
        }

        if (snapshot.temporaryFocusPeriods.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "TEMPORARY FOCUS PERIODS",
                    items = snapshot.temporaryFocusPeriods.map { it.node.title },
                )
            }
        }
        items(snapshot.weekLayer.take(6), key = { it.node.id }) { item ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = BorderStroke(1.dp, TactileTheme.Border),
            ) {
                Column(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        item.node.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TactileTheme.Text,
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = { viewModel.setTemporaryFocusPeriod(item.node, 7) },
                            label = { Text("7D FOCUS") },
                        )
                        AssistChip(
                            onClick = { viewModel.setTemporaryFocusPeriod(item.node, 14) },
                            label = { Text("14D FOCUS") },
                        )
                        AssistChip(
                            onClick = { viewModel.clearTemporaryFocusPeriod(item.node) },
                            label = { Text("CLEAR FOCUS") },
                        )
                        AssistChip(onClick = { onEditNode(item.node.id) }, label = { Text("OPEN") })
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun PlacesLayer(
    viewModel: MainViewModel,
    snapshot: PhysicalLogisticsSnapshot,
    allAreas: List<NodeEntity>,
    onEditNode: (Long) -> Unit,
) {
    var newPlaceTitle by remember { mutableStateOf("") }
    var logisticsTitle by remember { mutableStateOf("") }
    var logisticsContent by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "PHYSICAL LOGISTICS",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Places ${snapshot.places.size} • Place tasks ${snapshot.placeBasedTasks.size} • Errands ${
                    snapshot.errandClusters.values.sumOf {
                        it.size
                    }
                }",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            Text(
                "Travel pack template: ${if (snapshot.travelPackTemplateReady) "READY" else "MISSING"}",
                style = MaterialTheme.typography.bodySmall,
                color = if (snapshot.travelPackTemplateReady) TactileTheme.Success else TactileTheme.Accent,
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = newPlaceTitle,
                onValueChange = { newPlaceTitle = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Place name") },
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                AssistChip(
                    onClick = {
                        viewModel.addPlace(newPlaceTitle, campus = true)
                        newPlaceTitle = ""
                    },
                    label = { Text("ADD CAMPUS LOCATION") },
                )
                AssistChip(
                    onClick = {
                        viewModel.addPlace(newPlaceTitle, home = true)
                        newPlaceTitle = ""
                    },
                    label = { Text("ADD HOME ZONE") },
                )
                AssistChip(
                    onClick = {
                        viewModel.addPlace(newPlaceTitle)
                        newPlaceTitle = ""
                    },
                    label = { Text("ADD PLACE") },
                )
                AssistChip(
                    onClick = { viewModel.ensureTravelPackTemplate() },
                    label = { Text("ENSURE TRAVEL PACK TEMPLATE") },
                )
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = logisticsTitle,
                onValueChange = { logisticsTitle = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("List / note title") },
            )
            OutlinedTextField(
                value = logisticsContent,
                onValueChange = { logisticsContent = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Optional logistics notes") },
                minLines = 2,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                AssistChip(
                    onClick = { viewModel.createWhatToBringList(logisticsTitle) },
                    label = { Text("WHAT TO BRING") },
                )
                AssistChip(
                    onClick = { viewModel.createPackingList(logisticsTitle) },
                    label = { Text("PACKING LIST") },
                )
                AssistChip(
                    onClick = {
                        viewModel.createLeaveHomeChecklist(
                            if (logisticsTitle.isBlank()) "Leave-home checklist" else logisticsTitle,
                        )
                    },
                    label = { Text("LEAVE HOME CHECKLIST") },
                )
                AssistChip(
                    onClick = { viewModel.createDontForgetSet(logisticsTitle) },
                    label = { Text("DON'T FORGET SET") },
                )
                AssistChip(
                    onClick = { viewModel.createEventPreparationList(logisticsTitle) },
                    label = { Text("EVENT PREP LIST") },
                )
                AssistChip(
                    onClick = { viewModel.createClassBringList(logisticsTitle) },
                    label = { Text("CLASS BRING LIST") },
                )
                AssistChip(
                    onClick = {
                        viewModel.addPhysicalLogisticsNote(logisticsTitle, logisticsContent)
                        logisticsTitle = ""
                        logisticsContent = ""
                    },
                    label = { Text("PHYSICAL LOGISTICS NOTE") },
                )
            }
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        if (snapshot.campusLocations.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "CAMPUS LOCATIONS",
                    items = snapshot.campusLocations.map { "${it.place.node.title} • ${it.relatedTasks.size} tasks" },
                )
            }
        }
        if (snapshot.homeZones.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "HOME ZONES",
                    items = snapshot.homeZones.map { "${it.place.node.title} • ${it.relatedTasks.size} tasks" },
                )
            }
        }
        if (snapshot.whatToBringLists.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "WHAT-TO-BRING LISTS",
                    items = snapshot.whatToBringLists.map { it.node.title },
                )
            }
        }
        if (snapshot.packingLists.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "PACKING LISTS",
                    items = snapshot.packingLists.map { it.node.title },
                )
            }
        }
        if (snapshot.leaveHomeChecklists.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "LEAVE-HOME CHECKLISTS",
                    items = snapshot.leaveHomeChecklists.map { it.node.title },
                )
            }
        }
        if (snapshot.dontForgetSets.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "DON'T FORGET ITEM SETS",
                    items = snapshot.dontForgetSets.map { it.node.title },
                )
            }
        }
        if (snapshot.eventPreparationLists.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "EVENT PREPARATION LISTS",
                    items = snapshot.eventPreparationLists.map { it.node.title },
                )
            }
        }
        if (snapshot.classSpecificBringLists.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "CLASS-SPECIFIC BRING LISTS",
                    items = snapshot.classSpecificBringLists.map { it.node.title },
                )
            }
        }
        if (snapshot.outOfHomeTaskClusters.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "OUT-OF-HOME CLUSTERS",
                    items = snapshot.outOfHomeTaskClusters.entries.map { "${it.key} • ${it.value.size}" },
                )
            }
        }
        if (snapshot.errandClusters.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "ERRAND CLUSTERS",
                    items = snapshot.errandClusters.entries.map { "${it.key} • ${it.value.size}" },
                )
            }
        }
        if (snapshot.locationSpecificReminders.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "LOCATION-SPECIFIC REMINDERS",
                    items = snapshot.locationSpecificReminders.map { it.node.title },
                )
            }
        }

        items(snapshot.places, key = { it.place.node.id }) { place ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = BorderStroke(1.dp, TactileTheme.Border),
            ) {
                Column(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        place.place.node.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TactileTheme.Text,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Related tasks ${place.relatedTasks.size} • Reminders ${place.remindersCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted,
                    )
                    if (place.relatedTasks.isNotEmpty()) {
                        GroupedOpenLoopSection(
                            title = "PLACE-BASED TASKS",
                            items =
                                place.relatedTasks.take(6).map { task ->
                                    val area =
                                        allAreas.find { it.id == task.node.areaId }?.title
                                            ?: "General"
                                    "${task.node.title} • $area"
                                },
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                    ) {
                        AssistChip(
                            onClick = { onEditNode(place.place.node.id) },
                            label = { Text("OPEN PLACE PAGE") },
                        )
                        AssistChip(
                            onClick = {
                                viewModel.createWhatToBringList(
                                    "What to bring for ${place.place.node.title}",
                                    place.place.node.id,
                                )
                            },
                            label = { Text("WHAT TO BRING") },
                        )
                    }
                }
            }
        }

        if (snapshot.physicalLogisticsNotes.isNotEmpty()) {
            items(snapshot.physicalLogisticsNotes, key = { it.node.id }) { note ->
                NodeCard(
                    nodeWithPin = note,
                    onToggleDone = { status -> viewModel.updateNodeStatus(note.node, status) },
                    onTogglePin = { isPinned -> viewModel.togglePin(note.node, isPinned) },
                    onClick = { onEditNode(note.node.id) },
                    onLongClick = { onEditNode(note.node.id) },
                    onArchive = { viewModel.archiveNode(note.node) },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun VaultsLayer(
    viewModel: MainViewModel,
    snapshot: VaultsSnapshot,
    onEditNode: (Long) -> Unit,
) {
    var entryTitle by remember { mutableStateOf("") }
    var entryContent by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("vault_document") }
    var entryType by remember { mutableStateOf("document") }

    val categories =
        listOf(
            "vault_document" to "DOCUMENT VAULT",
            "vault_links" to "IMPORTANT LINKS",
            "vault_medical" to "MEDICAL INFO",
            "vault_university" to "UNIVERSITY INFO",
            "vault_ids_forms" to "IDs & FORMS",
            "vault_application_status" to "APPLICATION STATUS",
            "vault_receipts_paperwork" to "RECEIPTS / PAPERWORK",
            "vault_account_reference" to "ACCOUNT / REFERENCE",
            "vault_official_deadline" to "OFFICIAL DEADLINE",
        )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "LIFE LOGISTICS & VAULTS",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Documents ${snapshot.documentVault.size} • Links ${snapshot.importantLinksVault.size} • Must-find-later ${snapshot.mustFindLater.size}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = entryTitle,
                onValueChange = { entryTitle = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Entry title") },
            )
            OutlinedTextField(
                value = entryContent,
                onValueChange = { entryContent = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Details / reference") },
                minLines = 2,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                categories.forEach { (key, label) ->
                    FilterChip(
                        selected = selectedCategory == key,
                        onClick = { selectedCategory = key },
                        label = { Text(label) },
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                listOf("document", "resource", "note", "vault").forEach { type ->
                    FilterChip(
                        selected = entryType == type,
                        onClick = { entryType = type },
                        label = { Text(type.uppercase()) },
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                AssistChip(
                    onClick = {
                        viewModel.addVaultEntry(
                            categoryTag = selectedCategory,
                            title = entryTitle,
                            content = entryContent,
                            asType = entryType,
                        )
                        entryTitle = ""
                        entryContent = ""
                    },
                    label = { Text("SAVE ENTRY") },
                )
                AssistChip(
                    onClick = {
                        viewModel.createApplicationStatusEntry(
                            title = if (entryTitle.isBlank()) "Application status" else entryTitle,
                            status = if (entryContent.isBlank()) "pending" else entryContent,
                            dueAt =
                                Clock.System
                                    .now()
                                    .toEpochMilliseconds() + (14L * 24 * 60 * 60 * 1000),
                        )
                        entryTitle = ""
                        entryContent = ""
                    },
                    label = { Text("APP STATUS +14D") },
                )
            }
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        if (snapshot.documentVault.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "DOCUMENT VAULT",
                    snapshot.documentVault.map { it.node.title },
                )
            }
        }
        if (snapshot.importantLinksVault.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "IMPORTANT LINKS VAULT",
                    snapshot.importantLinksVault.map { it.node.title },
                )
            }
        }
        if (snapshot.medicalInfoVault.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "MEDICAL INFO VAULT",
                    snapshot.medicalInfoVault.map { it.node.title },
                )
            }
        }
        if (snapshot.universityInfoVault.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "UNIVERSITY INFO VAULT",
                    snapshot.universityInfoVault.map { it.node.title },
                )
            }
        }
        if (snapshot.idsAndFormsVault.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "IDs / FORMS VAULT",
                    snapshot.idsAndFormsVault.map { it.node.title },
                )
            }
        }
        if (snapshot.applicationStatusTracking.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "APPLICATION STATUS TRACKING",
                    snapshot.applicationStatusTracking.map { it.node.title },
                )
            }
        }
        if (snapshot.receiptsPaperwork.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "RECEIPTS / PAPERWORK",
                    snapshot.receiptsPaperwork.map { it.node.title },
                )
            }
        }
        if (snapshot.accountReferenceVault.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "ACCOUNT / REFERENCE VAULT",
                    snapshot.accountReferenceVault.map { it.node.title },
                )
            }
        }
        if (snapshot.officialDeadlineReminders.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "OFFICIAL DEADLINE REMINDERS",
                    snapshot.officialDeadlineReminders.map { it.node.title },
                )
            }
        }

        items(snapshot.mustFindLater, key = { it.node.id }) { item ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = BorderStroke(1.dp, TactileTheme.Primary),
            ) {
                Column(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        item.node.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TactileTheme.Text,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Type ${item.node.type.uppercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                    ) {
                        AssistChip(
                            onClick = { viewModel.markMustFindLater(item.node, false) },
                            label = { Text("UNMARK MUST-FIND-LATER") },
                        )
                        AssistChip(
                            onClick = { onEditNode(item.node.id) },
                            label = { Text("OPEN") },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CapacityLayer(
    snapshot: CapacitySnapshot,
    allAreas: List<NodeEntity>,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "LOAD & CAPACITY",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Load ${snapshot.loadScore} • Fragmentation ${snapshot.fragmentationScore}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            listOfNotNull(
                snapshot.tooManyActiveProjectsWarning,
                snapshot.adminDebtWarning,
                snapshot.openLoopsOverloadWarning,
                snapshot.capacityMismatch,
                snapshot.unrealisticWeekSignal,
                snapshot.tooManyActiveFrontsIndicator,
                snapshot.attentionFragmentedIndicator,
                snapshot.weeklyStructuralOverloadWarning,
            ).forEach { warning ->
                Text(
                    warning,
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Error,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        if (snapshot.loadByArea.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "LOAD BY AREA",
                    items =
                        snapshot.loadByArea.entries
                            .sortedByDescending { it.value }
                            .map { (areaId, score) ->
                                val areaName =
                                    if (areaId == null) {
                                        "UNASSIGNED"
                                    } else {
                                        (
                                            allAreas.find { it.id == areaId }?.title
                                                ?: "UNKNOWN"
                                        )
                                    }
                                "$areaName • $score"
                            },
                )
            }
        }
        if (snapshot.loadByMode.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "LOAD BY MODE",
                    items =
                        snapshot.loadByMode.entries
                            .sortedByDescending { it.value }
                            .map { (mode, score) -> "$mode • $score" },
                )
            }
        }
        if (snapshot.loadTrend.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "LOAD TREND",
                    items = snapshot.loadTrend.map { "${it.label} • L${it.load} / F${it.fragmentation}" },
                )
            }
        }
        if (snapshot.capacityAwareSuggestions.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "CAPACITY-AWARE SUGGESTIONS",
                    items = snapshot.capacityAwareSuggestions,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun SignatureLayer(
    viewModel: MainViewModel,
    snapshot: LifeOSSignatureSnapshot,
    onEditNode: (Long) -> Unit,
) {
    val capabilities =
        listOf(
            "Operating Modes" to snapshot.operatingModesEnabled,
            "Life Areas Health" to snapshot.areaHealthEnabled,
            "Open Loops" to snapshot.openLoopsEnabled,
            "Decision Queue" to snapshot.decisionSystemEnabled,
            "Maintenance" to snapshot.maintenanceEnabled,
            "Context Filtering" to snapshot.contextAwareFilteringEnabled,
            "Transition Protocols" to snapshot.transitionProtocolsEnabled,
            "Recovery Modes" to snapshot.recoveryModeEnabled,
            "Relationships" to snapshot.relationshipLayerEnabled,
            "Vault / Logistics" to snapshot.logisticsVaultEnabled,
            "Load / Capacity" to snapshot.loadCapacityEnabled,
            "Principles / Playbooks" to snapshot.personalPrinciplesPlaybooksEnabled,
        )

    val enabledCount = capabilities.count { it.second }
    val missingCapabilities = capabilities.filterNot { it.second }.map { it.first }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "LIFEOS SIGNATURE",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Capabilities $enabledCount/${capabilities.size} • Work-date coverage ${snapshot.workDateDueCoveragePercent}%",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            Text(
                "Mode of life: ${snapshot.modeOfLifeLabel.replace('_', ' ').uppercase()}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Accent,
                fontWeight = FontWeight.Bold,
            )
            if (snapshot.modeOfLifeReason.isNotBlank()) {
                Text(
                    snapshot.modeOfLifeReason,
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted,
                )
            }
        }
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
    ) {
        capabilities.forEach { (label, enabled) ->
            FilterChip(
                selected = enabled,
                onClick = {},
                enabled = false,
                label = {
                    Text("${label.uppercase()} • ${if (enabled) "ON" else "OFF"}")
                },
            )
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        if (missingCapabilities.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "MISSING SIGNATURE CAPABILITIES",
                    items = missingCapabilities,
                )
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = BorderStroke(1.dp, TactileTheme.Border),
            ) {
                Column(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "WORK DATE VS DUE DATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Coverage ${snapshot.workDateDueCoveragePercent}% • Missing work date ${snapshot.workDateDueItems.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted,
                    )
                    if (snapshot.workDateDueItems.isEmpty()) {
                        Text(
                            "All active due tasks have a work date.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TactileTheme.Success,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        items(snapshot.workDateDueItems, key = { it.node.id }) { item ->
            val node = item.node
            val dueAt = node.dueAt
            val now = Clock.System.now().toEpochMilliseconds()
            val todayWorkAt = now
            val dueMinusDay = dueAt?.minus(24L * 60 * 60 * 1000)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = BorderStroke(1.dp, TactileTheme.Border),
            ) {
                Column(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        node.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TactileTheme.Text,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Due ${dueAt?.let { formatProtocolTimestamp(it) } ?: "No due date"} • Work date missing",
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                    ) {
                        AssistChip(
                            onClick = { onEditNode(node.id) },
                            label = { Text("OPEN") },
                        )
                        AssistChip(
                            onClick = { viewModel.setWorkDate(node, todayWorkAt) },
                            label = { Text("WORK TODAY") },
                        )
                        AssistChip(
                            onClick = { viewModel.setWorkDate(node, dueMinusDay ?: todayWorkAt) },
                            label = { Text("WORK DUE-1D") },
                        )
                        AssistChip(
                            onClick = { viewModel.setWorkDate(node, null) },
                            label = { Text("CLEAR") },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DistinctionLayer(snapshot: LifeOSSecondBrainSnapshot) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "LIFEOS VS SECOND BRAIN",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Second Brain coverage ${snapshot.secondBrainCoveragePercent}% • LifeOS coverage ${snapshot.lifeOSCoveragePercent}%",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            Text(
                "System posture: ${snapshot.postureLabel.replace('_', ' ').uppercase()}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Accent,
                fontWeight = FontWeight.Bold,
            )
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        if (snapshot.secondBrainQuestions.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "SECOND BRAIN-ORIENTED QUESTIONS",
                    items = snapshot.secondBrainQuestions.map { it.question },
                )
            }
            items(snapshot.secondBrainQuestions, key = { it.question }) { item ->
                DistinctionQuestionCard(item = item)
            }
        }

        if (snapshot.lifeOSQuestions.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "LIFEOS-ORIENTED QUESTIONS",
                    items = snapshot.lifeOSQuestions.map { it.question },
                )
            }
            items(snapshot.lifeOSQuestions, key = { it.question }) { item ->
                DistinctionQuestionCard(item = item)
            }
        }
    }
}

@Composable
private fun DistinctionQuestionCard(item: DistinctionQuestionState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
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
private fun DirectionLayer(snapshot: CombinedDirectionSnapshot) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "COMBINED DIRECTION",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Direction completion ${snapshot.completionPercent}% • Posture ${
                    snapshot.postureLabel.replace(
                        '_',
                        ' ',
                    ).uppercase()
                }",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        if (snapshot.practicalitySignals.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "PRACTICALITY SIGNALS",
                    items = snapshot.practicalitySignals,
                )
            }
        }
        items(snapshot.commitments, key = { it.commitment }) { item ->
            DirectionCommitmentCard(item = item)
        }
    }
}

@Composable
private fun DirectionCommitmentCard(item: DirectionCommitmentStatus) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
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
private fun CoreShiftLayer(snapshot: CoreLifeOSShiftSnapshot) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "CORE LIFEOS SHIFT",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Section 1 completion ${snapshot.completionPercent}% • Connected ${if (snapshot.connectedProperly) "YES" else "NO"}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            snapshot.integrationWarning?.let { warning ->
                Text(
                    warning,
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Error,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        items(snapshot.items, key = { it.criterion }) { item ->
            CoreShiftCriterionCard(item = item)
        }
    }
}

@Composable
private fun CoreShiftCriterionCard(item: CoreLifeOSShiftItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
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
@OptIn(ExperimentalLayoutApi::class)
private fun RulesLayer(
    viewModel: MainViewModel,
    snapshot: PersonalRulesSnapshot,
    playbookSnapshot: PlaybookSnapshot,
    onEditNode: (Long) -> Unit,
) {
    var ruleTitle by remember { mutableStateOf("") }
    var ruleContent by remember { mutableStateOf("") }
    var selectedRuleTag by remember { mutableStateOf("rule_foundational") }

    val ruleCategories =
        listOf(
            "rule_anti_goal" to "ANTI-GOAL",
            "rule_red_flag" to "RED FLAG",
            "rule_green_flag" to "GREEN FLAG",
            "rule_priority" to "PRIORITY",
            "rule_tend_to_forget" to "TEND TO FORGET",
            "rule_messes_me_up" to "MESSES ME UP",
            "rule_helps_off_balance" to "HELPS OFF-BALANCE",
            "rule_decision_principle" to "DECISION PRINCIPLE",
            "rule_constraint" to "CONSTRAINT",
            "rule_foundational" to "FOUNDATIONAL RULE",
            "rule_recovery_reminder" to "RECOVERY REMINDER",
            "rule_distrust_brain" to "DO NOT TRUST BRAIN WHEN",
            "rule_what_works" to "WHAT WORKS FOR ME",
        )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "PERSONAL RULES VAULT",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Rules ${snapshot.vault.size} • Pinned ${snapshot.pinnedPrinciples.size} • Playbook links ${snapshot.playbookLinksCount}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = ruleTitle,
                onValueChange = { ruleTitle = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Rule title") },
            )
            OutlinedTextField(
                value = ruleContent,
                onValueChange = { ruleContent = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Rule details") },
                minLines = 2,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                ruleCategories.forEach { (tag, label) ->
                    FilterChip(
                        selected = selectedRuleTag == tag,
                        onClick = { selectedRuleTag = tag },
                        label = { Text(label) },
                    )
                }
            }
            AssistChip(
                onClick = {
                    viewModel.addPersonalRule(
                        title = ruleTitle,
                        content = ruleContent,
                        categoryTag = selectedRuleTag,
                    )
                    ruleTitle = ""
                    ruleContent = ""
                },
                label = { Text("SAVE TO RULES VAULT") },
            )
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        if (snapshot.antiGoals.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "ANTI-GOALS",
                    snapshot.antiGoals.map { it.node.title },
                )
            }
        }
        if (snapshot.redFlags.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "RED FLAGS",
                    snapshot.redFlags.map { it.node.title },
                )
            }
        }
        if (snapshot.greenFlags.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "GREEN FLAGS",
                    snapshot.greenFlags.map { it.node.title },
                )
            }
        }
        if (snapshot.priorities.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "PRIORITIES",
                    snapshot.priorities.map { it.node.title },
                )
            }
        }
        if (snapshot.tendToForget.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "WHAT I TEND TO FORGET",
                    snapshot.tendToForget.map { it.node.title },
                )
            }
        }
        if (snapshot.messesMeUp.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "WHAT RELIABLY MESSES ME UP",
                    snapshot.messesMeUp.map { it.node.title },
                )
            }
        }
        if (snapshot.helpsOffBalance.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "WHAT HELPS WHEN OFF-BALANCE",
                    snapshot.helpsOffBalance.map { it.node.title },
                )
            }
        }
        if (snapshot.decisionPrinciples.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "DECISION PRINCIPLES",
                    snapshot.decisionPrinciples.map { it.node.title },
                )
            }
        }
        if (snapshot.constraints.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "PERSONAL CONSTRAINTS",
                    snapshot.constraints.map { it.node.title },
                )
            }
        }
        if (snapshot.foundationalRules.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "FOUNDATIONAL LIFE RULES",
                    snapshot.foundationalRules.map { it.node.title },
                )
            }
        }
        if (snapshot.recoveryReminders.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "RECOVERY REMINDERS",
                    snapshot.recoveryReminders.map { it.node.title },
                )
            }
        }
        if (snapshot.distrustBrainNotes.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "DO NOT TRUST YOUR BRAIN WHEN...",
                    snapshot.distrustBrainNotes.map { it.node.title },
                )
            }
        }
        if (snapshot.whatWorksNotes.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "WHAT WORKS FOR ME",
                    snapshot.whatWorksNotes.map { it.node.title },
                )
            }
        }

        items(snapshot.vault, key = { it.node.id }) { rule ->
            val isPinned = snapshot.pinnedPrinciples.any { it.node.id == rule.node.id }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border =
                    BorderStroke(
                        1.dp,
                        if (isPinned) TactileTheme.Primary else TactileTheme.Border,
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        rule.node.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TactileTheme.Text,
                        fontWeight = FontWeight.Bold,
                    )
                    if (rule.node.content.isNotBlank()) {
                        Text(
                            rule.node.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = TactileTheme.Muted,
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                    ) {
                        AssistChip(
                            onClick = { viewModel.pinOperatingPrinciple(rule.node, !isPinned) },
                            label = { Text(if (isPinned) "UNPIN FROM HOME" else "PIN TO HOME") },
                        )
                        AssistChip(
                            onClick = { onEditNode(rule.node.id) },
                            label = { Text("OPEN") },
                        )
                    }
                    if (playbookSnapshot.playbooks.isNotEmpty()) {
                        Text(
                            "LINK TO PLAYBOOK",
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Muted,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                        ) {
                            playbookSnapshot.playbooks.take(8).forEach { playbook ->
                                AssistChip(
                                    onClick = {
                                        viewModel.linkPrincipleToPlaybook(
                                            rule.node.id,
                                            playbook.node.node.id,
                                        )
                                    },
                                    label = {
                                        Text(
                                            playbook.node.node.title
                                                .uppercase(),
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun PeopleLayer(
    viewModel: MainViewModel,
    snapshot: RelationshipSnapshot,
    onEditNode: (Long) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "RELATIONSHIP LAYER",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "People ${snapshot.people.size} • Follow-up ${snapshot.followUpNeeded.size} • Reply queue ${snapshot.replyQueue.size}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            snapshot.gentlePrompt?.let { prompt ->
                Text(
                    prompt,
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Accent,
                )
            }
        }
    }

    if (snapshot.people.isEmpty()) {
        EmptyState("No person nodes yet. Add a `person` node to start relationship tracking.")
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        if (snapshot.importantRelationships.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "IMPORTANT RELATIONSHIPS",
                    items = snapshot.importantRelationships.map { it.person.node.title },
                )
            }
        }

        if (snapshot.upcomingImportantDates.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "IMPORTANT DATES",
                    items =
                        snapshot.upcomingImportantDates.map { item ->
                            "${item.person.node.title} • ${item.followUpDueInDays ?: 0}d"
                        },
                )
            }
        }

        if (snapshot.replyQueue.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "REPLY QUEUE",
                    items = snapshot.replyQueue.map { it.node.title },
                )
            }
        }

        if (snapshot.sharedPlans.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "SHARED PLANS",
                    items = snapshot.sharedPlans.map { it.node.title },
                )
            }
        }

        if (snapshot.professors.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "PROFESSOR / ACADEMIC CONTACTS",
                    items = snapshot.professors.map { it.person.node.title },
                )
            }
        }

        if (snapshot.friendsAndFamily.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "FRIEND / FAMILY FOLLOW-UPS",
                    items = snapshot.friendsAndFamily.map { it.person.node.title },
                )
            }
        }

        items(snapshot.people, key = { it.person.node.id }) { person ->
            PersonRelationshipCard(
                item = person,
                viewModel = viewModel,
                onEditNode = onEditNode,
            )
        }
    }
}

@Composable
private fun GroupedOpenLoopSection(
    title: String,
    items: List<String>,
) {
    if (items.isEmpty()) return
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            items.forEach {
                Text(it, style = MaterialTheme.typography.bodySmall, color = TactileTheme.Text)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun OpenLoopCard(
    item: OpenLoopStatusItem,
    areaName: String?,
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
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
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
                "Type ${(item.node.node.openLoopType ?: "untyped").uppercase()} • ${item.urgency.uppercase()} • Age ${item.ageDays}d • Stale ${item.stalenessDays}d • Decay ${item.decayScore}%",
                style = MaterialTheme.typography.bodySmall,
                color = urgencyColor,
            )
            Text(
                "Area ${areaName ?: "Unassigned"} • Person ${item.relatedPersonName ?: "None"}",
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
                AssistChip(onClick = { onEditNode(item.node.node.id) }, label = { Text("OPEN") })

                if (item.node.node.status == "active") {
                    AssistChip(onClick = onResolve, label = { Text("RESOLVE") })
                    AssistChip(onClick = onConvertTask, label = { Text("TO TASK") })
                    AssistChip(onClick = onConvertDecision, label = { Text("TO DECISION") })
                    AssistChip(onClick = onConvertNote, label = { Text("TO NOTE") })
                } else {
                    Button(onClick = onArchive) { Text("ARCHIVE") }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun MaintenanceCard(
    item: MaintenanceStatusItem,
    areaName: String?,
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
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
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
                AssistChip(
                    onClick = {
                        val now =
                            kotlin.time.Clock.System
                                .now()
                                .toEpochMilliseconds()
                        onSetOverdue(now + 3 * 24 * 60 * 60 * 1000L)
                    },
                    label = { Text("DUE +3D") },
                )
                AssistChip(
                    onClick = {
                        val now =
                            kotlin.time.Clock.System
                                .now()
                                .toEpochMilliseconds()
                        onSetOverdue(now + 7 * 24 * 60 * 60 * 1000L)
                    },
                    label = { Text("DUE +7D") },
                )
                AssistChip(
                    onClick = { onSetOverdue(null) },
                    label = { Text("CLEAR DUE") },
                )
            }

            HorizontalDivider(color = TactileTheme.Border)

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                AssistChip(onClick = { onEditNode(item.node.node.id) }, label = { Text("OPEN") })
                if (item.node.node.status == "active") {
                    AssistChip(onClick = onResolve, label = { Text("RESOLVE") })
                } else {
                    Button(onClick = onArchive) { Text("ARCHIVE") }
                }
            }
        }
    }
}

@Composable
private fun OperationsTabRow(
    current: OperationsTab,
    onTabSelected: (OperationsTab) -> Unit,
) {
    ScrollableTabRow(
        selectedTabIndex = current.ordinal,
        containerColor = TactileTheme.Surface,
        edgePadding = TactileTheme.SpacingSm,
    ) {
        OperationsTab.entries.forEach { tab ->
            Tab(
                selected = tab == current,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.label,
                        fontWeight = if (tab == current) FontWeight.Bold else FontWeight.Medium,
                    )
                },
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ProtocolCard(
    item: TransitionProtocolItem,
    onEditNode: (Long) -> Unit,
    onRun: () -> Unit,
    onToggleChecklist: (Int, Boolean) -> Unit,
    onArchive: () -> Unit,
) {
    val checklistItems = parseProtocolChecklist(item.node.node.content)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
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
            val lastTriggeredAt = item.lastTriggeredAt
            if (lastTriggeredAt != null) {
                Text(
                    "Last run ${formatProtocolTimestamp(lastTriggeredAt)}",
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
@OptIn(ExperimentalLayoutApi::class)
private fun PersonRelationshipCard(
    item: RelationshipStatusItem,
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
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
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
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
                "Role ${(item.relationshipType ?: "general").uppercase()} • Last contact ${item.daysSinceLastContact ?: "?"}d • Follow-up ${item.followUpDueInDays ?: "none"}",
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
                AssistChip(
                    onClick = {
                        val dueAt =
                            Clock.System.now().toEpochMilliseconds() + (30L * 24 * 60 * 60 * 1000)
                        viewModel.setPersonImportantDate(person, dueAt)
                    },
                    label = { Text("IMPORTANT DATE +30D") },
                )
                AssistChip(
                    onClick = {
                        val dueAt =
                            Clock.System.now().toEpochMilliseconds() + (365L * 24 * 60 * 60 * 1000)
                        viewModel.setPersonImportantDate(person, dueAt)
                    },
                    label = { Text("BIRTHDAY +1Y") },
                )
                AssistChip(
                    onClick = { viewModel.setPersonImportantDate(person, null) },
                    label = { Text("CLEAR DATE") },
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                AssistChip(
                    onClick = {
                        viewModel.createReplyNeededForPerson(
                            person.id,
                            "Reply to ${person.title}",
                            "Gentle follow-up",
                        )
                    },
                    label = { Text("REPLY LOOP") },
                )
                AssistChip(
                    onClick = {
                        viewModel.createSharedPlanForPerson(
                            person.id,
                            "Shared plan with ${person.title}",
                            "",
                        )
                    },
                    label = { Text("SHARED PLAN") },
                )
                AssistChip(
                    onClick = {
                        viewModel.createAskAboutNextTimeNote(
                            person.id,
                            "Ask ${person.title} about …",
                        )
                    },
                    label = { Text("ASK NEXT TIME") },
                )
                AssistChip(
                    onClick = { onEditNode(person.id) },
                    label = { Text("OPEN PERSON PAGE") },
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
                AssistChip(
                    onClick = {
                        viewModel.setPersonRelationshipContext(
                            person,
                            relationshipContext,
                        )
                    },
                    label = { Text("SAVE CONTEXT NOTES") },
                )
            }

            if (relatedItems.isNotEmpty()) {
                GroupedOpenLoopSection(
                    title = "RELATED TASKS / DECISIONS / NOTES",
                    items = relatedItems.take(6).map { it.node.title },
                )
            }
        }
    }
}

private fun parseProtocolChecklist(content: String): List<Pair<Boolean, String>> =
    content.lines().mapNotNull { line ->
        val trimmed = line.trimStart()
        when
            {
                trimmed.startsWith("- [x] ") -> true to trimmed.removePrefix("- [x] ").trim()
                trimmed.startsWith("- [ ] ") -> false to trimmed.removePrefix("- [ ] ").trim()
                else -> null
            }
    }

private fun formatProtocolTimestamp(timestamp: Long): String {
    val local =
        Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
    val hh = local.hour.toString().padStart(2, '0')
    val mm = local.minute.toString().padStart(2, '0')
    return "${local.date} $hh:$mm"
}
