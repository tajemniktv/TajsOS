/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import tajsos.composeapp.generated.resources.*

/**
 * Screen defines the navigation graph of the app.
 */
sealed class Screen(
    val route: String,
    val label: StringResource,
    val icon: ImageVector,
    val hasInternalHeader: Boolean = false,
) {
    data object Dashboard :
        Screen("dashboard", Res.string.screen_dash, Icons.Default.Home, hasInternalHeader = true)

    data object Inbox : Screen("inbox", Res.string.screen_inbox, Icons.Default.Email)

    data object Search : Screen("search", Res.string.screen_search, Icons.Default.Search)

    data object Today : Screen("today", Res.string.screen_today, Icons.Default.DateRange)

    data object Focus : Screen("focus", Res.string.screen_focus, Icons.Default.PlayArrow)

    data object Track : Screen("track", Res.string.screen_track, Icons.Default.CheckCircle)

    data object Tasks : Screen("tasks", Res.string.screen_tasks, Icons.AutoMirrored.Filled.List)

    data object Notes : Screen("notes", Res.string.screen_notes, Icons.Default.Edit)

    data object NoteDetail : Screen(
        "note/{noteId}",
        Res.string.screen_note,
        Icons.Default.Edit,
        hasInternalHeader = true
    )

    data object Insights : Screen("insights", Res.string.screen_stats, Icons.Default.Info)

    data object Archive : Screen("archive", Res.string.screen_archive, Icons.Default.Delete)

    data object Calendar : Screen("calendar", Res.string.screen_cal, Icons.Default.Event)

    data object CalendarSettings :
        Screen("calendar_settings", Res.string.screen_cal_opts, Icons.Default.Settings)

    data object Graph : Screen("graph", Res.string.screen_graph, Icons.Default.Share)

    data object Projects :
        Screen("projects", Res.string.screen_proj, Icons.AutoMirrored.Filled.List)

    data object Areas : Screen("areas", Res.string.screen_area, Icons.Default.LocationOn)

    data object ProjectDetail : Screen(
        "project/{projectId}",
        Res.string.screen_project,
        Icons.AutoMirrored.Filled.List,
        hasInternalHeader = true
    )

    data object AreaDetail :
        Screen(
            "area/{areaId}",
            Res.string.screen_area,
            Icons.Default.LocationOn,
            hasInternalHeader = true
        )

    data object Settings : Screen("settings", Res.string.screen_opts, Icons.Default.Settings)

    data object Templates : Screen(
        "templates",
        Res.string.screen_templates,
        Icons.Default.Settings,
        hasInternalHeader = true
    )

    data object Review : Screen(
        "review",
        Res.string.screen_review,
        Icons.Default.RateReview,
        hasInternalHeader = true
    )

    companion object {
        val groupedItems by lazy {
            listOf(
                Res.string.nav_core to listOf(Dashboard, Inbox, Search),
                Res.string.nav_execution to listOf(Today, Tasks, Focus, Calendar),
                Res.string.nav_brain to listOf(Notes, Projects, Areas),
                Res.string.nav_status to listOf(Track, Insights, Graph, Review),
                Res.string.nav_system to listOf(Archive, Settings),
            )
        }
    }
}
