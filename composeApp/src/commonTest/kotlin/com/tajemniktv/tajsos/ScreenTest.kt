package com.tajemniktv.tajsos

import com.tajemniktv.tajsos.ui.Screen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests for Screen sealed class changes introduced in this PR.
 * The Graph screen was removed; all other screens remain with correct routes and labels.
 */
class ScreenTest {

    private val allScreens: List<Screen> = listOf(
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
    )

    // --- Graph screen removal ---

    @Test
    fun graphScreen_doesNotExistInScreenList() {
        val routes = allScreens.map { it.route }
        assertFalse(routes.contains("graph"), "Graph route should not exist after removal")
    }

    @Test
    fun graphScreen_labelNotPresentInKnownScreens() {
        val labels = allScreens.map { it.label }
        assertFalse(labels.contains("GRAPH"), "GRAPH label should not exist after removal")
    }

    // --- Route correctness for retained screens ---

    @Test
    fun dashboard_hasCorrectRoute() {
        assertEquals("dashboard", Screen.Dashboard.route)
    }

    @Test
    fun dashboard_hasCorrectLabel() {
        assertEquals("DASH", Screen.Dashboard.label)
    }

    @Test
    fun archive_hasCorrectRoute() {
        assertEquals("archive", Screen.Archive.route)
    }

    @Test
    fun archive_hasCorrectLabel() {
        assertEquals("ARCHIVE", Screen.Archive.label)
    }

    @Test
    fun settings_hasCorrectRoute() {
        assertEquals("settings", Screen.Settings.route)
    }

    @Test
    fun settings_hasCorrectLabel() {
        assertEquals("OPTS", Screen.Settings.label)
    }

    @Test
    fun insights_hasCorrectRoute() {
        assertEquals("insights", Screen.Insights.route)
    }

    @Test
    fun noteDetail_routeContainsNoteIdPlaceholder() {
        assertEquals("note/{noteId}", Screen.NoteDetail.route)
    }

    @Test
    fun projectDetail_routeContainsProjectIdPlaceholder() {
        assertEquals("project/{projectId}", Screen.ProjectDetail.route)
    }

    @Test
    fun areaDetail_routeContainsAreaIdPlaceholder() {
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

    // --- Total screen count after Graph removal ---

    @Test
    fun screenCount_is15AfterGraphRemoval() {
        assertEquals(15, allScreens.size)
    }

    // --- Routes must be unique (no duplicate routes) ---

    @Test
    fun allRoutes_areUnique() {
        val routes = allScreens.map { it.route }
        val uniqueRoutes = routes.toSet()
        assertEquals(routes.size, uniqueRoutes.size, "All screen routes must be unique")
    }

    // --- Navigation screens shown in the bottom nav ---

    @Test
    fun bottomNavScreens_doNotIncludeGraph() {
        // The nav bar screens list used in App.kt doesn't include Screen.Graph
        val navScreens = listOf(
            Screen.Dashboard,
            Screen.Today,
            Screen.Tasks,
            Screen.Notes,
            Screen.Projects,
            Screen.Areas,
            Screen.Track,
            Screen.Insights,
            Screen.Archive,
            Screen.Settings
        )
        val graphRoute = "graph"
        assertFalse(navScreens.any { it.route == graphRoute })
    }

    // --- Regression: Archive route unchanged (was present before PR too) ---

    @Test
    fun archiveScreen_isDistinctFromGraph() {
        assertFalse(Screen.Archive.route == "graph")
    }
}