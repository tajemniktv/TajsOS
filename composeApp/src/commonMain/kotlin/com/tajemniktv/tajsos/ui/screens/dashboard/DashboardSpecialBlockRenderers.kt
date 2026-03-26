/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.modes.RecoveryBasicsBlock
import com.tajemniktv.tajsos.ui.components.nodes.SuggestionGroup

@Composable
internal fun renderBasicsBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    RecoveryBasicsBlock(
        onMedsClick = { context.onNavigateTo(Screen.Track) },
        onHydrationClick = { },
        onFoodClick = { },
    )
}

@Composable
internal fun renderShoppingListBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    _root_ide_package_.com.tajemniktv.tajsos.ui.screens.dashboard.ErrandListBlock(
        errands = context.dashboardState.shoppingList,
        onEdit = context.onEditNode,
    )
}

@Composable
internal fun renderTinyWinsBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    _root_ide_package_.com.tajemniktv.tajsos.ui.screens.dashboard.TinyVictoriesBlock(
        victories = context.dashboardState.tinyVictories,
        onEdit = context.onEditNode,
    )
}

@Composable
internal fun renderCurrentFocusBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    _root_ide_package_.com.tajemniktv.tajsos.ui.screens.dashboard.CurrentTaskBlock(
        activeTask = context.pinnedNodes.firstOrNull(),
        onEdit = context.onEditNode,
    )
}

@Composable
internal fun renderClassesBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    _root_ide_package_.com.tajemniktv.tajsos.ui.screens.dashboard.renderStudyModuleBlock(
        context = context,
        key = "classes",
    )
}

@Composable
internal fun renderAssignmentsBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    _root_ide_package_.com.tajemniktv.tajsos.ui.screens.dashboard.renderStudyModuleBlock(
        context = context,
        key = "assignments",
    )
}

@Composable
internal fun renderRevisionTargetsBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    _root_ide_package_.com.tajemniktv.tajsos.ui.screens.dashboard.renderStudyModuleBlock(
        context = context,
        key = "revision_targets",
    )
}

@Composable
private fun renderStudyModuleBlock(
    context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext,
    key: String,
) {
    val studentBoard by context.viewModel.studentBoardState.collectAsState()
    val studyNodes =
        when (key)
        {
            "classes" -> studentBoard.assignmentTracker
            "assignments" -> studentBoard.assignmentDeadlines
            else -> studentBoard.revisitBeforeExam
        }
    SuggestionGroup(
        title = "STUDY MODULE // ${key.uppercase()}",
        icon = Icons.Default.School,
        color = Color(0xFFFF9800),
        nodes = studyNodes.take(5),
        onEditNode = context.onEditNode,
    )
    TextButton(onClick = { context.onNavigateTo(Screen.Study) }) {
        Text("OPEN STUDY WORKSPACE")
    }
}

@Composable
internal fun renderPaperworkBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    SuggestionGroup(
        title = "ADMIN // PAPERWORK",
        icon = Icons.Default.Gavel,
        color = Color(0xFF607D8B),
        nodes = context.dashboardState.unresolvedBureaucracy,
        onEditNode = context.onEditNode,
    )
}
