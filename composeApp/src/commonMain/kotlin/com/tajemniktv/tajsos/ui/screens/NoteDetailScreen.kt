/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.common.ConnectionCard
import com.tajemniktv.tajsos.ui.components.common.DetailHeader
import com.tajemniktv.tajsos.ui.components.common.DetailSectionHeader
import com.tajemniktv.tajsos.ui.components.common.InfoCard
import com.tajemniktv.tajsos.ui.components.common.LinkedNodeItem
import com.tajemniktv.tajsos.ui.components.common.SelectorDialog
import com.tajemniktv.tajsos.ui.components.common.StatusCard
import com.tajemniktv.tajsos.ui.components.nodes.DecisionDetailContent
import com.tajemniktv.tajsos.ui.components.ActionButton
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Detailed view for a single node (Note, Idea, Task, etc.)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteDetailScreen(
    viewModel: MainViewModel,
    noteId: Long,
    onBack: () -> Unit,
    onNavigateToNode: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
)
{
    val scope = rememberCoroutineScope()
    val nodes by viewModel.allNodes.collectAsState()
    val nodeWithPin = remember(nodes, noteId) { nodes.find { it.node.id == noteId } }

    if (nodeWithPin == null)
    {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (nodes.isEmpty())
            {
                CircularProgressIndicator(color = TactileTheme.Primary)
            } else
            {
                Text(
                    stringResource(Res.string.detail_not_found, noteId),
                    color = TactileTheme.Muted,
                )
            }
        }
        return
    }

    val node = nodeWithPin.node
    var title by remember { mutableStateOf(node.title) }
    var content by remember { mutableStateOf(node.content) }

    val tags by viewModel.getTagsForNode(noteId).collectAsState(initial = emptyList())
    val allTags by viewModel.allTags.collectAsState()
    val relations by viewModel.getRelationsForNode(noteId).collectAsState(initial = emptyList())
    val attachments by viewModel.getAttachmentsForNode(noteId).collectAsState(initial = emptyList())
    val areas by viewModel.allAreas.collectAsState()
    val projects by viewModel.allProjects.collectAsState()
    val snapshots by viewModel.getSnapshotsForNode(noteId).collectAsState(initial = emptyList())

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

    val suggestions by viewModel.getNoteSuggestions(noteId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "NEURAL_INTERFACE",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.detail_back),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.togglePermanentPin(node) }) {
                        Icon(
                            if (node.isPinned) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (node.isPinned) TactileTheme.Primary else TactileTheme.Text,
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
                    IconButton(onClick = { showMoreDialog = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TactileTheme.Background,
                    titleContentColor = TactileTheme.Primary,
                    navigationIconContentColor = TactileTheme.Text,
                    actionIconContentColor = TactileTheme.Text,
                ),
            )
        },
        containerColor = TactileTheme.Background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showRelationDialog = true },
                containerColor = TactileTheme.Primary,
                contentColor = TactileTheme.Background,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                modifier = Modifier.padding(16.dp),
            ) {
                Icon(Icons.Default.Link, contentDescription = null)
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(TactileTheme.Background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = TactileTheme.SpacingMd)
                .padding(bottom = TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingLg),
        ) {
            // Header
            DetailHeader(
                title = title,
                subtitle = "DETAIL VIEW",
            )

            ActionButton(
                text = "NEW NODE",
                onClick = {
                    scope.launch {
                        viewModel.addNode(
                            title = "NEW NODE",
                            type = "task",
                            projectId = node.projectId,
                            areaId = node.areaId,
                        )
                    }
                },
                containerColor = TactileTheme.Primary,
                contentColor = TactileTheme.Background,
                icon = Icons.Default.Add,
                modifier = Modifier.fillMaxWidth(),
            )

            // Relationship Inspector
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                DetailSectionHeader(
                    title = stringResource(Res.string.detail_relationship_inspector),
                    icon = Icons.Default.Hub,
                )

                val forwardLinks = relations.filter { it.fromNodeId == noteId }
                val backLinks = relations.filter { it.toNodeId == noteId }
                val nodesMap = remember(nodes) { nodes.associateBy { it.node.id } }

                if (relations.isEmpty() && suggestions.isEmpty())
                {
                    ConnectionCard(text = "CONNECT NODE", onClick = { showRelationDialog = true })
                } else
                {
                    if (backLinks.isNotEmpty())
                    {
                        Text(
                            stringResource(Res.string.detail_backlinks).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Muted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        backLinks.forEach { relation ->
                            nodesMap[relation.fromNodeId]?.node?.let { relatedNode ->
                                LinkedNodeItem(
                                    title = relatedNode.title,
                                    subtitle = "Linked context",
                                    icon = when (relatedNode.type)
                                    {
                                        "project" -> Icons.AutoMirrored.Filled.List
                                        "area"    -> Icons.Default.Work
                                        else      -> Icons.AutoMirrored.Filled.Article
                                    },
                                    onClick = { onNavigateToNode(relatedNode.id) },
                                )
                            }
                        }
                    }

                    forwardLinks.forEach { relation ->
                        nodesMap[relation.toNodeId]?.node?.let { relatedNode ->
                            LinkedNodeItem(
                                title = relatedNode.title,
                                subtitle = relation.relationType,
                                icon = when (relatedNode.type)
                                {
                                    "task"    -> Icons.Default.CheckCircle
                                    "project" -> Icons.AutoMirrored.Filled.List
                                    else      -> Icons.AutoMirrored.Filled.Article
                                },
                                onClick = { onNavigateToNode(relatedNode.id) },
                            )
                        }
                    }
                }
            }

            // Status Card
            StatusCard(
                status = node.status,
                color = when (node.status)
                {
                    "active"   -> TactileTheme.Success
                    "done"     -> TactileTheme.Primary
                    "archived" -> TactileTheme.Muted
                    "blocked"  -> TactileTheme.Error
                    else       -> TactileTheme.Accent
                },
                onClick = { showStatusDialog = true },
            )

            if (node.type == "decision")
            {
                DecisionDetailContent(
                    viewModel = viewModel,
                    node = node,
                    onNavigateToProject = { id ->
                        onNavigateToNode(id)
                    },
                )
            }

            // Info Grid (Due Date & Reminder)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
            ) {
                InfoCard(
                    title = "DUE AT",
                    value = node.dueAt?.let {
                        Instant.fromEpochMilliseconds(it)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                    } ?: "None",
                    icon = Icons.Default.CalendarToday,
                    modifier = Modifier.weight(1f),
                    onClick = { showDueDialog = true },
                )
                InfoCard(
                    title = "REMINDER",
                    value = node.reminderAt?.let { "Set" } ?: "None",
                    icon = Icons.Default.Notifications,
                    modifier = Modifier.weight(1f),
                    onClick = { showReminderDialog = true },
                )
            }

            // Task Specific Metadata
            if (node.type == "task")
            {
                Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                    DetailSectionHeader(
                        title = "OPERATIONAL METADATA",
                        icon = Icons.Default.Settings,
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = TactileTheme.Surface,
                        shape = RoundedCornerShape(TactileTheme.RadiusMd),
                        border = BorderStroke(1.dp, TactileTheme.Border),
                    ) {
                        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f).clickable {
                                        showEnergyDialog = true
                                    },
                                ) {
                                    Text(
                                        "ENERGY",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TactileTheme.Muted,
                                        fontSize = 8.sp,
                                    )
                                    Text(
                                        when (node.energyLevel)
                                        {
                                            1    -> "LOW"
                                            2    -> "MED"
                                            3    -> "HIGH"
                                            else -> "NOT SET"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = when (node.energyLevel)
                                        {
                                            1    -> TactileTheme.Success
                                            2    -> TactileTheme.Primary
                                            3    -> TactileTheme.Error
                                            else -> TactileTheme.Text
                                        },
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f).clickable {
                                        showFrictionDialog = true
                                    },
                                ) {
                                    Text(
                                        "FRICTION",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TactileTheme.Muted,
                                        fontSize = 8.sp,
                                    )
                                    Text(
                                        (node.friction ?: "STANDARD").uppercase().replace("_", " "),
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
                                    modifier = Modifier.weight(1f)
                                        .clickable { showEstimateDialog = true },
                                ) {
                                    Text(
                                        "ESTIMATE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TactileTheme.Muted,
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
                                            color = TactileTheme.Muted,
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

                    if (node.nextSmallestStep != null)
                    {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = TactileTheme.Accent.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(TactileTheme.RadiusMd),
                            border = BorderStroke(1.dp, TactileTheme.Accent.copy(alpha = 0.3f)),
                        ) {
                            Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                                Text(
                                    "NEXT SMALLEST STEP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TactileTheme.Accent,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                BasicTextField(
                                    value = node.nextSmallestStep!!,
                                    onValueChange = {
                                        viewModel.updateNode(
                                            node.copy(
                                                nextSmallestStep = it,
                                            ),
                                        )
                                    },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = TactileTheme.Text,
                                        fontWeight = FontWeight.Medium,
                                    ),
                                    cursorBrush = SolidColor(TactileTheme.Accent),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
            }

            // Resource Specific Metadata
            if (node.type == "resource")
            {
                Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                    DetailSectionHeader(
                        title = "RESOURCE DATA",
                        icon = Icons.AutoMirrored.Filled.LibraryBooks,
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = TactileTheme.Surface,
                        shape = RoundedCornerShape(TactileTheme.RadiusMd),
                        border = BorderStroke(1.dp, TactileTheme.Border),
                    ) {
                        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                        .clickable { showMediaTypeDialog = true },
                                ) {
                                    Text(
                                        "MEDIA TYPE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TactileTheme.Muted,
                                        fontSize = 8.sp,
                                    )
                                    Text(
                                        (node.mediaType ?: "Link").uppercase(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f)
                                        .clickable { showRatingDialog = true },
                                ) {
                                    Text(
                                        "RATING",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TactileTheme.Muted,
                                        fontSize = 8.sp,
                                    )
                                    Text(
                                        if (node.rating != null) "⭐".repeat(node.rating!!) else "UNRATED",
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
                                    color = TactileTheme.Muted,
                                    fontSize = 8.sp,
                                )
                                BasicTextField(
                                    value = node.author ?: "",
                                    onValueChange = { viewModel.updateNode(node.copy(author = it)) },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = TactileTheme.Text,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
            }

            // Context Graph (Tags)
            if (tags.isNotEmpty() || showTagDialog)
            {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(TactileTheme.RadiusLg),
                    border = BorderStroke(1.dp, TactileTheme.Border.copy(alpha = 0.5f)),
                ) {
                    Column(modifier = Modifier.padding(TactileTheme.SpacingLg)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(32.dp)
                                        .background(TactileTheme.Surface, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Default.Psychology,
                                        null,
                                        tint = TactileTheme.Primary,
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
                                color = TactileTheme.Surface,
                                shape = CircleShape,
                                border = BorderStroke(1.dp, TactileTheme.Border),
                            ) {
                                Text(
                                    "${tags.size} Tags",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    color = TactileTheme.Muted,
                                )
                            }
                        }

                        Spacer(Modifier.height(TactileTheme.SpacingLg))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            tags.forEach { tag ->
                                AssistChip(
                                    onClick = { viewModel.updateSearchQuery("#${tag.name}"); onNavigateToSearch() },
                                    label = { Text(tag.name) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            null,
                                            modifier = Modifier.size(14.dp).clickable {
                                                viewModel.detachTagFromNode(
                                                    noteId,
                                                    tag.id,
                                                )
                                            },
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = TactileTheme.Surface,
                                        labelColor = TactileTheme.Text,
                                    ),
                                    shape = RoundedCornerShape(TactileTheme.RadiusSm),
                                )
                            }
                            IconButton(
                                onClick = { showTagDialog = true },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(Icons.Default.Add, null, tint = TactileTheme.Primary)
                            }
                        }
                    }
                }
            }

            // Cadence / Recurring
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = TactileTheme.Surface,
                    shape = RoundedCornerShape(TactileTheme.RadiusMd),
                    border = BorderStroke(1.dp, TactileTheme.Border),
                ) {
                    Row(
                        modifier = Modifier.padding(TactileTheme.SpacingMd),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.Repeat,
                            null,
                            tint = TactileTheme.Muted,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(TactileTheme.SpacingMd))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "RECURRING SCHEDULE",
                                style = MaterialTheme.typography.labelSmall,
                                color = TactileTheme.Muted,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                if (node.isRecurring) node.recurringInterval
                                        ?: "Set" else "One-time Event",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        TextButton(
                            onClick = {
                                showRecurringDialog = true
                            },
                        ) {
                            Text(
                                "MODIFY",
                                style = MaterialTheme.typography.labelSmall,
                                color = TactileTheme.Muted,
                            )
                        }
                    }
                }
            }

            // Organization Card (Area & Project)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = BorderStroke(1.dp, TactileTheme.Border),
            ) {
                Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                    Text(
                        "ORGANIZATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(TactileTheme.SpacingSm))

                    val area = areas.find { it.id == node.areaId }
                    val project = projects.find { it.id == node.projectId }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f).clickable { showAreaDialog = true }) {
                            Text(
                                "AREA",
                                style = MaterialTheme.typography.labelSmall,
                                color = TactileTheme.Primary,
                                fontSize = 8.sp,
                            )
                            Text(
                                area?.title ?: "Unassigned",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f).clickable { showProjectDialog = true },
                        ) {
                            Text(
                                "PROJECT",
                                style = MaterialTheme.typography.labelSmall,
                                color = TactileTheme.Primary,
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

            // Attachments
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                DetailSectionHeader(title = "ATTACHMENTS", icon = Icons.Default.Attachment)
                attachments.forEach { attachment ->
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
                        viewModel.addAttachment(noteId, "URL", "https://example.com", "New Link")
                    },
                )
            }

            // Knowledge / Media Type (if applicable)
            if (node.type == "note" || node.type == "idea")
            {
                Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                    DetailSectionHeader(
                        title = "KNOWLEDGE CONFIG",
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = TactileTheme.Surface,
                        shape = RoundedCornerShape(TactileTheme.RadiusMd),
                        border = BorderStroke(1.dp, TactileTheme.Border),
                    ) {
                        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                        .clickable { showNoteTypeDialog = true },
                                ) {
                                    Text(
                                        "NOTE TYPE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TactileTheme.Muted,
                                        fontSize = 8.sp,
                                    )
                                    Text(
                                        (node.noteType ?: "Standard").uppercase(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f)
                                        .clickable { showNoteStateDialog = true },
                                ) {
                                    Text(
                                        "STATE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TactileTheme.Muted,
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

            // Content Editor
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                DetailSectionHeader(title = "CONTENT", icon = Icons.Default.Description)
                BasicTextField(
                    value = content,
                    onValueChange = {
                        content = it
                        viewModel.updateNode(node.copy(content = it))
                    },
                    textStyle = (if (isAtomicMode) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge).copy(
                        color = TactileTheme.Text,
                    ),
                    cursorBrush = SolidColor(TactileTheme.Primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp)
                        .background(TactileTheme.Surface, RoundedCornerShape(TactileTheme.RadiusMd))
                        .border(
                            1.dp,
                            TactileTheme.Border,
                            RoundedCornerShape(TactileTheme.RadiusMd),
                        )
                        .padding(TactileTheme.SpacingMd),
                    decorationBox = { innerTextField ->
                        Box {
                            if (content.isEmpty())
                            {
                                Text(
                                    stringResource(Res.string.detail_start_writing),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TactileTheme.Muted,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }

            Spacer(Modifier.height(TactileTheme.SpacingXl))
        }
    }

    // Dialogs
    SelectorDialog(
        show = showTagDialog,
        onDismiss = { showTagDialog = false },
        title = "SELECT TAG",
        options = allTags,
        selectedOption = null,
        onSelect = { tag -> viewModel.attachTagToNode(noteId, tag.id); showTagDialog = false },
        optionName = { it.name },
        optionIcon = { Icons.Default.Tag },
        optionSubtext = { "TAG_ID_${it.id}" },
    )

    SelectorDialog(
        show = showRelationDialog,
        onDismiss = { showRelationDialog = false },
        title = "LINK NODE",
        options = nodes.filter { it.node.id != noteId }.take(10),
        selectedOption = null,
        onSelect = { nodeWithPin ->
            viewModel.addRelation(
                noteId,
                nodeWithPin.node.id,
                "RELATED",
            ); showRelationDialog = false
        },
        optionName = { it.node.title },
        optionIcon = {
            when (it.node.type)
            {
                "task"    -> Icons.Default.CheckCircle
                "project" -> Icons.AutoMirrored.Filled.List
                "area"    -> Icons.Default.Work
                else      -> Icons.AutoMirrored.Filled.Article
            }
        },
        optionSubtext = { it.node.type.uppercase() },
    )

    SelectorDialog(
        show = showStatusDialog,
        onDismiss = { showStatusDialog = false },
        title = "SET STATUS",
        options = listOf("active", "done", "archived", "on_hold", "someday", "blocked"),
        selectedOption = node.status,
        onSelect = { status -> viewModel.updateNodeStatus(node, status); showStatusDialog = false },
        optionName = { it },
        optionIcon = {
            when (it)
            {
                "active"   -> Icons.Default.PlayArrow
                "done"     -> Icons.Default.Check
                "archived" -> Icons.Default.Archive
                "on_hold"  -> Icons.Default.Pause
                "someday"  -> Icons.Default.CalendarToday
                "blocked"  -> Icons.Default.Block
                else       -> Icons.Default.Info
            }
        },
        optionSubtext = { "SYST_STATE_${it.uppercase()}" },
    )

    SelectorDialog(
        show = showAreaDialog,
        onDismiss = { showAreaDialog = false },
        title = "ASSIGN TO AREA",
        options = areas,
        selectedOption = areas.find { it.id == node.areaId },
        onSelect = { area ->
            viewModel.updateNode(node.copy(areaId = area.id)); showAreaDialog = false
        },
        optionName = { it.title },
        optionIcon = { Icons.Default.Place },
        optionSubtext = { "AREA_SYST_${it.id}" },
    )

    SelectorDialog(
        show = showProjectDialog,
        onDismiss = { showProjectDialog = false },
        title = "ASSIGN TO PROJECT",
        options = projects,
        selectedOption = projects.find { it.id == node.projectId },
        onSelect = { project ->
            viewModel.updateNode(node.copy(projectId = project.id)); showProjectDialog = false
        },
        optionName = { it.title },
        optionIcon = { Icons.AutoMirrored.Filled.List },
        optionSubtext = { "PROJ_SYST_${it.id}" },
    )

    val nowMillis = Clock.System.now().toEpochMilliseconds()
    val dueOptions = listOf(
        "Today" to nowMillis,
        "Tomorrow" to nowMillis + 86400000,
        "1 Week" to nowMillis + 86400000 * 7,
        "Clear" to null,
    )

    SelectorDialog(
        show = showDueDialog,
        onDismiss = { showDueDialog = false },
        title = "SET DUE DATE",
        options = dueOptions,
        selectedOption = null,
        onSelect = { option ->
            viewModel.updateNode(node.copy(dueAt = option.second)); showDueDialog = false
        },
        optionName = { it.first },
        optionIcon = {
            when (it.first)
            {
                "Today"    -> Icons.Default.Today
                "Tomorrow" -> Icons.Default.Event
                "1 Week"   -> Icons.AutoMirrored.Filled.NextPlan
                else       -> Icons.Default.Clear
            }
        },
        optionSubtext = { if (it.second != null) "SYST_TIME_${it.second}" else "CLEAR_FIELD" },
    )

    val reminderOptions = listOf(
        "1 Hour" to nowMillis + 3600000,
        "Tomorrow" to nowMillis + 86400000,
        "Next Week" to nowMillis + 86400000 * 7,
        "Clear" to null,
    )

    SelectorDialog(
        show = showReminderDialog,
        onDismiss = { showReminderDialog = false },
        title = "SET REMINDER",
        options = reminderOptions,
        selectedOption = null,
        onSelect = { option ->
            viewModel.updateNode(node.copy(reminderAt = option.second)); showReminderDialog = false
        },
        optionName = { it.first },
        optionIcon = {
            when (it.first)
            {
                "1 Hour"    -> Icons.Default.Timer
                "Tomorrow"  -> Icons.Default.NotificationsActive
                "Next Week" -> Icons.AutoMirrored.Filled.EventNote
                else        -> Icons.Default.Clear
            }
        },
        optionSubtext = { if (it.second != null) "SYST_ALARM_${it.second}" else "CLEAR_FIELD" },
    )

    SelectorDialog(
        show = showSnapshotDialog,
        onDismiss = { showSnapshotDialog = false },
        title = "VERSION HISTORY",
        options = snapshots,
        selectedOption = null,
        onSelect = { snapshot ->
            viewModel.restoreSnapshot(snapshot)
            content = snapshot.content
            title = snapshot.title
            showSnapshotDialog = false
        },
        optionName = {
            Instant.fromEpochMilliseconds(it.timestamp)
                .toLocalDateTime(TimeZone.currentSystemDefault()).toString().replace("T", " ")
        },
        optionIcon = { Icons.Default.History },
        optionSubtext = { it.content.take(20) + "..." },
    )

    SelectorDialog(
        show = showMergeDialog,
        onDismiss = { showMergeDialog = false },
        title = "MERGE NODES",
        options = nodes.filter { it.node.id != noteId && (it.node.type == "note" || it.node.type == "idea") },
        selectedOption = null,
        onSelect = { other ->
            viewModel.mergeNodes(noteId, listOf(other.node.id))
            showMergeDialog = false
        },
        optionName = { it.node.title },
        optionIcon = { Icons.Default.Merge },
        optionSubtext = { it.node.type.uppercase() },
    )

    SelectorDialog(
        show = showNoteTypeDialog,
        onDismiss = { showNoteTypeDialog = false },
        title = "SELECT NOTE TYPE",
        options = listOf(
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
            viewModel.updateNode(node.copy(noteType = type)); showNoteTypeDialog = false
        },
        optionName = { it },
        optionIcon = {
            when (it)
            {
                "thought"    -> Icons.Default.Psychology
                "lecture"    -> Icons.Default.School
                "research"   -> Icons.Default.Search
                "idea"       -> Icons.Default.Lightbulb
                "reflection" -> Icons.Default.Visibility
                "bug"        -> Icons.Default.BugReport
                "concept"    -> Icons.Default.Architecture
                "evergreen"  -> Icons.Default.Park
                "read_later" -> Icons.Default.Bookmark
                "quote"      -> Icons.Default.FormatQuote
                "meeting"    -> Icons.Default.Groups
                "reading"    -> Icons.AutoMirrored.Filled.MenuBook
                "journal"    -> Icons.Default.HistoryEdu
                else         -> Icons.AutoMirrored.Filled.Article
            }
        },
        optionSubtext = { "NOTE_TYPE_${it.uppercase()}" },
    )

    SelectorDialog(
        show = showNoteStateDialog,
        onDismiss = { showNoteStateDialog = false },
        title = "SELECT STATE",
        options = listOf("raw", "highlighted", "distilled", "takeaway"),
        selectedOption = node.noteState,
        onSelect = { state ->
            viewModel.updateNode(node.copy(noteState = state)); showNoteStateDialog = false
        },
        optionName = { it },
        optionIcon = {
            when (it)
            {
                "raw"         -> Icons.AutoMirrored.Filled.Article
                "highlighted" -> Icons.Default.FormatQuote
                "distilled"   -> Icons.Default.FilterAlt
                "takeaway"    -> Icons.Default.Star
                else          -> Icons.Default.HistoryEdu
            }
        },
        optionSubtext = { "PROGRESS_LVL_${it.uppercase()}" },
    )

    SelectorDialog(
        show = showMediaTypeDialog,
        onDismiss = { showMediaTypeDialog = false },
        title = "SELECT MEDIA TYPE",
        options = listOf("book", "article", "podcast", "video", "link"),
        selectedOption = node.mediaType,
        onSelect = { type ->
            viewModel.updateNode(node.copy(mediaType = type)); showMediaTypeDialog = false
        },
        optionName = { it },
        optionIcon = {
            when (it)
            {
                "book"    -> Icons.AutoMirrored.Filled.MenuBook
                "article" -> Icons.AutoMirrored.Filled.Article
                "podcast" -> Icons.Default.Podcasts
                "video"   -> Icons.Default.PlayCircle
                else      -> Icons.Default.Link
            }
        },
        optionSubtext = { "MEDIA_TYPE_${it.uppercase()}" },
    )

    SelectorDialog(
        show = showRatingDialog,
        onDismiss = { showRatingDialog = false },
        title = "RATE RESOURCE",
        options = (1..5).toList(),
        selectedOption = node.rating,
        onSelect = { stars ->
            viewModel.updateNode(node.copy(rating = stars)); showRatingDialog = false
        },
        optionName = { "$it STARS" },
        optionIcon = { Icons.Default.Star },
        optionSubtext = { "RATING_VAL_$it" },
    )

    SelectorDialog(
        show = showEstimateDialog,
        onDismiss = { showEstimateDialog = false },
        title = "SET ESTIMATE",
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

    SelectorDialog(
        show = showEnergyDialog,
        onDismiss = { showEnergyDialog = false },
        title = "SET ENERGY LEVEL",
        options = listOf(1, 2, 3),
        selectedOption = node.energyLevel,
        onSelect = { level ->
            viewModel.updateNode(node.copy(energyLevel = level)); showEnergyDialog = false
        },
        optionName = {
            when (it)
            {
                1    -> "LOW"
                2    -> "MEDIUM"
                3    -> "HIGH"
                else -> "UNKNOWN"
            }
        },
        optionIcon = {
            when (it)
            {
                1    -> Icons.Default.Battery1Bar
                2    -> Icons.Default.Battery4Bar
                3    -> Icons.Default.BatteryFull
                else -> Icons.AutoMirrored.Filled.BatteryUnknown
            }
        },
        optionSubtext = { "ENERGY_LVL_$it" },
    )

    SelectorDialog(
        show = showFrictionDialog,
        onDismiss = { showFrictionDialog = false },
        title = "SET FRICTION",
        options = listOf("easy", "annoying", "mentally_heavy", "unclear"),
        selectedOption = node.friction,
        onSelect = { friction ->
            viewModel.updateNode(node.copy(friction = friction)); showFrictionDialog = false
        },
        optionName = { it.replace("_", " ") },
        optionIcon = {
            when (it)
            {
                "easy"           -> Icons.Default.Bolt
                "annoying"       -> Icons.Default.SentimentDissatisfied
                "mentally_heavy" -> Icons.Default.Psychology
                "unclear"        -> Icons.AutoMirrored.Filled.Help
                else             -> Icons.Default.Info
            }
        },
        optionSubtext = { "FRICTION_STATE_${it.uppercase()}" },
    )

    val moreActions = remember(node, isAtomicMode) {
        mutableListOf<MoreAction>().apply {
            if (node.type == "note" || node.type == "idea")
            {
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
                add(MoreAction("merge", "Merge Nodes", Icons.Default.Merge, "CONSOLIDATE_DATA"))
            }
            if (node.inboxState)
            {
                add(
                    MoreAction(
                        "processed",
                        "Mark as Processed",
                        Icons.Default.DoneAll,
                        "CLEAR_INBOX",
                    ),
                )
            }
            if (node.type == "task")
            {
                add(MoreAction("repeat", "Repeat Task", Icons.Default.Repeat, "CLONE_ENTRY"))
            }
            add(MoreAction("convert", "Convert Type", Icons.Default.Transform, "MORPH_DATA"))
        }
    }

    SelectorDialog(
        show = showMoreDialog,
        onDismiss = { showMoreDialog = false },
        title = "SYSTEM COMMANDS",
        prefix = "MODULE_CONTROL // ACCESS_MENU",
        options = moreActions,
        selectedOption = null,
        onSelect = { action ->
            showMoreDialog = false
            when (action.id)
            {
                "atomic"    -> isAtomicMode = !isAtomicMode
                "snapshot"  -> viewModel.createSnapshot(noteId)
                "history"   -> showSnapshotDialog = true
                "merge"     -> showMergeDialog = true
                "processed" -> viewModel.markAsProcessed(noteId)
                "repeat"    ->
                {
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

                "convert"   ->
                {
                    scope.launch {
                        viewModel.getNodeById(noteId)?.let { original ->
                            val targetType = when (original.type)
                            {
                                "note", "idea" -> if (original.noteType == "idea") "project" else "task"
                                "task"         -> "project"
                                else           -> original.type
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
    )

    SelectorDialog(
        show = showRecurringDialog,
        onDismiss = { showRecurringDialog = false },
        title = "RECURRING SCHEDULE",
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
            when (it)
            {
                "NONE"    -> Icons.Default.Close
                "DAILY"   -> Icons.Default.Today
                "WEEKLY"  -> Icons.Default.DateRange
                "MONTHLY" -> Icons.Default.CalendarMonth
                else      -> Icons.Default.Repeat
            }
        },
        optionSubtext = { if (it == "NONE") "DISABLE_RECURRING" else "SYST_CADENCE_$it" },
    )
}

data class MoreAction(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val subtext: String = "",
)
