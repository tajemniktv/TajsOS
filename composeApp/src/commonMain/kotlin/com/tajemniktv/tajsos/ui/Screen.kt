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
import com.tajemniktv.tajsos.ui.domain.DomainRegistry
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

    data object TaskDetail : Screen(
        "task/{taskId}",
        Res.string.type_task,
        Icons.AutoMirrored.Filled.List,
        isRoot = false,
    )

    data object RecordDetail : Screen(
        "record/{recordId}",
        Res.string.type_record,
        Icons.Default.Description,
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

    data object SettingsHealth : Screen(
        "settings_health",
        Res.string.screen_settings_health,
        Icons.Default.Favorite,
        isRoot = false,
    )

    data object SettingsAppearance : Screen(
        "settings_appearance",
        Res.string.screen_settings_appearance,
        Icons.Default.Palette,
        isRoot = false,
    )

    data object SettingsFeaturePacks : Screen(
        "settings_feature_packs",
        Res.string.screen_settings_feature_packs,
        Icons.Default.Extension,
        isRoot = false,
    )

    data object SettingsData : Screen(
        "settings_data",
        Res.string.screen_settings_data,
        Icons.Default.Storage,
        isRoot = false,
    )

    data object SettingsDebug : Screen(
        "settings_debug",
        Res.string.screen_settings_debug,
        Icons.Default.BugReport,
        isRoot = false,
    )

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
        isRoot = false,
    )

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

    /**
     * Legacy deep-link compatibility route.
     * Any "study" route is normalized to [Education] in [fromRoute].
     */
    data object StudyLegacy : Screen(
        "study",
        Res.string.screen_education,
        Icons.Default.School,
        isRoot = false,
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
            if (currentRouteBase == StudyLegacy.route) return Education
            return listOf(
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
                        Calendar,
                    ),
                Res.string.nav_systems to listOf(Projects, Areas, Protocols, TimeArchitecture, Places),
                Res.string.nav_brain to listOf(Notes, Vaults, Rules),
                Res.string.nav_status to listOf(Track, Insights, Capacity, Identity, Graph, Review),
                Res.string.nav_system to listOf(*DomainRegistry.screens.toTypedArray(), Archive, Settings),
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

        /**
         * Returns a context-aware subset of sidebar groups for the currently visible screen.
         * The first group is the screen's own group, followed by a lightweight core shortcut group.
         */
        fun contextualItemsFor(
            currentScreen: Screen,
            packRegistry: PackRegistry,
        ): List<Pair<StringResource, List<Screen>>> {
            val visibleGroups = groupedItemsForPacks(packRegistry)
            val rootScreen = sidebarContextRoot(currentScreen)

            val activeGroup = visibleGroups.find { (_, items) -> rootScreen in items }
            val coreGroup = visibleGroups.find { (header, _) -> header == Res.string.nav_core }

            return buildList {
                if (activeGroup != null) add(activeGroup)
                if (coreGroup != null && coreGroup != activeGroup) add(coreGroup)
            }
        }

        /**
         * Returns the contextual header label for the active sidebar context.
         */
        fun contextualHeaderFor(
            currentScreen: Screen,
            packRegistry: PackRegistry,
        ): StringResource? {
            val rootScreen = sidebarContextRoot(currentScreen)
            return groupedItemsForPacks(packRegistry)
                .find { (_, items) -> rootScreen in items }
                ?.first
        }

        fun sidebarContextRoot(screen: Screen): Screen =
            when (screen)
            {
                NoteDetail -> Notes
                TaskDetail -> Tasks
                RecordDetail -> Notes
                ProjectDetail -> Projects
                AreaDetail -> Areas
                CalendarSettings -> Calendar
                Templates -> Settings
                Profile -> Settings
                SettingsHealth -> Settings
                SettingsAppearance -> Settings
                SettingsFeaturePacks -> Settings
                SettingsData -> Settings
                SettingsDebug -> Settings
                else -> screen
            }
    }
}
