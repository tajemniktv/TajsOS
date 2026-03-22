/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Screen defines the navigation graph of the app.
 */
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "DASH", Icons.Default.Home)
    data object Search : Screen("search", "FIND", Icons.Default.Search)
    data object Today : Screen("today", "TODAY", Icons.Default.DateRange)
    data object Focus : Screen("focus", "FOCUS", Icons.Default.PlayArrow)
    data object Track : Screen("track", "TRACK", Icons.Default.CheckCircle)
    data object Tasks : Screen("tasks", "TASKS", Icons.AutoMirrored.Filled.List)
    data object Notes : Screen("notes", "NOTES", Icons.Default.Edit)
    data object NoteDetail : Screen("note/{noteId}", "NOTE", Icons.Default.Edit)
    data object Insights : Screen("insights", "STATS", Icons.Default.Info)
    data object Archive : Screen("archive", "ARCHIVE", Icons.Default.Delete)
    data object Calendar : Screen("calendar", "CAL", Icons.Default.DateRange)
    data object CalendarSettings : Screen("calendar_settings", "CAL OPTS", Icons.Default.Settings)
    data object Graph : Screen("graph", "GRAPH", Icons.Default.Share)
    data object Projects : Screen("projects", "PROJ", Icons.AutoMirrored.Filled.List)
    data object Areas : Screen("areas", "AREA", Icons.Default.LocationOn)
    data object ProjectDetail : Screen(
        "project/{projectId}", "PROJECT",
        Icons.AutoMirrored.Filled.List
    )
    data object AreaDetail : Screen("area/{areaId}", "AREA", Icons.Default.LocationOn)
    data object Settings : Screen("settings", "OPTS", Icons.Default.Settings)
}
