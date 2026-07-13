/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.notes.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.BatteryUnknown
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.NextPlan
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Battery1Bar
import androidx.compose.material.icons.filled.Battery4Bar
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.ZoomInMap
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.ItemKind
import com.tajemniktv.tajsos.data.isNoteItem
import com.tajemniktv.tajsos.data.isTaskItem
import com.tajemniktv.tajsos.data.itemKindOrNull
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.common.SelectorDialog
import com.tajemniktv.tajsos.ui.components.screen.ScreenHeaderModel
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold
import com.tajemniktv.tajsos.ui.components.screen.ScreenScrollBehavior
import com.tajemniktv.tajsos.ui.components.screen.screenBreadcrumbs
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.detail_not_found
import tajsos.composeapp.generated.resources.type_area
import tajsos.composeapp.generated.resources.type_note
import tajsos.composeapp.generated.resources.type_project
import tajsos.composeapp.generated.resources.type_record
import tajsos.composeapp.generated.resources.type_task
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.PointerIcon

/**
 * Detailed view for a single node (Note, Idea, Task, etc.)
 *
 * @param viewModel Source of note state.
 * @param noteId ID of the note to display.
 * @param onBack Callback to go back.
 * @param onNavigateToNode Callback to navigate to another node.
 * @param onNavigateToSearch Callback to navigate to search.
 * @param isDesktop Whether the current environment is a desktop layout.
 * @param onNavigate Navigation callback.
 * @param headerScreen Override screen for breadcrumbs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    viewModel: MainViewModel,
    noteId: Long,
    onBack: () -> Unit,
    onNavigateToNode: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    isDesktop: Boolean = false,
    onNavigate: (String) -> Unit,
    headerScreen: Screen = Screen.NoteDetail,
) {
    val scope = rememberCoroutineScope()
    val nodes by viewModel.allNodes.collectAsState()
    val nodeWithPin = remember(nodes, noteId) { nodes.find { it.node.id == noteId } }

    if (nodeWithPin == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (nodes.isEmpty()) {
                CircularProgressIndicator(color = TajsOSTheme.Primary)
            } else {
                Text(
                    stringResource(Res.string.detail_not_found, noteId),
                    color = TajsOSTheme.Muted,
                )
            }
        }
        return
    }

    val node = nodeWithPin.node
    val tags by viewModel.getTagsForNode(noteId).collectAsState(initial = emptyList())
    val allTags by viewModel.allTags.collectAsState()
    val relations by viewModel.getRelationsForNode(noteId).collectAsState(initial = emptyList())
    val attachments by viewModel.getAttachmentsForNode(noteId).collectAsState(initial = emptyList())
    val areas by viewModel.allAreas.collectAsState()
    val projects by viewModel.allProjects.collectAsState()
    val snapshots by viewModel.getSnapshotsForNode(noteId).collectAsState(initial = emptyList())
    val suggestionsWithPin by viewModel
        .getNoteSuggestions(noteId)
        .collectAsState(initial = emptyList())
    val suggestions = remember(suggestionsWithPin) { suggestionsWithPin.map { it.node } }

    val nodesMap = remember(nodes) { nodes.associateBy { it.node.id } }

    var showTagDialog by remember { mutableStateOf(false) }
    var showRelationDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showAreaDialog by remember { mutableStateOf(false) }
    var showProjectDialog by remember { mutableStateOf(false) }
    var showNoteTypeDialog by remember { mutableStateOf(false) }
    var showNoteStateDialog by remember { mutableStateOf(false) }
    var showSnapshotDialog by remember { mutableStateOf(false) }
    var showMergeDialog by remember { mutableStateOf(false) }
    var isAtomicMode by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showDueDialog by remember { mutableStateOf(false) }
    var showEstimateDialog by remember { mutableStateOf(false) }
    var showMediaTypeDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showMoreDialog by remember { mutableStateOf(false) }
    var showEnergyDialog by remember { mutableStateOf(false) }
    var showFrictionDialog by remember { mutableStateOf(false) }
    var showRecurringDialog by remember { mutableStateOf(false) }
    val areaOptions =
        remember(areas) {
            listOf(AssignmentOption(id = null, name = "Unassign")) +
                areas.map { AssignmentOption(id = it.id, name = it.title) }
        }
    val projectOptions =
        remember(projects) {
            listOf(AssignmentOption(id = null, name = "Unassign")) +
                projects.map { AssignmentOption(id = it.id, name = it.title) }
        }

    val actions: @Composable RowScope.() -> Unit = {
        IconButton(onClick = { viewModel.togglePermanentPin(node) }, modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)) {
            Icon(
                if (node.isPinned) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = if (node.isPinned) TajsOSTheme.Primary else TajsOSTheme.Text,
                modifier = Modifier.size(18.dp),
            )
        }
        IconButton(
            onClick = {
                scope.launch {
                    viewModel.getNodeById(noteId)?.let { original ->
                        viewModel.addNode(
                            title = "${original.title} (COPY)",
                            content = original.content,
                            type = original.type,
                            projectId = original.projectId,
                            areaId = original.areaId,
                        )
                    }
                }
            },
        ) {
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }
        IconButton(
            onClick = {
                viewModel.archiveNode(node)
                onBack()
            },
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }
        IconButton(onClick = { showMoreDialog = true }, modifier = Modifier.pointerHoverIcon(PointerIcon.Hand).size(48.dp)) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }
    }

    val context =
        NoteDetailContext(
            viewModel = viewModel,
            node = node,
            tags = tags,
            allTags = allTags,
            relations = relations,
            attachments = attachments,
            areas = areas,
            projects = projects,
            snapshots = snapshots,
            suggestions = suggestions,
            nodes = nodes,
            nodesMap = nodesMap,
            isAtomicMode = isAtomicMode,
            onNavigateToNode = onNavigateToNode,
            onNavigateToSearch = onNavigateToSearch,
            onUpdateTitle = { title -> viewModel.updateNode(node.copy(title = title)) },
            onUpdateContent = { content -> viewModel.updateNode(node.copy(content = content)) },
            onShowTagDialog = { showTagDialog = true },
            onShowRelationDialog = { showRelationDialog = true },
            onShowStatusDialog = { showStatusDialog = true },
            onShowAreaDialog = { showAreaDialog = true },
            onShowProjectDialog = { showProjectDialog = true },
            onShowNoteTypeDialog = { showNoteTypeDialog = true },
            onShowNoteStateDialog = { showNoteStateDialog = true },
            onShowSnapshotDialog = { showSnapshotDialog = true },
            onShowDueDialog = { showDueDialog = true },
            onShowReminderDialog = { showReminderDialog = true },
            onShowEnergyDialog = { showEnergyDialog = true },
            onShowFrictionDialog = { showFrictionDialog = true },
            onShowEstimateDialog = { showEstimateDialog = true },
            onShowMediaTypeDialog = { showMediaTypeDialog = true },
            onShowRatingDialog = { showRatingDialog = true },
            onShowRecurringDialog = { showRecurringDialog = true },
            onShowMoreDialog = { showMoreDialog = true },
            onToggleAtomicMode = { isAtomicMode = !isAtomicMode },
        )

    val surface = if (isDesktop) NoteDetailSurface.DESKTOP else NoteDetailSurface.MOBILE
    val plan =
        remember(surface, node.id, tags.size) { buildNoteDetailPlan(surface, context) }

    ScreenScaffold(
        screen = headerScreen,
        onNavigate = onNavigate,
        screenHeader =
            ScreenHeaderModel(
                breadcrumbs = screenBreadcrumbs(headerScreen),
                title = node.title,
                subtitle = node.itemKindOrNull()?.let { itemKindLabel(it) },
                actions = actions,
            ),
        backgroundColor = TajsOSTheme.Background,
        scrollBehavior = ScreenScrollBehavior.None,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingLg),
        ) {
            plan.primary.forEach { block ->
                NoteDetailBlocks.resolve(block.id)?.invoke(context)
            }
        }

        FloatingActionButton(
            onClick = { showRelationDialog = true },
            containerColor = TajsOSTheme.Primary,
            contentColor = TajsOSTheme.Background,
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            Icon(Icons.Default.Link, contentDescription = null)
        }
    }

    // Dialogs
    if (showTagDialog) {
        SelectorDialog(
            show = true,
            onDismiss = { showTagDialog = false },
            title = "SELECT TAG",
            prefix = "CONTEXT_TAG // FILTER",
            options = allTags,
            selectedOption = null,
            onSelect = { tag ->
                viewModel.attachTagToNode(noteId, tag.id)
                showTagDialog = false
            },
            optionName = { it.name },
            optionIcon = { Icons.Default.Tag },
            optionSubtext = { "TAG_ID_${it.id}" },
        )
    }

    if (showRelationDialog) {
        SelectorDialog(
            show = true,
            onDismiss = { showRelationDialog = false },
            title = "LINK NODE",
            prefix = "SYST_LINK // CONNECT",
            options = nodes.filter { it.node.id != noteId }.take(10),
            selectedOption = null,
            onSelect = { nodeWithPin ->
                viewModel.addRelation(
                    noteId,
                    nodeWithPin.node.id,
                    "RELATED",
                )
                showRelationDialog = false
            },
            optionName = { it.node.title },
            optionIcon = {
                when (it.node.type) {
                    "task" -> Icons.Default.CheckCircle
                    "project" -> Icons.AutoMirrored.Filled.List
                    "area" -> Icons.Default.Work
                    else -> Icons.AutoMirrored.Filled.Article
                }
            },
            optionSubtext = { it.node.type.uppercase() },
        )
    }

    if (showStatusDialog) {
        SelectorDialog(
            show = true,
            onDismiss = { showStatusDialog = false },
            title = "SET STATUS",
            prefix = "NODE_STATE // MODIFY",
            options = listOf("active", "done", "archived", "on_hold", "someday", "blocked"),
            selectedOption = node.status,
            onSelect = { status ->
                viewModel.updateNodeStatus(node, status)
                showStatusDialog = false
            },
            optionName = { it },
            optionIcon = {
                when (it) {
                    "active" -> Icons.Default.PlayArrow
                    "done" -> Icons.Default.Check
                    "archived" -> Icons.Default.Archive
                    "on_hold" -> Icons.Default.Pause
                    "someday" -> Icons.Default.CalendarToday
                    "blocked" -> Icons.Default.Block
                    else -> Icons.Default.Info
                }
            },
            optionSubtext = { "SYST_STATE_${it.uppercase()}" },
        )
    }

    if (showAreaDialog) {
        SelectorDialog(
            show = true,
            onDismiss = { showAreaDialog = false },
            title = "ASSIGN TO AREA",
            prefix = "DOMAIN_MAPPING // MOVE",
            options = areaOptions,
            selectedOption = areaOptions.find { it.id == node.areaId },
            onSelect = { area ->
                viewModel.updateNode(node.copy(areaId = area.id))
                showAreaDialog = false
            },
            optionName = { it.name },
            optionIcon = { Icons.Default.Place },
            optionSubtext = { it.id?.let { id -> "AREA_SYST_$id" } ?: "UNASSIGNED" },
        )
    }

    if (showProjectDialog) {
        SelectorDialog(
            show = true,
            onDismiss = { showProjectDialog = false },
            title = "ASSIGN TO PROJECT",
            prefix = "WORK_STREAM // MOVE",
            options = projectOptions,
            selectedOption = projectOptions.find { it.id == node.projectId },
            onSelect = { project ->
                viewModel.updateNode(node.copy(projectId = project.id))
                showProjectDialog = false
            },
            optionName = { it.name },
            optionIcon = { Icons.AutoMirrored.Filled.List },
            optionSubtext = { it.id?.let { id -> "PROJ_SYST_$id" } ?: "UNASSIGNED" },
        )
    }

    val nowMillis =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds()
    if (showDueDialog) {
        val dueOptions =
            listOf(
                "Today" to nowMillis,
                "Tomorrow" to nowMillis + 86400000,
                "1 Week" to nowMillis + 86400000 * 7,
                "Clear" to null,
            )

        SelectorDialog(
            show = true,
            onDismiss = { showDueDialog = false },
            title = "SET DUE DATE",
            prefix = "TEMPORAL_LOCK // SCHEDULE",
            options = dueOptions,
            selectedOption = null,
            onSelect = { option ->
                viewModel.updateNode(node.copy(dueAt = option.second))
                showDueDialog = false
            },
            optionName = { it.first },
            optionIcon = {
                when (it.first) {
                    "Today" -> Icons.Default.Today
                    "Tomorrow" -> Icons.Default.Event
                    "1 Week" -> Icons.AutoMirrored.Filled.NextPlan
                    else -> Icons.Default.Clear
                }
            },
            optionSubtext = { if (it.second != null) "SYST_TIME_${it.second}" else "CLEAR_FIELD" },
        )
    }

    if (showReminderDialog) {
        val reminderOptions =
            listOf(
                "1 Hour" to nowMillis + 3600000,
                "Tomorrow" to nowMillis + 86400000,
                "Next Week" to nowMillis + 86400000 * 7,
                "Clear" to null,
            )

        SelectorDialog(
            show = true,
            onDismiss = { showReminderDialog = false },
            title = "SET REMINDER",
            prefix = "NOTIFICATION_HOOK // ALARM",
            options = reminderOptions,
            selectedOption = null,
            onSelect = { option ->
                viewModel.updateNode(node.copy(reminderAt = option.second))
                showReminderDialog = false
            },
            optionName = { it.first },
            optionIcon = {
                when (it.first) {
                    "1 Hour" -> Icons.Default.Timer
                    "Tomorrow" -> Icons.Default.NotificationsActive
                    "Next Week" -> Icons.AutoMirrored.Filled.EventNote
                    else -> Icons.Default.Clear
                }
            },
            optionSubtext = { if (it.second != null) "SYST_ALARM_${it.second}" else "CLEAR_FIELD" },
        )
    }

    if (showSnapshotDialog) {
        SelectorDialog(
            show = true,
            onDismiss = { showSnapshotDialog = false },
            title = "VERSION HISTORY",
            prefix = "MODULE_CONTROL // ACCESS_MENU",
            options = snapshots,
            selectedOption = null,
            onSelect = { snapshot ->
                viewModel.restoreSnapshot(snapshot)
                showSnapshotDialog = false
            },
            optionName = {
                kotlin.time.Instant
                    .fromEpochMilliseconds(it.timestamp)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .toString()
                    .replace("T", " ")
            },
            optionIcon = { Icons.Default.History },
            optionSubtext = { it.content.take(20) + "..." },
        )
    }

    if (showMergeDialog) {
        SelectorDialog(
            show = true,
            onDismiss = { showMergeDialog = false },
            title = "MERGE NODES",
            prefix = "DATA_MERGE // CONSOLIDATE",
            options = nodes.filter { it.node.id != noteId && it.node.isNoteItem() },
            selectedOption = null,
            onSelect = { other ->
                viewModel.mergeNodes(noteId, listOf(other.node.id))
                showMergeDialog = false
            },
            optionName = { it.node.title },
            optionIcon = { Icons.Default.Merge },
            optionSubtext = { it.node.type.uppercase() },
        )
    }

    if (showNoteTypeDialog) {
        SelectorDialog(
            show = true,
            onDismiss = { showNoteTypeDialog = false },
            title = "SELECT NOTE TYPE",
            prefix = "TYPE_SPEC // CLASSIFY",
            options =
                listOf(
                    "thought",
                    "lecture",
                    "research",
                    "idea",
                    "reflection",
                    "bug",
                    "concept",
                    "evergreen",
                    "read_later",
                    "quote",
                    "meeting",
                    "reading",
                    "journal",
                ),
            selectedOption = node.noteType,
            onSelect = { type ->
                viewModel.updateNode(node.copy(noteType = type))
                showNoteTypeDialog = false
            },
            optionName = { it },
            optionIcon = {
                when (it) {
                    "thought" -> Icons.Default.Psychology
                    "lecture" -> Icons.Default.School
                    "research" -> Icons.Default.Search
                    "idea" -> Icons.Default.Lightbulb
                    "reflection" -> Icons.Default.Visibility
                    "bug" -> Icons.Default.BugReport
                    "concept" -> Icons.Default.Architecture
                    "evergreen" -> Icons.Default.Park
                    "read_later" -> Icons.Default.Bookmark
                    "quote" -> Icons.Default.FormatQuote
                    "meeting" -> Icons.Default.Groups
                    "reading" -> Icons.AutoMirrored.Filled.MenuBook
                    "journal" -> Icons.Default.HistoryEdu
                    else -> Icons.AutoMirrored.Filled.Article
                }
            },
            optionSubtext = { "NOTE_TYPE_${it.uppercase()}" },
        )
    }

    if (showNoteStateDialog) {
        SelectorDialog(
            show = true,
            onDismiss = { showNoteStateDialog = false },
            title = "SELECT STATE",
            prefix = "PROGRESSION_STATE // TRACK",
            options = listOf("raw", "highlighted", "distilled", "takeaway"),
            selectedOption = node.noteState,
            onSelect = { state ->
                viewModel.updateNode(node.copy(noteState = state))
                showNoteStateDialog = false
            },
            optionName = { it },
            optionIcon = {
                when (it) {
                    "raw" -> Icons.AutoMirrored.Filled.Article
                    "highlighted" -> Icons.Default.FormatQuote
                    "distilled" -> Icons.Default.FilterAlt
                    "takeaway" -> Icons.Default.Star
                    else -> Icons.Default.HistoryEdu
                }
            },
            optionSubtext = { "PROGRESS_LVL_${it.uppercase()}" },
        )
    }

    if (showMediaTypeDialog) {
        SelectorDialog(
            show = true,
            onDismiss = { showMediaTypeDialog = false },
            title = "SELECT MEDIA TYPE",
            prefix = "MEDIA_DATA // SOURCE",
            options = listOf("book", "article", "podcast", "video", "link"),
            selectedOption = node.mediaType,
            onSelect = { type ->
                viewModel.updateNode(node.copy(mediaType = type))
                showMediaTypeDialog = false
            },
            optionName = { it },
            optionIcon = {
                when (it) {
                    "book" -> Icons.AutoMirrored.Filled.MenuBook
                    "article" -> Icons.AutoMirrored.Filled.Article
                    "podcast" -> Icons.Default.Podcasts
                    "video" -> Icons.Default.PlayCircle
                    else -> Icons.Default.Link
                }
            },
            optionSubtext = { "MEDIA_TYPE_${it.uppercase()}" },
        )
    }

    if (showRatingDialog) {
        SelectorDialog(
            show = true,
            onDismiss = { showRatingDialog = false },
            title = "RATE RESOURCE",
            prefix = "VALUE_ASSESSMENT // RATE",
            options = (1..5).toList(),
            selectedOption = node.rating,
            onSelect = { stars ->
                viewModel.updateNode(node.copy(rating = stars))
                showRatingDialog = false
            },
            optionName = { "$it STARS" },
            optionIcon = { Icons.Default.Star },
            optionSubtext = { "RATING_VAL_$it" },
        )
    }

    if (showEstimateDialog) {
        SelectorDialog(
            show = true,
            onDismiss = { showEstimateDialog = false },
            title = "SET ESTIMATE",
            prefix = "TEMPORAL_COST // ALLOCATE",
            options = listOf(5, 15, 30, 60, 120, -1), // -1 for clear
            selectedOption = node.estimatedMinutes,
            onSelect = { mins ->
                viewModel.updateNode(node.copy(estimatedMinutes = if (mins == -1) null else mins))
                showEstimateDialog = false
            },
            optionName = { if (it == -1) "CLEAR" else "$it MIN" },
            optionIcon = { if (it == -1) Icons.Default.Clear else Icons.Default.Timer },
            optionSubtext = { if (it == -1) "CLEAR_VAL" else "EST_MIN_$it" },
        )
    }

    if (showEnergyDialog) {
        SelectorDialog(
            show = true,
            onDismiss = { showEnergyDialog = false },
            title = "SET ENERGY LEVEL",
            prefix = "BIOLOGICAL_COST // ASSESS",
            options = listOf(1, 2, 3),
            selectedOption = node.energyLevel,
            onSelect = { level ->
                viewModel.updateNode(node.copy(energyLevel = level))
                showEnergyDialog = false
            },
            optionName = {
                when (it) {
                    1 -> "LOW"
                    2 -> "MEDIUM"
                    3 -> "HIGH"
                    else -> "UNKNOWN"
                }
            },
            optionIcon = {
                when (it) {
                    1 -> Icons.Default.Battery1Bar
                    2 -> Icons.Default.Battery4Bar
                    3 -> Icons.Default.BatteryFull
                    else -> Icons.AutoMirrored.Filled.BatteryUnknown
                }
            },
            optionSubtext = { "ENERGY_LVL_$it" },
        )
    }

    if (showFrictionDialog) {
        SelectorDialog(
            show = true,
            onDismiss = { showFrictionDialog = false },
            title = "SET FRICTION",
            prefix = "RESISTANCE_LEVEL // ASSESS",
            options = listOf("easy", "annoying", "mentally_heavy", "unclear"),
            selectedOption = node.friction,
            onSelect = { friction ->
                viewModel.updateNode(node.copy(friction = friction))
                showFrictionDialog = false
            },
            optionName = { it.replace("_", " ") },
            optionIcon = {
                when (it) {
                    "easy" -> Icons.Default.Bolt
                    "annoying" -> Icons.Default.SentimentDissatisfied
                    "mentally_heavy" -> Icons.Default.Psychology
                    "unclear" -> Icons.AutoMirrored.Filled.Help
                    else -> Icons.Default.Info
                }
            },
            optionSubtext = { "FRICTION_STATE_${it.uppercase()}" },
        )
    }

    if (showMoreDialog) {
        val moreActions =
            remember(node, isAtomicMode) {
                mutableListOf<MoreAction>().apply {
                    if (node.isNoteItem()) {
                        add(
                            MoreAction(
                                "atomic",
                                if (isAtomicMode) "Disable Atomic Mode" else "Enable Atomic Mode",
                                Icons.Default.ZoomInMap,
                                "TOGGLE_UI_MODE",
                            ),
                        )
                        add(
                            MoreAction(
                                "snapshot",
                                "Create Snapshot",
                                Icons.Default.Save,
                                "VERSION_CONTROL",
                            ),
                        )
                        add(
                            MoreAction(
                                "history",
                                "Version History",
                                Icons.Default.History,
                                "ROLLBACK_SYST",
                            ),
                        )
                        add(
                            MoreAction(
                                "merge",
                                "Merge Nodes",
                                Icons.Default.Merge,
                                "CONSOLIDATE_DATA",
                            ),
                        )
                    }
                    if (node.inboxState) {
                        add(
                            MoreAction(
                                "processed",
                                "Mark as Processed",
                                Icons.Default.DoneAll,
                                "CLEAR_INBOX",
                            ),
                        )
                    }
                    if (node.isTaskItem()) {
                        add(
                            MoreAction(
                                "repeat",
                                "Repeat Task",
                                Icons.Default.Repeat,
                                "CLONE_ENTRY",
                            ),
                        )
                    }
                    add(
                        MoreAction(
                            "convert",
                            "Convert Type",
                            Icons.Default.Transform,
                            "MORPH_DATA",
                        ),
                    )
                }
            }

        SelectorDialog(
            show = true,
            title = "SYSTEM COMMANDS",
            prefix = "MODULE_CONTROL // ACCESS_MENU",
            options = moreActions,
            selectedOption = null,
            onSelect = { action ->
                showMoreDialog = false
                when (action.id) {
                    "atomic" -> {
                        isAtomicMode = !isAtomicMode
                    }

                    "snapshot" -> {
                        viewModel.createSnapshot(noteId)
                    }

                    "history" -> {
                        showSnapshotDialog = true
                    }

                    "merge" -> {
                        showMergeDialog = true
                    }

                    "processed" -> {
                        viewModel.markAsProcessed(noteId)
                    }

                    "repeat" -> {
                        scope.launch {
                            viewModel.getNodeById(noteId)?.let { original ->
                                viewModel.addNode(
                                    title = original.title,
                                    content = original.content,
                                    type = original.type,
                                    projectId = original.projectId,
                                    areaId = original.areaId,
                                    inboxState = false,
                                )
                            }
                        }
                    }

                    "convert" -> {
                        scope.launch {
                            viewModel.getNodeById(noteId)?.let { original ->
                                val targetType =
                                    when (original.itemKindOrNull()) {
                                        ItemKind.NOTE -> if (original.noteType == "idea") "project" else "task"
                                        ItemKind.TASK -> "project"
                                        ItemKind.RECORD -> "note"
                                        else -> original.type
                                    }
                                viewModel.updateNode(original.copy(type = targetType))
                            }
                        }
                    }
                }
            },
            optionName = { it.name },
            optionIcon = { it.icon },
            optionSubtext = { it.subtext },
            onDismiss = { showMoreDialog = false },
        )
    }

    if (showRecurringDialog) {
        SelectorDialog(
            show = true,
            onDismiss = { showRecurringDialog = false },
            title = "RECURRING SCHEDULE",
            prefix = "CADENCE_SPEC // FREQUENCY",
            options = listOf("NONE", "DAILY", "WEEKLY", "MONTHLY"),
            selectedOption = if (node.isRecurring) node.recurringInterval ?: "DAILY" else "NONE",
            onSelect = { interval ->
                viewModel.updateNode(
                    node.copy(
                        isRecurring = interval != "NONE",
                        recurringInterval = if (interval == "NONE") null else interval,
                    ),
                )
                showRecurringDialog = false
            },
            optionName = { it },
            optionIcon = {
                when (it) {
                    "NONE" -> Icons.Default.Close
                    "DAILY" -> Icons.Default.Today
                    "WEEKLY" -> Icons.Default.DateRange
                    "MONTHLY" -> Icons.Default.CalendarMonth
                    else -> Icons.Default.Repeat
                }
            },
            optionSubtext = { if (it == "NONE") "DISABLE_RECURRING" else "SYST_CADENCE_$it" },
        )
    }
}

@Composable
private fun itemKindLabel(itemKind: ItemKind): String =
    when (itemKind) {
        ItemKind.TASK -> stringResource(Res.string.type_task)
        ItemKind.NOTE -> stringResource(Res.string.type_note)
        ItemKind.RECORD -> stringResource(Res.string.type_record)
        ItemKind.PROJECT -> stringResource(Res.string.type_project)
        ItemKind.AREA -> stringResource(Res.string.type_area)
    }

private data class AssignmentOption(
    val id: Long?,
    val name: String,
)
