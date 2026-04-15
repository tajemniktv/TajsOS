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
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.dash_action_open_study
import tajsos.composeapp.generated.resources.dash_label_admin_paperwork
import tajsos.composeapp.generated.resources.dash_label_study_module

@Composable
internal fun renderBasicsBlock(context: DashboardBlockContext) {
    RecoveryBasicsBlock(
        onMedsClick = { context.onNavigateTo(Screen.Track) },
        onHydrationClick = { },
        onFoodClick = { },
    )
}

@Composable
internal fun renderShoppingListBlock(context: DashboardBlockContext) {
    ErrandListBlock(
        errands = context.dashboardState.shoppingList,
        onEdit = context.onEditNode,
    )
}

@Composable
internal fun renderTinyWinsBlock(context: DashboardBlockContext) {
    TinyVictoriesBlock(
        victories = context.dashboardState.tinyVictories,
        onEdit = context.onEditNode,
    )
}

@Composable
internal fun renderCurrentFocusBlock(context: DashboardBlockContext) {
    CurrentTaskBlock(
        activeTask = context.pinnedNodes.firstOrNull(),
        onEdit = context.onEditNode,
    )
}

@Composable
internal fun renderClassesBlock(context: DashboardBlockContext) {
    renderStudyModuleBlock(
        context = context,
        key = "classes",
    )
}

@Composable
internal fun renderAssignmentsBlock(context: DashboardBlockContext) {
    renderStudyModuleBlock(
        context = context,
        key = "assignments",
    )
}

@Composable
internal fun renderRevisionTargetsBlock(context: DashboardBlockContext) {
    renderStudyModuleBlock(
        context = context,
        key = "revision_targets",
    )
}

@Composable
internal fun renderStudyModuleBlock(
    context: DashboardBlockContext,
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
        title = stringResource(Res.string.dash_label_study_module, key.uppercase()),
        icon = Icons.Default.School,
        color = Color(0xFFFF9800),
        nodes = studyNodes.take(5),
        onEditNode = context.onEditNode,
    )
    TextButton(onClick = { context.onNavigateTo(Screen.Education) }) {
        Text(stringResource(Res.string.dash_action_open_study))
    }
}

@Composable
internal fun renderPaperworkBlock(context: DashboardBlockContext) {
    SuggestionGroup(
        title = stringResource(Res.string.dash_label_admin_paperwork),
        icon = Icons.Default.Gavel,
        color = Color(0xFF607D8B),
        nodes = context.dashboardState.unresolvedBureaucracy,
        onEditNode = context.onEditNode,
    )
}
