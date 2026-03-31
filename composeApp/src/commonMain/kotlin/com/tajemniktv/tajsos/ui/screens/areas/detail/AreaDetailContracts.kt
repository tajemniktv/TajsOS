/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.areas.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Reviews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.tajemniktv.tajsos.data.EventLogEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MainViewModel
import org.jetbrains.compose.resources.StringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.area_detail_tab_notes
import tajsos.composeapp.generated.resources.area_detail_tab_overview
import tajsos.composeapp.generated.resources.area_detail_tab_projects
import tajsos.composeapp.generated.resources.area_detail_tab_review
import tajsos.composeapp.generated.resources.area_detail_tab_timeline
import tajsos.composeapp.generated.resources.area_detail_tab_work

/**
 * Defines the supported surfaces for area detail layout planning.
 */
enum class AreaDetailSurface {
    MOBILE,
    DESKTOP,
}

/**
 * Identifies a logical area detail block.
 */
data class AreaDetailBlock(
    val id: String,
)

/**
 * Structured layout plan for the area detail screen.
 */
data class AreaDetailPlan(
    val primary: List<AreaDetailBlock> = emptyList(),
    val secondary: List<AreaDetailBlock> = emptyList(),
)

/**
 * Shared state and actions for area detail block renderers.
 */
data class AreaDetailContext(
    val viewModel: MainViewModel,
    val area: NodeEntity,
    val areaName: String,
    val statement: String,
    val health: String,
    val healthColor: Color,
    val load: Int,
    val cadence: String,
    val tab: AreaTab,
    val onTab: (AreaTab) -> Unit,
    val activeProjects: List<NodeEntity>,
    val openResponsibilities: List<NodeEntity>,
    val pressure: List<NodeEntity>,
    val tasks: List<NodeEntity>,
    val notes: List<NodeEntity>,
    val records: List<NodeEntity>,
    val recentItems: List<NodeEntity>,
    val logs: List<EventLogEntity>,
    val relationIds: List<Long>,
    val nodesById: Map<Long, NodeWithPin>,
    val attachmentNames: List<String>,
    val tags: List<String>,
    val onNavigateToProject: (Long) -> Unit,
    val onEditNode: (Long) -> Unit,
    val onBack: () -> Unit,
)

/**
 * Functional interface for rendering an area detail block.
 */
typealias AreaDetailBlockRenderer = @Composable (AreaDetailContext) -> Unit

/**
 * Defines the tabs available in the area detail view.
 */
enum class AreaTab(
    val label: StringResource,
    val icon: ImageVector,
) {
    Overview(Res.string.area_detail_tab_overview, Icons.Default.Insights),
    Projects(Res.string.area_detail_tab_projects, Icons.Default.Folder),
    Work(Res.string.area_detail_tab_work, Icons.Default.CheckCircle),
    Notes(Res.string.area_detail_tab_notes, Icons.Default.Description),
    Timeline(Res.string.area_detail_tab_timeline, Icons.Default.History),
    Review(Res.string.area_detail_tab_review, Icons.Default.Reviews),
}
