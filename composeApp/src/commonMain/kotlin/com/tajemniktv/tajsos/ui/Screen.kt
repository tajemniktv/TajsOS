/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.ui.graphics.vector.ImageVector
import com.tajemniktv.tajsos.data.AppPack
import com.tajemniktv.tajsos.data.PackRegistry
import com.tajemniktv.tajsos.ui.domain.DomainRegistry
import com.tajemniktv.tajsos.ui.screens.tasks.TasksTab
import org.jetbrains.compose.resources.StringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.dash_decisions
import tajsos.composeapp.generated.resources.nav_brain
import tajsos.composeapp.generated.resources.nav_core
import tajsos.composeapp.generated.resources.nav_execution
import tajsos.composeapp.generated.resources.nav_status
import tajsos.composeapp.generated.resources.nav_system
import tajsos.composeapp.generated.resources.nav_systems
import tajsos.composeapp.generated.resources.profile_title
import tajsos.composeapp.generated.resources.screen_archive
import tajsos.composeapp.generated.resources.screen_area
import tajsos.composeapp.generated.resources.screen_briefing
import tajsos.composeapp.generated.resources.screen_cal
import tajsos.composeapp.generated.resources.screen_cal_opts
import tajsos.composeapp.generated.resources.screen_capacity
import tajsos.composeapp.generated.resources.screen_dash
import tajsos.composeapp.generated.resources.screen_education
import tajsos.composeapp.generated.resources.screen_finances
import tajsos.composeapp.generated.resources.screen_focus
import tajsos.composeapp.generated.resources.screen_graph
import tajsos.composeapp.generated.resources.screen_health
import tajsos.composeapp.generated.resources.screen_identity
import tajsos.composeapp.generated.resources.screen_inbox
import tajsos.composeapp.generated.resources.screen_note
import tajsos.composeapp.generated.resources.screen_notes
import tajsos.composeapp.generated.resources.screen_open_loops
import tajsos.composeapp.generated.resources.screen_opts
import tajsos.composeapp.generated.resources.screen_places
import tajsos.composeapp.generated.resources.screen_proj
import tajsos.composeapp.generated.resources.screen_project
import tajsos.composeapp.generated.resources.screen_protocols
import tajsos.composeapp.generated.resources.screen_relationships
import tajsos.composeapp.generated.resources.screen_review
import tajsos.composeapp.generated.resources.screen_rules
import tajsos.composeapp.generated.resources.screen_search
import tajsos.composeapp.generated.resources.screen_settings_appearance
import tajsos.composeapp.generated.resources.screen_settings_data
import tajsos.composeapp.generated.resources.screen_settings_debug
import tajsos.composeapp.generated.resources.screen_settings_feature_packs
import tajsos.composeapp.generated.resources.screen_settings_health
import tajsos.composeapp.generated.resources.screen_stats
import tajsos.composeapp.generated.resources.screen_study
import tajsos.composeapp.generated.resources.screen_tasks
import tajsos.composeapp.generated.resources.screen_templates
import tajsos.composeapp.generated.resources.screen_time_architecture
import tajsos.composeapp.generated.resources.screen_today
import tajsos.composeapp.generated.resources.screen_track
import tajsos.composeapp.generated.resources.screen_vaults
import tajsos.composeapp.generated.resources.settings_tab_preferences
import tajsos.composeapp.generated.resources.type_record
import tajsos.composeapp.generated.resources.type_task

/**
 * Screen defines the navigation graph of the app.
 */
sealed class Screen(
    val route: String,
    val label: StringResource,
    val icon: ImageVector,
    val isRoot: Boolean = true,
) {
    open val breadcrumbParent: Screen? get() = null

    /**
     * Returns the list of child screens for this root screen.
     */
    open val children: List<Screen> get() = emptyList()

    data object Briefing :
        Screen("briefing", Res.string.screen_briefing, Icons.Default.Description)

    data object Dashboard :
        Screen("dashboard", Res.string.screen_dash, Icons.Default.Home)

    data object Inbox : Screen("inbox", Res.string.screen_inbox, Icons.Default.Email)

    data object Search : Screen("search", Res.string.screen_search, Icons.Default.Search)

    data object Today : Screen("today", Res.string.screen_today, Icons.Default.DateRange)

    data object Focus : Screen("focus", Res.string.screen_focus, Icons.Default.PlayArrow)

    data object Track : Screen("track", Res.string.screen_track, Icons.Default.CheckCircle)

    data object Tasks : Screen("tasks", Res.string.screen_tasks, Icons.Default.Checklist) {
        override val children: List<Screen>
            get() = TasksTab.entries.map { it.toScreen() }
    }

    data object Notes : Screen("notes", Res.string.screen_notes, Icons.AutoMirrored.Filled.Notes)

    data object NoteDetail : Screen(
        "note/{noteId}",
        Res.string.screen_note,
        Icons.Default.Edit,
        isRoot = false,
    ) {
        override val breadcrumbParent: Screen = Notes
    }

    data object TaskDetail : Screen(
        "task/{taskId}",
        Res.string.type_task,
        Icons.AutoMirrored.Filled.List,
        isRoot = false,
    ) {
        override val breadcrumbParent: Screen = Tasks
    }

    data object RecordDetail : Screen(
        "record/{recordId}",
        Res.string.type_record,
        Icons.Default.Description,
        isRoot = false,
    ) {
        override val breadcrumbParent: Screen = Notes
    }

    data object Insights : Screen("insights", Res.string.screen_stats, Icons.Default.Info)

    data object Archive : Screen("archive", Res.string.screen_archive, Icons.Default.Delete)

    data object Calendar : Screen("calendar", Res.string.screen_cal, Icons.Default.Event)

    data object CalendarSettings :
        Screen(
            "calendar_settings",
            Res.string.screen_cal_opts,
            Icons.Default.Settings,
            isRoot = false,
        ) {
            override val breadcrumbParent: Screen = Settings
        }

    data object Graph : Screen("graph", Res.string.screen_graph, Icons.Default.Share)

    data object Projects :
        Screen("projects", Res.string.screen_proj, Icons.AutoMirrored.Filled.List)

    data object Areas : Screen("areas", Res.string.screen_area, Icons.Default.LocationOn)

    data object ProjectDetail : Screen(
        "project/{projectId}",
        Res.string.screen_project,
        Icons.AutoMirrored.Filled.List,
        isRoot = false,
    ) {
        override val breadcrumbParent: Screen = Projects
    }

    data object AreaDetail :
        Screen(
            "area/{areaId}",
            Res.string.screen_area,
            Icons.Default.LocationOn,
            isRoot = false,
        ) {
            override val breadcrumbParent: Screen = Areas
        }

    data object Settings : Screen("settings", Res.string.screen_opts, Icons.Default.Settings) {
        override val children: List<Screen>
            get() =
                listOf(
                    Sub(
                        Settings,
                        "preferences",
                        Res.string.settings_tab_preferences,
                        Icons.Default.Settings,
                    ),
                    Profile,
                    SettingsAppearance,
                    Sub(
                        Settings,
                        "calendar",
                        Res.string.screen_cal_opts,
                        Icons.Default.Event,
                        "view",
                    ),
                    SettingsFeaturePacks,
                    SettingsHealth,
                    SettingsData,
                    SettingsDebug,
                )
    }

    data object SettingsHealth : Screen(
        "settings_health",
        Res.string.screen_settings_health,
        Icons.Default.Favorite,
        isRoot = false,
    ) {
        override val breadcrumbParent: Screen = Settings
    }

    data object SettingsAppearance : Screen(
        "settings_appearance",
        Res.string.screen_settings_appearance,
        Icons.Default.Palette,
        isRoot = false,
    ) {
        override val breadcrumbParent: Screen = Settings
    }

    data object SettingsFeaturePacks : Screen(
        "settings_feature_packs",
        Res.string.screen_settings_feature_packs,
        Icons.Default.Extension,
        isRoot = false,
    ) {
        override val breadcrumbParent: Screen = Settings
    }

    data object SettingsData : Screen(
        "settings_data",
        Res.string.screen_settings_data,
        Icons.Default.Storage,
        isRoot = false,
    ) {
        override val breadcrumbParent: Screen = Settings
    }

    data object SettingsDebug : Screen(
        "settings_debug",
        Res.string.screen_settings_debug,
        Icons.Default.BugReport,
        isRoot = false,
    ) {
        override val breadcrumbParent: Screen = Settings
    }

    data object Templates : Screen(
        "templates",
        Res.string.screen_templates,
        Icons.AutoMirrored.Filled.List,
        isRoot = false,
    ) {
        override val breadcrumbParent: Screen = Settings
    }

    data object Review : Screen(
        "review",
        Res.string.screen_review,
        Icons.Default.RateReview,
        isRoot = false,
    )

    data object Profile : Screen(
        "profile",
        Res.string.profile_title,
        Icons.Default.Person,
        isRoot = false,
    ) {
        override val breadcrumbParent: Screen = Settings
    }

    data object Decisions : Screen(
        "decisions",
        Res.string.dash_decisions,
        Icons.Default.QuestionMark,
    )

    data object OpenLoops : Screen(
        "open_loops",
        Res.string.screen_open_loops,
        Icons.Default.AllInclusive,
    )

    data object Protocols : Screen(
        "protocols",
        Res.string.screen_protocols,
        Icons.Default.RocketLaunch,
    )

    data object TimeArchitecture : Screen(
        "time_architecture",
        Res.string.screen_time_architecture,
        Icons.Default.Schedule,
    )

    data object Places : Screen(
        "places",
        Res.string.screen_places,
        Icons.Default.Place,
    )

    data object Finances : Screen(
        "finances",
        Res.string.screen_finances,
        Icons.Default.AttachMoney,
    )

    data object Health : Screen(
        "health",
        Res.string.screen_health,
        Icons.Default.Favorite,
    )

    data object Relationships : Screen(
        "relationships",
        Res.string.screen_relationships,
        Icons.Default.People,
    )

    data object Education : Screen(
        "education",
        Res.string.screen_education,
        Icons.Default.School,
    )

    data object StudyLegacy : Screen(
        "study",
        Res.string.screen_study,
        Icons.Default.School,
    )

    data object Rules : Screen(
        "rules",
        Res.string.screen_rules,
        Icons.Default.Gavel,
    )

    data object Vaults : Screen(
        "vaults",
        Res.string.screen_vaults,
        Icons.Default.Inventory2,
    )

    data object Capacity : Screen(
        "capacity",
        Res.string.screen_capacity,
        Icons.Default.Speed,
    )

    data object Identity : Screen(
        "identity",
        Res.string.screen_identity,
        Icons.Default.Psychology,
    )

    /**
     * Generic sub-screen for any root screen with nested content/tabs.
     *
     * @param parent The parent root screen (e.g., Screen.Tasks).
     * @param subRoute The unique sub-route segment (e.g., "inbox").
     * @param label Resource for the sub-screen's display label.
     * @param icon Icon representing the sub-screen.
     * @param paramName The name of the query parameter used for sub-navigation.
     */
    class Sub(
        val parent: Screen,
        subRoute: String,
        label: StringResource,
        icon: ImageVector,
        val paramName: String = "tab",
    ) : Screen("${parent.route}?$paramName=$subRoute", label, icon, isRoot = false) {
        override val breadcrumbParent: Screen = parent
    }

    fun breadcrumbTrail(): List<Screen> = breadcrumbParent?.breadcrumbTrail().orEmpty() + this

    companion object {
        /**
         * Resolve a navigation route string to its corresponding Screen.
         *
         * The function first checks for an exact screen route match (including query parameters), then attempts to match
         * tab-style `Sub` routes derived from query parameters, applies a special-case mapping of the legacy study route
         * to `Education`, and finally falls back to matching the first root screen whose base path segment equals the
         * route's base segment.
         *
         * @param route The navigation route string, which may include path segments and query parameters (for example, "note/123?edit=true"); may be null.
         * @return The matching `Screen`, or `null` if `route` is null or no match exists.
         */
        fun fromRoute(route: String?): Screen? {
            if (route == null) return null
            val currentRouteBase =
                route
                    .split("/")
                    .first()
                    .split("?")
                    .first()
            if (currentRouteBase == StudyLegacy.route) return Education

            val rootScreens =
                listOf(
                    NoteDetail,
                    TaskDetail,
                    RecordDetail,
                    ProjectDetail,
                    AreaDetail,
                    CalendarSettings,
                    SettingsHealth,
                    SettingsAppearance,
                    SettingsFeaturePacks,
                    SettingsData,
                    SettingsDebug,
                    Briefing,
                    Dashboard,
                    Inbox,
                    Search,
                    Today,
                    Focus,
                    Track,
                    Tasks,
                    Notes,
                    Insights,
                    Archive,
                    Calendar,
                    Graph,
                    Projects,
                    Areas,
                    Settings,
                    Templates,
                    Review,
                    Profile,
                    Decisions,
                    OpenLoops,
                    Protocols,
                    TimeArchitecture,
                    Places,
                    Finances,
                    Health,
                    Relationships,
                    Education,
                    Rules,
                    Vaults,
                    Capacity,
                    Identity,
                )

            val allScreens = rootScreens + rootScreens.flatMap { it.children }

            // Try exact match first (for Sub screens with query params)
            allScreens.find { it.route == route }?.let { return it }

            // Ensure exact matches for Sub routes with query params are prioritized correctly
            // if route string was partially modified.
            if (route.contains("?")) {
                val queryBase = route.substringBefore("?")
                val sub =
                    allScreens.filterIsInstance<Sub>().find {
                        it.route == route ||
                            it.route == "$queryBase?tab=${route.substringAfter("=")}"
                    }
                if (sub != null) return sub
            }

            // Try base route match, ensuring we don't accidentally match sub-screens that share a base
            // unless they are explicitly the root screen.
            return rootScreens.find { it.route.split("/").first() == currentRouteBase }
        }

        val groupedItems by lazy {
            listOf(
                Res.string.nav_core to listOf(Briefing, Dashboard, Inbox, Search),
                Res.string.nav_execution to
                    listOf(
                        Today,
                        Tasks,
                        Focus,
                        Decisions,
                        OpenLoops,
                        Calendar,
                    ),
                Res.string.nav_systems to
                    listOf(
                        Projects,
                        Areas,
                        Protocols,
                        TimeArchitecture,
                        Places,
                    ),
                Res.string.nav_brain to listOf(Notes, Vaults, Rules),
                Res.string.nav_status to listOf(Track, Insights, Capacity, Identity, Graph, Review),
                Res.string.nav_system to
                    listOf(
                        *DomainRegistry.screens.toTypedArray(),
                        Archive,
                        Settings,
                    ),
            )
        }

        fun groupedItemsForPacks(packRegistry: PackRegistry): List<Pair<StringResource, List<Screen>>> {
            val visible =
                groupedItems.map { (group, screens) ->
                    group to
                        screens.filter { screen ->
                            when (screen)
                            {
                                Graph -> {
                                    packRegistry.isEnabled(AppPack.CREATOR) ||
                                        packRegistry.isEnabled(
                                            AppPack.STUDENT,
                                        )
                                }

                                Education -> {
                                    packRegistry.isEnabled(AppPack.STUDENT)
                                }

                                OpenLoops,
                                Protocols,
                                TimeArchitecture,
                                Places,
                                Finances,
                                Health,
                                Relationships,
                                Rules,
                                Vaults,
                                Capacity,
                                Identity,
                                Calendar,
                                -> {
                                    true
                                }

                                else -> {
                                    true
                                }
                            }
                        }
                }
            return visible.filter { (_, screens) -> screens.isNotEmpty() }
        }

        fun sidebarContextRoot(screen: Screen): Screen =
            when (screen)
            {
                NoteDetail -> Notes
                TaskDetail -> Tasks
                RecordDetail -> Notes
                ProjectDetail -> Projects
                AreaDetail -> Areas
                CalendarSettings -> Settings
                Templates -> Settings
                Profile -> Settings
                SettingsHealth -> Settings
                SettingsAppearance -> Settings
                SettingsFeaturePacks -> Settings
                SettingsData -> Settings
                SettingsDebug -> Settings
                is Sub -> screen.parent
                else -> screen
            }
    }
}
