/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.notes.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.components.ActionButton
import com.tajemniktv.tajsos.ui.components.cards.ConnectionCard
import com.tajemniktv.tajsos.ui.components.cards.InfoCard
import com.tajemniktv.tajsos.ui.components.cards.LinkedNodeItem
import com.tajemniktv.tajsos.ui.components.cards.StatusCard
import com.tajemniktv.tajsos.ui.components.common.DetailHeader
import com.tajemniktv.tajsos.ui.components.common.DetailSectionHeader
import com.tajemniktv.tajsos.ui.components.nodes.DecisionDetailContent
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.context_10_min
import tajsos.composeapp.generated.resources.context_brain_works
import tajsos.composeapp.generated.resources.context_campus
import tajsos.composeapp.generated.resources.context_commute
import tajsos.composeapp.generated.resources.context_emotionally_wrecked
import tajsos.composeapp.generated.resources.context_high_focus
import tajsos.composeapp.generated.resources.context_home
import tajsos.composeapp.generated.resources.context_internet
import tajsos.composeapp.generated.resources.context_laptop
import tajsos.composeapp.generated.resources.context_low_energy
import tajsos.composeapp.generated.resources.context_out
import tajsos.composeapp.generated.resources.context_phone
import tajsos.composeapp.generated.resources.context_privacy
import tajsos.composeapp.generated.resources.context_waiting
import tajsos.composeapp.generated.resources.detail_backlinks
import tajsos.composeapp.generated.resources.detail_relationship_inspector
import tajsos.composeapp.generated.resources.detail_start_writing
import tajsos.composeapp.generated.resources.note_detail_connect_node
import tajsos.composeapp.generated.resources.note_detail_linked_context
import tajsos.composeapp.generated.resources.note_detail_new_node
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import com.tajemniktv.tajsos.ui.components.common.mouseClickable

object NoteDetailBlocks {
    private val renderers: Map<String, NoteDetailBlockRenderer> =
        mapOf(
            "note_header" to ::renderNoteHeader,
            "note_action_button" to ::renderNoteActionButton,
            "note_relationship_inspector" to ::renderNoteRelationshipInspector,
            "note_status_card" to ::renderNoteStatusCard,
            "note_decision_content" to ::renderNoteDecisionContent,
            "note_info_grid" to ::renderNoteInfoGrid,
            "note_task_metadata" to ::renderNoteTaskMetadata,
            "note_resource_metadata" to ::renderNoteResourceMetadata,
            "note_context_graph" to ::renderNoteContextGraph,
            "note_cadence" to ::renderNoteCadence,
            "note_aware_planning" to ::renderNoteAwarePlanning,
            "note_organization" to ::renderNoteOrganization,
            "note_attachments" to ::renderNoteAttachments,
            "note_knowledge_config" to ::renderNoteKnowledgeConfig,
            "note_content_editor" to ::renderNoteContentEditor,
        )

    fun resolve(id: String): NoteDetailBlockRenderer? = renderers[id]
}

@Composable
private fun renderNoteHeader(context: NoteDetailContext) {
    DetailHeader(
        title = context.node.title,
        subtitle = "DETAIL VIEW",
    )
}

@Composable
private fun renderNoteActionButton(context: NoteDetailContext) {
    val scope = rememberCoroutineScope()
    val node = context.node
    val viewModel = context.viewModel
    ActionButton(
        text = stringResource(Res.string.note_detail_new_node),
        onClick = {
            scope.launch {
                viewModel.addNode(
                    title = "NEW NODE",
                    projectId = node.projectId,
                    areaId = node.areaId,
                )
            }
        },
        containerColor = TajsOSTheme.Primary,
        contentColor = TajsOSTheme.Background,
        icon = Icons.Default.Add,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun renderNoteRelationshipInspector(context: NoteDetailContext) {
    val noteId = context.node.id
    val relations = context.relations
    val backLinks = relations.filter { it.toNodeId == noteId }
    val forwardLinks = relations.filter { it.fromNodeId == noteId }
    val nodesMap = context.nodesMap

    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)) {
        DetailSectionHeader(
            title = stringResource(Res.string.detail_relationship_inspector),
            icon = Icons.Default.Hub,
        )

        if (relations.isEmpty() && context.suggestions.isEmpty()) {
            ConnectionCard(
                text = stringResource(Res.string.note_detail_connect_node),
                onClick = context.onShowRelationDialog,
            )
        } else {
            if (backLinks.isNotEmpty()) {
                Text(
                    stringResource(Res.string.detail_backlinks).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Muted,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                )
                backLinks.forEach { relation ->
                    nodesMap[relation.fromNodeId]?.node?.let { relatedNode ->
                        LinkedNodeItem(
                            title = relatedNode.title,
                            subtitle = stringResource(Res.string.note_detail_linked_context),
                            icon =
                                when (relatedNode.type) {
                                    "project" -> Icons.AutoMirrored.Filled.List
                                    "area" -> Icons.Default.Work
                                    else -> Icons.AutoMirrored.Filled.Article
                                },
                            onClick = { context.onNavigateToNode(relatedNode.id) },
                        )
                    }
                }
            }

            forwardLinks.forEach { relation ->
                nodesMap[relation.toNodeId]?.node?.let { relatedNode ->
                    LinkedNodeItem(
                        title = relatedNode.title,
                        subtitle = relation.relationType,
                        icon =
                            when (relatedNode.type) {
                                "task" -> Icons.Default.CheckCircle
                                "project" -> Icons.AutoMirrored.Filled.List
                                else -> Icons.AutoMirrored.Filled.Article
                            },
                        onClick = { context.onNavigateToNode(relatedNode.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun renderNoteStatusCard(context: NoteDetailContext) {
    StatusCard(
        status = context.node.status,
        color =
            when (context.node.status) {
                "active" -> TajsOSTheme.Success
                "done" -> TajsOSTheme.Primary
                "archived" -> TajsOSTheme.Muted
                "blocked" -> TajsOSTheme.Error
                else -> TajsOSTheme.Accent
            },
        onClick = context.onShowStatusDialog,
    )
}

@Composable
private fun renderNoteDecisionContent(context: NoteDetailContext) {
    DecisionDetailContent(
        viewModel = context.viewModel,
        node = context.node,
        onNavigateToProject = { id ->
            context.onNavigateToNode(id)
        },
    )
}

@Composable
private fun renderNoteInfoGrid(context: NoteDetailContext) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
    ) {
        InfoCard(
            title = "DUE AT",
            value =
                context.node.dueAt?.let {
                    kotlin.time.Instant
                        .fromEpochMilliseconds(it)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                        .toString()
                } ?: "None",
            icon = Icons.Default.CalendarToday,
            modifier = Modifier.weight(1f),
            onClick = context.onShowDueDialog,
        )
        InfoCard(
            title = "REMINDER",
            value = context.node.reminderAt?.let { "Set" } ?: "None",
            icon = Icons.Default.Notifications,
            modifier = Modifier.weight(1f),
            onClick = context.onShowReminderDialog,
        )
    }
}

@Composable
private fun renderNoteTaskMetadata(context: NoteDetailContext) {
    val node = context.node
    val viewModel = context.viewModel
    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)) {
        DetailSectionHeader(
            title = "OPERATIONAL METADATA",
            icon = Icons.Default.Settings,
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TajsOSTheme.CardSurface,
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        ) {
            Column(modifier = Modifier.padding(TajsOSTheme.SpacingMd)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(
                        modifier =
                            Modifier.weight(1f).mouseClickable(onClick = {
                                context.onShowEnergyDialog()
                            }),
                    ) {
                        Text(
                            "ENERGY",
                            style = MaterialTheme.typography.labelSmall,
                            color = TajsOSTheme.Muted,
                            fontSize = 8.sp,
                        )
                        Text(
                            when (node.energyLevel) {
                                1 -> "LOW"
                                2 -> "MED"
                                3 -> "HIGH"
                                else -> "NOT SET"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color =
                                when (node.energyLevel) {
                                    1 -> TajsOSTheme.Success
                                    2 -> TajsOSTheme.Primary
                                    3 -> TajsOSTheme.Error
                                    else -> TajsOSTheme.Text
                                },
                        )
                    }
                    Column(
                        modifier =
                            Modifier.weight(1f).mouseClickable(onClick = {
                                context.onShowFrictionDialog()
                            }),
                    ) {
                        Text(
                            "FRICTION",
                            style = MaterialTheme.typography.labelSmall,
                            color = TajsOSTheme.Muted,
                            fontSize = 8.sp,
                        )
                        Text(
                            (node.friction ?: "STANDARD")
                                .uppercase()
                                .replace("_", " "),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .mouseClickable(onClick = { context.onShowEstimateDialog() }),
                    ) {
                        Text(
                            "ESTIMATE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TajsOSTheme.Muted,
                            fontSize = 8.sp,
                        )
                        Text(
                            node.estimatedMinutes?.let { "$it min" } ?: "NOT SET",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "CRITICAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = TajsOSTheme.Muted,
                                fontSize = 8.sp,
                            )
                            Spacer(Modifier.width(8.dp))
                            Switch(
                                checked = node.isHardDeadline,
                                onCheckedChange = {
                                    viewModel.updateNode(
                                        node.copy(
                                            isHardDeadline = it,
                                        ),
                                    )
                                },
                                modifier = Modifier.scale(0.6f),
                            )
                        }
                    }
                }
            }
        }

        val nextStep = node.nextSmallestStep
        if (nextStep != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TajsOSTheme.Accent.copy(alpha = 0.1f),
                shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                border = BorderStroke(1.dp, TajsOSTheme.GhostBorder),
            ) {
                Column(modifier = Modifier.padding(TajsOSTheme.SpacingMd)) {
                    Text(
                        "NEXT SMALLEST STEP",
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Accent,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    BasicTextField(
                        value = nextStep,
                        onValueChange = {
                            viewModel.updateNode(
                                node.copy(
                                    nextSmallestStep = it,
                                ),
                            )
                        },
                        textStyle =
                            MaterialTheme.typography.bodyMedium.copy(
                                color = TajsOSTheme.Text,
                                fontWeight = FontWeight.Medium,
                            ),
                        cursorBrush = SolidColor(TajsOSTheme.Accent),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun renderNoteResourceMetadata(context: NoteDetailContext) {
    val node = context.node
    val viewModel = context.viewModel
    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)) {
        DetailSectionHeader(
            title = "RESOURCE DATA",
            icon = Icons.AutoMirrored.Filled.LibraryBooks,
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TajsOSTheme.CardSurface,
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        ) {
            Column(modifier = Modifier.padding(TajsOSTheme.SpacingMd)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .mouseClickable(onClick = { context.onShowMediaTypeDialog() }),
                    ) {
                        Text(
                            "MEDIA TYPE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TajsOSTheme.Muted,
                            fontSize = 8.sp,
                        )
                        Text(
                            (node.mediaType ?: "Link").uppercase(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .mouseClickable(onClick = { context.onShowRatingDialog() }),
                    ) {
                        Text(
                            "RATING",
                            style = MaterialTheme.typography.labelSmall,
                            color = TajsOSTheme.Muted,
                            fontSize = 8.sp,
                        )
                        val rating = node.rating
                        Text(
                            if (rating != null) "⭐".repeat(rating) else "UNRATED",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Column {
                    Text(
                        "AUTHOR / SOURCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Muted,
                        fontSize = 8.sp,
                    )
                    BasicTextField(
                        value = node.author ?: "",
                        onValueChange = { viewModel.updateNode(node.copy(author = it)) },
                        textStyle =
                            MaterialTheme.typography.bodyMedium.copy(
                                color = TajsOSTheme.Text,
                                fontWeight = FontWeight.Bold,
                            ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun renderNoteContextGraph(context: NoteDetailContext) {
    val tags = context.tags
    val viewModel = context.viewModel
    val noteId = context.node.id

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.3f),
        shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
    ) {
        Column(modifier = Modifier.padding(TajsOSTheme.SpacingLg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier =
                            Modifier
                                .size(32.dp)
                                .background(
                                    TajsOSTheme.Surface,
                                    RoundedCornerShape(TajsOSTheme.RadiusSm),
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            null,
                            tint = TajsOSTheme.Primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Context Graph",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Surface(
                    color = TajsOSTheme.CardSurface,
                    shape = CircleShape,
                ) {
                    Text(
                        "${tags.size} Tags",
                        style = MaterialTheme.typography.labelSmall,
                        modifier =
                            Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 2.dp,
                            ),
                        color = TajsOSTheme.Muted,
                    )
                }
            }

            Spacer(Modifier.height(TajsOSTheme.SpacingLg))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tags.forEach { tag ->
                    AssistChip(
                        onClick = {
                            viewModel.updateSearchQuery("#${tag.name}")
                            context.onNavigateToSearch()
                        },
                        label = { Text(tag.name) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                null,
                                modifier =
                                    Modifier.size(14.dp).mouseClickable(onClick = {
                                        viewModel.detachTagFromNode(
                                            noteId,
                                            tag.id,
                                        )
                                    }),
                            )
                        },
                        colors =
                            AssistChipDefaults.assistChipColors(
                                containerColor = TajsOSTheme.CardSurface,
                                labelColor = TajsOSTheme.Text,
                            ),
                        shape = RoundedCornerShape(TajsOSTheme.RadiusSm),
                    )
                }
                IconButton(
                    onClick = context.onShowTagDialog,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Default.Add, null, tint = TajsOSTheme.Primary)
                }
            }
        }
    }
}

@Composable
private fun renderNoteCadence(context: NoteDetailContext) {
    val node = context.node
    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TajsOSTheme.CardSurface,
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        ) {
            Row(
                modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Repeat,
                    null,
                    tint = TajsOSTheme.Muted,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(TajsOSTheme.SpacingMd))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "RECURRING SCHEDULE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Muted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        if (node.isRecurring) {
                            node.recurringInterval
                                ?: "Set"
                        } else {
                            "One-time Event"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                TextButton(
                    onClick = context.onShowRecurringDialog,
                ) {
                    Text(
                        "MODIFY",
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Muted,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun renderNoteAwarePlanning(context: NoteDetailContext) {
    val node = context.node
    val viewModel = context.viewModel
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.CardSurface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "CONTEXT-AWARE PLANNING",
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Muted,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
            )

            val locationContexts =
                listOf(
                    "at_home" to stringResource(Res.string.context_home),
                    "on_campus" to stringResource(Res.string.context_campus),
                    "out_of_home" to stringResource(Res.string.context_out),
                )
            val deviceContexts =
                listOf(
                    "laptop_required" to stringResource(Res.string.context_laptop),
                    "phone_okay" to stringResource(Res.string.context_phone),
                    "needs_internet" to stringResource(Res.string.context_internet),
                )
            val energyContexts =
                listOf(
                    "low_energy" to stringResource(Res.string.context_low_energy),
                    "high_focus" to stringResource(Res.string.context_high_focus),
                    "brain_works" to stringResource(Res.string.context_brain_works),
                    "emotionally_wrecked" to stringResource(Res.string.context_emotionally_wrecked),
                )
            val socialContexts =
                listOf(
                    "needs_privacy" to stringResource(Res.string.context_privacy),
                    "commute_friendly" to stringResource(Res.string.context_commute),
                )
            val timeWindowContexts =
                listOf(
                    "10_minute" to stringResource(Res.string.context_10_min),
                    "waiting_room" to stringResource(Res.string.context_waiting),
                )

            @Composable
            fun contextRow(
                title: String,
                selected: String?,
                options: List<Pair<String, String>>,
                apply: (String?) -> Unit,
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Primary,
                    fontSize = 9.sp,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    options.forEach { (key, label) ->
                        FilterChip(
                            selected = selected == key,
                            onClick = { apply(if (selected == key) null else key) },
                            label = { Text(label) },
                        )
                    }
                }
            }

            contextRow(
                title = "LOCATION",
                selected = node.locationContext,
                options = locationContexts,
                apply = { value -> viewModel.updateNode(node.copy(locationContext = value)) },
            )
            contextRow(
                title = "DEVICE",
                selected = node.deviceContext,
                options = deviceContexts,
                apply = { value -> viewModel.updateNode(node.copy(deviceContext = value)) },
            )
            contextRow(
                title = "ENERGY",
                selected = node.energyContext,
                options = energyContexts,
                apply = { value -> viewModel.updateNode(node.copy(energyContext = value)) },
            )
            contextRow(
                title = "SOCIAL",
                selected = node.socialContext,
                options = socialContexts,
                apply = { value -> viewModel.updateNode(node.copy(socialContext = value)) },
            )
            contextRow(
                title = "TIME WINDOW",
                selected = node.timeWindowContext,
                options = timeWindowContexts,
                apply = { value -> viewModel.updateNode(node.copy(timeWindowContext = value)) },
            )
        }
    }
}

@Composable
private fun renderNoteOrganization(context: NoteDetailContext) {
    val node = context.node
    val area = context.areas.find { it.id == node.areaId }
    val project = context.projects.find { it.id == node.projectId }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.CardSurface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Column(modifier = Modifier.padding(TajsOSTheme.SpacingMd)) {
            Text(
                "ORGANIZATION",
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Muted,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(TajsOSTheme.SpacingSm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .mouseClickable(onClick = { context.onShowAreaDialog() }),
                ) {
                    Text(
                        "AREA",
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Primary,
                        fontSize = 8.sp,
                    )
                    Text(
                        area?.title ?: "Unassigned",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .mouseClickable(onClick = { context.onShowProjectDialog() }),
                ) {
                    Text(
                        "PROJECT",
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Primary,
                        fontSize = 8.sp,
                    )
                    Text(
                        project?.title ?: "None",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun renderNoteAttachments(context: NoteDetailContext) {
    val viewModel = context.viewModel
    val noteId = context.node.id
    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
        DetailSectionHeader(title = "ATTACHMENTS", icon = Icons.Default.Attachment)
        context.attachments.forEach { attachment ->
            LinkedNodeItem(
                title = attachment.title ?: attachment.uriOrPath,
                subtitle = attachment.assetType,
                icon = Icons.Default.FilePresent,
                onClick = { /* Open attachment logic */ },
            )
        }
        ConnectionCard(
            text = "ADD ATTACHMENT",
            onClick = {
                viewModel.addAttachment(
                    noteId,
                    "URL",
                    "https://example.com",
                    "New Link",
                )
            },
        )
    }
}

@Composable
private fun renderNoteKnowledgeConfig(context: NoteDetailContext) {
    val node = context.node
    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)) {
        DetailSectionHeader(
            title = "KNOWLEDGE CONFIG",
            icon = Icons.AutoMirrored.Filled.MenuBook,
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TajsOSTheme.CardSurface,
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        ) {
            Column(modifier = Modifier.padding(TajsOSTheme.SpacingMd)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .mouseClickable(onClick = { context.onShowNoteTypeDialog() }),
                    ) {
                        Text(
                            "NOTE TYPE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TajsOSTheme.Muted,
                            fontSize = 8.sp,
                        )
                        Text(
                            (node.noteType ?: "Standard").uppercase(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .mouseClickable(onClick = { context.onShowNoteStateDialog() }),
                    ) {
                        Text(
                            "STATE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TajsOSTheme.Muted,
                            fontSize = 8.sp,
                        )
                        Text(
                            (node.noteState ?: "Raw").uppercase(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun renderNoteContentEditor(context: NoteDetailContext) {
    val node = context.node
    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
        DetailSectionHeader(title = "CONTENT", icon = Icons.Default.Description)
        BasicTextField(
            value = node.content,
            onValueChange = context.onUpdateContent,
            textStyle =
                (if (context.isAtomicMode) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge).copy(
                    color = TajsOSTheme.Text,
                ),
            cursorBrush = SolidColor(TajsOSTheme.Primary),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp)
                    .background(
                        TajsOSTheme.Surface,
                        RoundedCornerShape(TajsOSTheme.RadiusMd),
                    ).border(
                        1.dp,
                        TajsOSTheme.Border,
                        RoundedCornerShape(TajsOSTheme.RadiusMd),
                    ).padding(TajsOSTheme.SpacingMd),
            decorationBox = { innerTextField ->
                Box {
                    if (node.content.isEmpty()) {
                        Text(
                            stringResource(Res.string.detail_start_writing),
                            style = MaterialTheme.typography.bodyLarge,
                            color = TajsOSTheme.Muted,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}
