/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.protocols

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
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
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.PlaybookSnapshot
import com.tajemniktv.tajsos.ui.ProtocolHistoryItem
import com.tajemniktv.tajsos.ui.TransitionProtocolsSnapshot
import com.tajemniktv.tajsos.ui.components.cards.ProtocolCard
import com.tajemniktv.tajsos.ui.screens.GroupedOpenLoopSection
import com.tajemniktv.tajsos.ui.screens.formatProtocolTimestamp
import com.tajemniktv.tajsos.ui.screens.parseProtocolChecklist
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun ProtocolsScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth >
                900.dp
            ) {
                ProtocolsDashboardSurface.DESKTOP
            } else {
                ProtocolsDashboardSurface.MOBILE
            }
        val plan =
            remember(surface) {
                buildProtocolsDashboardPlan(
                    surface,
                )
            }
        val context =
            remember(viewModel, onEditNode) {
                ProtocolsDashboardContext(
                    viewModel,
                    onEditNode,
                )
            }
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                ProtocolsDashboardBlockRegistry
                    .resolve(
                        block.id,
                    )?.invoke(context)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun ProtocolsLayer(
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
                "ROUTINES & PLAYBOOKS",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Routine templates ${snapshot.templates.size} • Active runs ${snapshot.protocols.size} • History ${history.size}",
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
                label = { Text("PLAYBOOK ${template.label.uppercase()}") },
            )
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        item {
            GroupedOpenLoopSection(
                title = "ROUTINE TEMPLATES",
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
                    title = "ACTIVE ROUTINES",
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
                checklistItems = parseProtocolChecklist(item.node.node.content),
                onEditNode = onEditNode,
                onRun = { viewModel.triggerProtocol(item.node.node.title, source = "operations") },
                onToggleChecklist = { index, checked ->
                    viewModel.toggleProtocolChecklistStep(item.node.node, index, checked)
                },
                onArchive = { viewModel.archiveNode(item.node.node) },
                formatTimestamp = ::formatProtocolTimestamp,
            )
        }

        if (history.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "ROUTINE HISTORY",
                    items =
                        history.take(12).map { item ->
                            "${item.protocolLabel.uppercase()} • ${formatProtocolTimestamp(item.executedAt)}"
                        },
                )
            }
        }
    }
}
