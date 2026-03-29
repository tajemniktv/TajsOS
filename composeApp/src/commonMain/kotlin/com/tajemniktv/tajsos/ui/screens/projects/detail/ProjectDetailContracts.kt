/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.projects.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.tajemniktv.tajsos.data.AttachmentEntity
import com.tajemniktv.tajsos.data.EventLogEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MainViewModel
import org.jetbrains.compose.resources.StringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.project_detail_assets
import tajsos.composeapp.generated.resources.project_detail_linked_notes
import tajsos.composeapp.generated.resources.project_detail_overview
import tajsos.composeapp.generated.resources.project_detail_review
import tajsos.composeapp.generated.resources.project_detail_timeline
import tajsos.composeapp.generated.resources.project_detail_work

/**
 * Defines the supported surfaces for project detail layout planning.
 */
enum class ProjectDetailSurface {
    MOBILE,
    DESKTOP,
}

/**
 * Identifies a logical project detail block.
 */
data class ProjectDetailBlock(
    val id: String,
)

/**
 * Structured layout plan for the project detail screen.
 */
data class ProjectDetailPlan(
    val primary: List<ProjectDetailBlock> = emptyList(),
    val secondary: List<ProjectDetailBlock> = emptyList(),
)

/**
 * Shared state and actions for project detail block renderers.
 */
data class ProjectDetailContext(
    val viewModel: MainViewModel,
    val project: NodeEntity,
    val areaName: String?,
    val healthLabel: String,
    val healthColor: Color,
    val progress: Float,
    val completedTasksCount: Int,
    val totalTasksCount: Int,
    val outcomeText: String,
    val targetDate: Long?,
    val selectedTab: ProjectDetailTab,
    val onSelectTab: (ProjectDetailTab) -> Unit,
    val nextActions: List<NodeEntity>,
    val blockedTasks: List<NodeEntity>,
    val linkedNotes: List<NodeEntity>,
    val linkedRecords: List<NodeEntity>,
    val linkedTasks: List<NodeEntity>,
    val timeline: List<EventLogEntity>,
    val attachments: List<AttachmentEntity>,
    val milestones: List<NodeEntity>,
    val tags: List<String>,
    val relatedNodeIds: List<Long>,
    val nodesById: Map<Long, NodeWithPin>,
    val attachmentNames: List<String>,
    val onEditNode: (Long) -> Unit,
    val onStatusClick: () -> Unit,
)

/**
 * Functional interface for rendering a project detail block.
 */
typealias ProjectDetailBlockRenderer = @Composable (ProjectDetailContext) -> Unit

/**
 * Defines the tabs available in the project detail view.
 */
enum class ProjectDetailTab(
    val labelRes: StringResource,
    val icon: ImageVector,
) {
    Overview(Res.string.project_detail_overview, Icons.Default.Insights),
    Work(Res.string.project_detail_work, Icons.Default.CheckCircle),
    Notes(Res.string.project_detail_linked_notes, Icons.Default.Description),
    Timeline(Res.string.project_detail_timeline, Icons.Default.History),
    Assets(Res.string.project_detail_assets, Icons.Default.AttachFile),
    Review(Res.string.project_detail_review, Icons.Default.EventAvailable),
}
