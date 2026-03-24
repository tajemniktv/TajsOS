/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.common.DetailHeader
import com.tajemniktv.tajsos.ui.components.common.DetailSectionHeader
import com.tajemniktv.tajsos.ui.components.common.InfoCard
import com.tajemniktv.tajsos.ui.components.common.LinkedNodeItem
import com.tajemniktv.tajsos.ui.components.common.StatusCard
import com.tajemniktv.tajsos.ui.components.dashboard.AreaHealthCard
import com.tajemniktv.tajsos.ui.components.nodes.NodeCard
import com.tajemniktv.tajsos.ui.components.nodes.ProjectItem
import com.tajemniktv.tajsos.ui.design.components.ActionButton
import com.tajemniktv.tajsos.ui.design.components.DashCard
import com.tajemniktv.tajsos.ui.design.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment

/**
 *
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaDetailScreen(
    viewModel: MainViewModel,
    areaId: Long,
    onNavigateToProject: (Long) -> Unit,
    onEditNode: (Long) -> Unit,
    onBack: () -> Unit
) {
    val nodes by viewModel.allNodes.collectAsState()
    val nodeWithPin = remember(nodes, areaId) { nodes.find { it.node.id == areaId } }

    if (nodeWithPin == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                stringResource(Res.string.area_detail_not_found),
                modifier = Modifier.padding(TactileTheme.SpacingMd)
            )
        }
        return
    }

    val area = nodeWithPin.node

    LaunchedEffect(areaId) {
        viewModel.setLastActiveContext(null, areaId)
    }

    val projects by viewModel.getProjectsForArea(areaId).collectAsState(initial = emptyList())
    val nodesWithPinInArea by viewModel.getNodesForArea(areaId)
        .collectAsState(initial = emptyList())

    val foundationalNote = nodesWithPinInArea.find {
        it.node.type == "note" && it.tags.any { tag ->
            tag.name.equals(
                "foundational",
                ignoreCase = true
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "NEURAL_INTERFACE",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.detail_back),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.archiveNode(area)
                        onBack()
                    }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TactileTheme.Background,
                    titleContentColor = TactileTheme.Primary,
                    navigationIconContentColor = TactileTheme.Text,
                    actionIconContentColor = TactileTheme.Text
                )
            )
        },
        containerColor = TactileTheme.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(TactileTheme.Background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = TactileTheme.SpacingMd)
                .padding(bottom = TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingLg)
        ) {
            // Header
            DetailHeader(
                title = area.title,
                subtitle = "WORKSPACE AREA"
            )

            ActionButton(
                text = "NEW PROJECT",
                onClick = { /* New project logic */ },
                containerColor = TactileTheme.Primary,
                contentColor = TactileTheme.Background,
                icon = Icons.Default.Add,
                modifier = Modifier.fillMaxWidth()
            )

            // Health / Status
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                DetailSectionHeader(
                    title = "SYSTEM STATE",
                    icon = Icons.Default.BarChart
                )
                StatusCard(
                    status = area.status,
                    color = when (area.status) {
                        "active" -> TactileTheme.Success
                        "archived" -> TactileTheme.Muted
                        else -> TactileTheme.Accent
                    }
                )
            }

            // Area Progress
            val areaNodesTotal = nodes.filter { it.node.areaId == areaId }
            if (areaNodesTotal.isNotEmpty()) {
                val totalCount = areaNodesTotal.size
                val completedCount = areaNodesTotal.count { it.node.status == "done" }
                val areaProgress = completedCount.toFloat() / totalCount

                InfoCard(
                    title = "AREA COMPLETION",
                    value = "${(areaProgress * 100).toInt()}% COMPLETE",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = TactileTheme.Primary
                )
            }

            // Foundational Principle
            if (foundationalNote != null) {
                Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                    DetailSectionHeader(title = "CORE PRINCIPLE", icon = Icons.Default.AutoAwesome)
                    Surface(
                        onClick = { onEditNode(foundationalNote.node.id) },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Black.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(TactileTheme.RadiusLg),
                        border = BorderStroke(1.dp, TactileTheme.Primary.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(TactileTheme.SpacingLg)) {
                            Text(
                                text = foundationalNote.node.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TactileTheme.Text
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = foundationalNote.node.content.take(150) + "...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TactileTheme.Muted
                            )
                        }
                    }
                }
            }

            // Active Projects
            val activeProjects =
                projects.filter { it.status == "active" || it.projectStatus == "active" }
            if (activeProjects.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                    DetailSectionHeader(title = "ACTIVE PROJECTS", icon = Icons.Default.AccountTree)
                    activeProjects.forEach { project ->
                        LinkedNodeItem(
                            title = project.title,
                            subtitle = project.projectStatus ?: "Active Project",
                            icon = Icons.Default.Folder,
                            onClick = { onNavigateToProject(project.id) }
                        )
                    }
                }
            }

            // Recent Activity
            if (nodesWithPinInArea.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                    DetailSectionHeader(title = "RECENT ACTIVITY", icon = Icons.Default.History)
                    nodesWithPinInArea.sortedByDescending { it.node.updatedAt }.take(5)
                        .forEach { item ->
                            LinkedNodeItem(
                                title = item.node.title,
                                subtitle = item.node.type.uppercase(),
                                icon = when (item.node.type) {
                                    "task" -> Icons.Default.CheckCircle
                                    "note" -> Icons.Default.Description
                                    "idea" -> Icons.Default.Lightbulb
                                    else -> Icons.Default.Description
                                },
                                onClick = { onEditNode(item.node.id) }
                            )
                        }
                }
            }

            Spacer(Modifier.height(TactileTheme.SpacingXl))
        }
    }
}
