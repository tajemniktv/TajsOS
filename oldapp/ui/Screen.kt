/*
 * Copyright (c) TajemnikTV 2026. All rights reserved.
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
    object Dashboard : com.tajemniktv.tajsos.ui.Screen("dashboard", "DASH", Icons.Default.Home)
    object Today : com.tajemniktv.tajsos.ui.Screen("today", "TODAY", Icons.Default.DateRange)
    object Focus : com.tajemniktv.tajsos.ui.Screen("focus", "FOCUS", Icons.Default.PlayArrow)
    object Track : com.tajemniktv.tajsos.ui.Screen("track", "TRACK", Icons.Default.CheckCircle)
    object Tasks : com.tajemniktv.tajsos.ui.Screen("tasks", "TASKS", Icons.AutoMirrored.Filled.List)
    object Notes : com.tajemniktv.tajsos.ui.Screen("notes", "NOTES", Icons.Default.Edit)
    object NoteDetail : com.tajemniktv.tajsos.ui.Screen("note/{noteId}", "NOTE", Icons.Default.Edit)
    object Insights : com.tajemniktv.tajsos.ui.Screen("insights", "STATS", Icons.Default.Info)
    object Projects : com.tajemniktv.tajsos.ui.Screen("projects", "PROJ", Icons.Default.List)
    object Areas : com.tajemniktv.tajsos.ui.Screen("areas", "AREA", Icons.Default.LocationOn)
    object ProjectDetail : com.tajemniktv.tajsos.ui.Screen("project/{projectId}", "PROJECT", Icons.Default.List)
    object AreaDetail : com.tajemniktv.tajsos.ui.Screen("area/{areaId}", "AREA", Icons.Default.LocationOn)
    object Settings : com.tajemniktv.tajsos.ui.Screen("settings", "OPTS", Icons.Default.Settings)
}
