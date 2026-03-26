/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.tajemniktv.tajsos.data.AppPack
import com.tajemniktv.tajsos.data.PackRegistry
import org.jetbrains.compose.resources.StringResource
import tajsos.composeapp.generated.resources.*

/**
 * Screen defines the navigation graph of the app.
 * @
 * @
 * @
 * @
 */
sealed class Screen(
    val route: String,
    val label: StringResource,
    val icon: ImageVector,
    val isRoot: Boolean = true,
) {
    data object Dashboard :
        Screen("dashboard", Res.string.screen_dash, Icons.Default.Home)

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
        isRoot = false,
    )

    data object Insights : Screen("insights", Res.string.screen_stats, Icons.Default.Info)

    data object Archive : Screen("archive", Res.string.screen_archive, Icons.Default.Delete)

    data object Calendar : Screen("calendar", Res.string.screen_cal, Icons.Default.Event)

    data object CalendarSettings :
        Screen(
            "calendar_settings",
            Res.string.screen_cal_opts,
            Icons.Default.Settings,
            isRoot = false,
        )

    data object Graph : Screen("graph", Res.string.screen_graph, Icons.Default.Share)

    data object Projects :
        Screen("projects", Res.string.screen_proj, Icons.AutoMirrored.Filled.List)

    data object Areas : Screen("areas", Res.string.screen_area, Icons.Default.LocationOn)

    data object ProjectDetail : Screen(
        "project/{projectId}",
        Res.string.screen_project,
        Icons.AutoMirrored.Filled.List,
        isRoot = false,
    )

    data object AreaDetail :
        Screen(
            "area/{areaId}",
            Res.string.screen_area,
            Icons.Default.LocationOn,
            isRoot = false,
        )

    data object Settings : Screen("settings", Res.string.screen_opts, Icons.Default.Settings)

    data object Templates : Screen(
        "templates",
        Res.string.screen_templates,
        Icons.Default.Settings,
        isRoot = false,
    )

    data object Review : Screen(
        "review",
        Res.string.screen_review,
        Icons.Default.RateReview,
    )

    data object Profile : Screen(
        "profile",
        Res.string.profile_title,
        Icons.Default.Person,
    )

    data object Decisions : Screen(
        "decisions",
        Res.string.dash_decisions,
        Icons.Default.QuestionMark,
    )

    data object Operations : Screen(
        "operations",
        Res.string.screen_ops,
        Icons.Default.Tune,
        isRoot = false,
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

    data object Relationships : Screen(
        "relationships",
        Res.string.screen_relationships,
        Icons.Default.People,
    )

    data object Study : Screen(
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

    data object StudentBoard : Screen(
        "student_board",
        Res.string.screen_student,
        Icons.Default.School,
        isRoot = false,
    )

    companion object {
        /**
         * Finds the Screen corresponding to the base segment of a navigation route.
         *
         * @param route The navigation route string, which may include path segments and query parameters (for example, "note/123?edit=true"); may be null.
         * @return The matching `Screen` whose route's base segment equals the provided route's base segment, or `null` if `route` is null or no match exists.
         */
        fun fromRoute(route: String?): Screen? {
            if (route == null) return null
            val currentRouteBase =
                route
                    .split("/")
                    .first()
                    .split("?")
                    .first()
            return listOf(
                NoteDetail,
                ProjectDetail,
                AreaDetail,
                CalendarSettings,
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
                Operations,
                OpenLoops,
                Protocols,
                TimeArchitecture,
                Places,
                Finances,
                Relationships,
                Study,
                Rules,
                Vaults,
                Capacity,
                Identity,
                StudentBoard,
            ).find { it.route.split("/").first() == currentRouteBase }
        }

        val groupedItems by lazy {
            listOf(
                Res.string.nav_core to listOf(Dashboard, Inbox, Search),
                Res.string.nav_execution to
                    listOf(
                        Today,
                        Tasks,
                        Focus,
                        Decisions,
                        OpenLoops,
                        Finances,
                        Relationships,
                        Study,
                        Calendar,
                    ),
                Res.string.nav_systems to listOf(Protocols, TimeArchitecture, Places),
                Res.string.nav_brain to listOf(Notes, Projects, Areas, Vaults, Rules),
                Res.string.nav_status to listOf(Track, Insights, Capacity, Identity, Graph, Review),
                Res.string.nav_system to listOf(Archive, Settings, Profile),
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

                                StudentBoard -> {
                                    packRegistry.isEnabled(AppPack.STUDENT)
                                }

                                Study -> {
                                    packRegistry.isEnabled(AppPack.STUDENT)
                                }

                                OpenLoops,
                                Protocols,
                                TimeArchitecture,
                                Places,
                                Finances,
                                Relationships,
                                Rules,
                                Vaults,
                                Capacity,
                                Identity,
                                Calendar,
                                -> {
                                    true
                                }

                                Operations -> {
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
    }
}
