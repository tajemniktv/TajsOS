/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import com.tajemniktv.tajsos.ui.Screen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScreenTest {
    private val allScreens: List<Screen> =
        listOf(
            Screen.Dashboard,
            Screen.Search,
            Screen.Today,
            Screen.Focus,
            Screen.Track,
            Screen.Tasks,
            Screen.Notes,
            Screen.NoteDetail,
            Screen.Insights,
            Screen.Archive,
            Screen.Projects,
            Screen.Areas,
            Screen.ProjectDetail,
            Screen.AreaDetail,
            Screen.Settings,
            Screen.Graph,
            Screen.Inbox,
            Screen.Calendar,
            Screen.CalendarSettings,
            Screen.Templates,
            Screen.Review,
            Screen.Profile,
            Screen.Decisions,
            Screen.OpenLoops,
            Screen.Protocols,
            Screen.TimeArchitecture,
            Screen.Places,
            Screen.Finances,
            Screen.Health,
            Screen.Relationships,
            Screen.Education,
            Screen.Rules,
            Screen.Vaults,
            Screen.Capacity,
            Screen.Identity,
        )

    @Test
    fun graphScreen_doesExistInScreenList() {
        val routes = allScreens.map { it.route }
        assertTrue(routes.contains("graph"), "Graph route should exist")
    }

    @Test
    fun dashboard_hasCorrectRoute() {
        assertEquals("dashboard", Screen.Dashboard.route)
    }

    @Test
    fun archive_hasCorrectRoute() {
        assertEquals("archive", Screen.Archive.route)
    }

    @Test
    fun settings_hasCorrectRoute() {
        assertEquals("settings", Screen.Settings.route)
    }

    @Test
    fun insights_hasCorrectRoute() {
        assertEquals("insights", Screen.Insights.route)
    }

    @Test
    fun noteDetail_hasCorrectRoute() {
        assertEquals("note/{noteId}", Screen.NoteDetail.route)
    }

    @Test
    fun projectDetail_hasCorrectRoute() {
        assertEquals("project/{projectId}", Screen.ProjectDetail.route)
    }

    @Test
    fun areaDetail_hasCorrectRoute() {
        assertEquals("area/{areaId}", Screen.AreaDetail.route)
    }

    @Test
    fun today_hasCorrectRoute() {
        assertEquals("today", Screen.Today.route)
    }

    @Test
    fun tasks_hasCorrectRoute() {
        assertEquals("tasks", Screen.Tasks.route)
    }

    @Test
    fun focus_hasCorrectRoute() {
        assertEquals("focus", Screen.Focus.route)
    }

    @Test
    fun track_hasCorrectRoute() {
        assertEquals("track", Screen.Track.route)
    }

    @Test
    fun projects_hasCorrectRoute() {
        assertEquals("projects", Screen.Projects.route)
    }

    @Test
    fun areas_hasCorrectRoute() {
        assertEquals("areas", Screen.Areas.route)
    }

    @Test
    fun finances_hasCorrectRoute() {
        assertEquals("finances", Screen.Finances.route)
    }

    @Test
    fun openLoops_hasCorrectRoute() {
        assertEquals("open_loops", Screen.OpenLoops.route)
    }

    @Test
    fun protocols_hasCorrectRoute() {
        assertEquals("protocols", Screen.Protocols.route)
    }

    @Test
    fun timeArchitecture_hasCorrectRoute() {
        assertEquals("time_architecture", Screen.TimeArchitecture.route)
    }

    @Test
    fun relationships_hasCorrectRoute() {
        assertEquals("relationships", Screen.Relationships.route)
    }

    @Test
    fun health_hasCorrectRoute() {
        assertEquals("health", Screen.Health.route)
    }

    @Test
    fun education_hasCorrectRoute() {
        assertEquals("education", Screen.Education.route)
    }

    @Test
    fun studyLegacy_mapsToEducation() {
        assertEquals(Screen.Education, Screen.fromRoute("study"))
    }

    @Test
    fun allRoutesMustBeUnique() {
        val routes = allScreens.map { it.route }
        val uniqueRoutes = routes.toSet()
        assertEquals(routes.size, uniqueRoutes.size, "All screen routes must be unique")
    }
}
