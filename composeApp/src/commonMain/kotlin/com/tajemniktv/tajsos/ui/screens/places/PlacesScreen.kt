/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.places

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.cards.NodeCard
import com.tajemniktv.tajsos.ui.main.state.PhysicalLogisticsSnapshot
import com.tajemniktv.tajsos.ui.screens.GroupedOpenLoopSection
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.places_add_campus_location
import tajsos.composeapp.generated.resources.places_add_home_zone
import tajsos.composeapp.generated.resources.places_add_place
import tajsos.composeapp.generated.resources.places_class_bring_list
import tajsos.composeapp.generated.resources.places_dont_forget_set
import tajsos.composeapp.generated.resources.places_ensure_travel_pack_template
import tajsos.composeapp.generated.resources.places_event_prep_list
import tajsos.composeapp.generated.resources.places_leave_home_checklist
import tajsos.composeapp.generated.resources.places_packing_list
import tajsos.composeapp.generated.resources.places_physical_logistics_note
import tajsos.composeapp.generated.resources.places_placeholder_list_title
import tajsos.composeapp.generated.resources.places_placeholder_logistics_notes
import tajsos.composeapp.generated.resources.places_placeholder_place_name
import tajsos.composeapp.generated.resources.places_what_to_bring

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun PlacesScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth >
                900.dp
            ) {
                PlacesDashboardSurface.DESKTOP
            } else {
                PlacesDashboardSurface.MOBILE
            }
        val plan =
            remember(surface) {
                buildPlacesDashboardPlan(
                    surface,
                )
            }
        val context =
            remember(viewModel, onEditNode) {
                PlacesDashboardContext(
                    viewModel,
                    onEditNode,
                )
            }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
        ) {
            plan.primary.forEach { block ->
                item(key = block.id) {
                    PlacesDashboardBlockRegistry
                        .resolve(block.id)
                        ?.invoke(context)
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun PlacesLayer(
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
        color = TajsOSTheme.Surface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        border = BorderStroke(1.dp, TajsOSTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "PHYSICAL LOGISTICS",
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Places ${snapshot.places.size} • Place tasks ${snapshot.placeBasedTasks.size} • Errands ${
                    snapshot.errandClusters.values.sumOf {
                        it.size
                    }
                }",
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
            Text(
                "Travel pack template: ${if (snapshot.travelPackTemplateReady) "READY" else "MISSING"}",
                style = MaterialTheme.typography.bodySmall,
                color = if (snapshot.travelPackTemplateReady) TajsOSTheme.Success else TajsOSTheme.Accent,
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.Surface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        border = BorderStroke(1.dp, TajsOSTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = newPlaceTitle,
                onValueChange = { newPlaceTitle = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.places_placeholder_place_name)) },
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                AssistChip(
                    onClick = {
                        viewModel.addPlace(newPlaceTitle, campus = true)
                        newPlaceTitle = ""
                    },
                    label = { Text(stringResource(Res.string.places_add_campus_location)) },
                )
                AssistChip(
                    onClick = {
                        viewModel.addPlace(newPlaceTitle, home = true)
                        newPlaceTitle = ""
                    },
                    label = { Text(stringResource(Res.string.places_add_home_zone)) },
                )
                AssistChip(
                    onClick = {
                        viewModel.addPlace(newPlaceTitle)
                        newPlaceTitle = ""
                    },
                    label = { Text(stringResource(Res.string.places_add_place)) },
                )
                AssistChip(
                    onClick = { viewModel.ensureTravelPackTemplate() },
                    label = { Text(stringResource(Res.string.places_ensure_travel_pack_template)) },
                )
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.Surface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        border = BorderStroke(1.dp, TajsOSTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = logisticsTitle,
                onValueChange = { logisticsTitle = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.places_placeholder_list_title)) },
            )
            OutlinedTextField(
                value = logisticsContent,
                onValueChange = { logisticsContent = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.places_placeholder_logistics_notes)) },
                minLines = 2,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                AssistChip(
                    onClick = { viewModel.createWhatToBringList(logisticsTitle) },
                    label = { Text(stringResource(Res.string.places_what_to_bring)) },
                )
                AssistChip(
                    onClick = { viewModel.createPackingList(logisticsTitle) },
                    label = { Text(stringResource(Res.string.places_packing_list)) },
                )
                AssistChip(
                    onClick = {
                        viewModel.createLeaveHomeChecklist(
                            if (logisticsTitle.isBlank()) "Leave-home checklist" else logisticsTitle,
                        )
                    },
                    label = { Text(stringResource(Res.string.places_leave_home_checklist)) },
                )
                AssistChip(
                    onClick = { viewModel.createDontForgetSet(logisticsTitle) },
                    label = { Text(stringResource(Res.string.places_dont_forget_set)) },
                )
                AssistChip(
                    onClick = { viewModel.createEventPreparationList(logisticsTitle) },
                    label = { Text(stringResource(Res.string.places_event_prep_list)) },
                )
                AssistChip(
                    onClick = { viewModel.createClassBringList(logisticsTitle) },
                    label = { Text(stringResource(Res.string.places_class_bring_list)) },
                )
                AssistChip(
                    onClick = {
                        viewModel.addPhysicalLogisticsNote(logisticsTitle, logisticsContent)
                        logisticsTitle = ""
                        logisticsContent = ""
                    },
                    label = { Text(stringResource(Res.string.places_physical_logistics_note)) },
                )
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
        if (snapshot.campusLocations.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "CAMPUS LOCATIONS",
                items = snapshot.campusLocations.map { "${it.place.node.title} • ${it.relatedTasks.size} tasks" },
            )
        }
        if (snapshot.homeZones.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "HOME ZONES",
                items = snapshot.homeZones.map { "${it.place.node.title} • ${it.relatedTasks.size} tasks" },
            )
        }
        if (snapshot.whatToBringLists.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "WHAT-TO-BRING LISTS",
                items = snapshot.whatToBringLists.map { it.node.title },
            )
        }
        if (snapshot.packingLists.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "PACKING LISTS",
                items = snapshot.packingLists.map { it.node.title },
            )
        }
        if (snapshot.leaveHomeChecklists.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "LEAVE-HOME CHECKLISTS",
                items = snapshot.leaveHomeChecklists.map { it.node.title },
            )
        }
        if (snapshot.dontForgetSets.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "DON'T FORGET ITEM SETS",
                items = snapshot.dontForgetSets.map { it.node.title },
            )
        }
        if (snapshot.eventPreparationLists.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "EVENT PREPARATION LISTS",
                items = snapshot.eventPreparationLists.map { it.node.title },
            )
        }
        if (snapshot.classSpecificBringLists.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "CLASS-SPECIFIC BRING LISTS",
                items = snapshot.classSpecificBringLists.map { it.node.title },
            )
        }
        if (snapshot.outOfHomeTaskClusters.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "OUT-OF-HOME CLUSTERS",
                items = snapshot.outOfHomeTaskClusters.entries.map { "${it.key} • ${it.value.size}" },
            )
        }
        if (snapshot.errandClusters.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "ERRAND CLUSTERS",
                items = snapshot.errandClusters.entries.map { "${it.key} • ${it.value.size}" },
            )
        }
        if (snapshot.locationSpecificReminders.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "LOCATION-SPECIFIC REMINDERS",
                items = snapshot.locationSpecificReminders.map { it.node.title },
            )
        }

        snapshot.places.forEach { place ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TajsOSTheme.Surface,
                shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                border = BorderStroke(1.dp, TajsOSTheme.Border),
            ) {
                Column(
                    modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        place.place.node.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TajsOSTheme.Text,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Related tasks ${place.relatedTasks.size} • Reminders ${place.remindersCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TajsOSTheme.Muted,
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
                        horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
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
            snapshot.physicalLogisticsNotes.forEach { note ->
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
