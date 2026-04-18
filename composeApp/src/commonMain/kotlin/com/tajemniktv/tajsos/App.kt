/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tajemniktv.tajsos.data.ItemKind
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.StableList
import com.tajemniktv.tajsos.data.TemplateEntity
import com.tajemniktv.tajsos.data.toStableList
import com.tajemniktv.tajsos.ui.DetailNavigationContract
import com.tajemniktv.tajsos.ui.LocalMainViewModel
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.common.CaptureSheet
import com.tajemniktv.tajsos.ui.components.common.mouseButtons
import com.tajemniktv.tajsos.ui.components.layout.AppShell
import com.tajemniktv.tajsos.ui.components.layout.rememberAppShellState
import com.tajemniktv.tajsos.ui.components.screen.LocalScreenHeaderController
import com.tajemniktv.tajsos.ui.components.screen.rememberScreenHeaderController
import com.tajemniktv.tajsos.ui.screens.calendar.CalendarSettingsRoute
import com.tajemniktv.tajsos.ui.screens.notes.detail.NoteDetailScreen
import com.tajemniktv.tajsos.ui.screens.tasks.TasksTab
import com.tajemniktv.tajsos.ui.screens.templates.TemplatesScreen
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.nav_capture

private const val CAPTURE_TYPE_INBOX = "inbox"

/**
 * Hosts the application's top-level UI: sets up navigation, collects app state from the ViewModel,
 * manages the capture-sheet and voice-capture lifecycles, and composes the app theme, layout, and
 * navigation graph.
 *
 * @param viewModel The main ViewModel providing app state (projects, areas, templates, modes, tracks, etc.).
 * @param modifier The modifier to be applied to the root layout.
 * @param onVoiceCapture Optional callback invoked to start a voice capture session.
 * @param voiceCaptureResult Optional text result from a completed voice capture to prefill the capture sheet.
 * @param onVoiceCaptureConsume Callback invoked when the voice capture result has been consumed (clears or acknowledges the result).
 * @param onPickAvatar Optional callback used by the profile screen to request a platform avatar picker.
 * @param avatarPickResult Optional selected avatar reference (URI/path) from a platform picker.
 * @param onAvatarPickConsume Callback invoked after the avatar picker result has been consumed by UI state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onVoiceCapture: (() -> Unit)? = null,
    voiceCaptureResult: String? = null,
    onVoiceCaptureConsume: () -> Unit = {},
    onPickAvatar: (() -> Unit)? = null,
    avatarPickResult: String? = null,
    onAvatarPickConsume: () -> Unit = {},
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val allProjects by viewModel.allProjects.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val allTemplates by viewModel.allTemplates.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()
    val lastActiveProjectId by viewModel.lastActiveProjectId.collectAsState()
    val lastActiveAreaId by viewModel.lastActiveAreaId.collectAsState()

    val currentMode by viewModel.currentMode.collectAsState()
    val allModes by viewModel.allModes.collectAsState()
    val enabledPacks by viewModel.enabledPacks.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val accentColorHex by viewModel.accentColorHex.collectAsState()
    val isGlassmorphismEnabled by viewModel.isGlassmorphismEnabled.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val sidebarMode by viewModel.sidebarMode.collectAsState()
    val sidebarExpandedWidthDp by viewModel.sidebarExpandedWidthDp.collectAsState()

    var showCaptureSheetState by remember { mutableStateOf(value = false) }
    var selectedTasksTab by rememberSaveable { mutableStateOf(TasksTab.COMMAND) }
    val shellState = rememberAppShellState(sidebarMode = sidebarMode)
    val screenHeaderController = rememberScreenHeaderController()
    val mouseForwardRoutes = remember { ArrayDeque<String>() }

    fun isForwardNavigable(route: String?): Boolean =
        !route.isNullOrBlank() &&
            !route.contains('{') &&
            route != Screen.Dashboard.route

    val accentColor =
        remember(accentColorHex) {
            try {
                val hex = accentColorHex.removePrefix("#")
                if (hex.length == 6) {
                    androidx.compose.ui.graphics.Color(
                        red = hex.substring(0, 2).toInt(16),
                        green = hex.substring(2, 4).toInt(16),
                        blue = hex.substring(4, 6).toInt(16),
                    )
                } else {
                    TajsOSTheme.Primary
                }
            } catch (_: Exception) {
                TajsOSTheme.Primary
            }
        }

    val stableProjects = remember(allProjects) { allProjects.toStableList() }
    val stableAreas = remember(allAreas) { allAreas.toStableList() }
    val stableNodes = remember(allNodes) { allNodes.toStableList() }
    val stableTemplates = remember(allTemplates) { allTemplates.toStableList() }

    TajsOSTheme(darkTheme = isDarkTheme, accentColor = accentColor) {
        CompositionLocalProvider(LocalMainViewModel provides viewModel) {
            BoxWithConstraints(modifier = modifier) {
                val isDesktop = maxWidth > 800.dp

                // The primary navigation callback for the main content area.
                val navigate: (String) -> Unit =
                    remember(navController, currentDestination, selectedTasksTab) {
                        { route ->
                            // Resolve the route, ensuring Tasks always includes the active tab segment.
                            val resolvedRoute =
                                if (route == Screen.Tasks.route) {
                                    "${Screen.Tasks.route}?${Screen.PARAM_TAB}=${selectedTasksTab.routeSegment}"
                                } else {
                                    route
                                }

                            // If the target is a tasks tab, keep our shell state synchronized.
                            if (resolvedRoute.startsWith("${Screen.Tasks.route}?${Screen.PARAM_TAB}=")) {
                                val tabSegment =
                                    resolvedRoute.substringAfter(
                                        "${Screen.PARAM_TAB}=",
                                        missingDelimiterValue = "",
                                    )
                                selectedTasksTab = TasksTab.fromRouteSegment(tabSegment)
                            }

                            val currentScreen = Screen.fromRoute(currentDestination?.route)
                            val targetScreen = Screen.fromRoute(resolvedRoute)

                            // Navigation logic governed by explicit route classification.
                            if (targetScreen?.isNavigableRoot == true) {
                                mouseForwardRoutes.clear()
                                if (resolvedRoute == Screen.Dashboard.route) {
                                    val popped =
                                        navController.popBackStack(
                                            Screen.Dashboard.route,
                                            inclusive = false,
                                        )
                                    if (!popped && (currentDestination?.route != Screen.Dashboard.route)) {
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                inclusive = true
                                            }
                                            launchSingleTop = true
                                        }
                                    }
                                } else {
                                    val isCurrentNavRoot = currentScreen?.isNavigableRoot == true

                                    // Restore state ONLY if we are switching between root domains (e.g. Tasks to Notes)
                                    // OR if we are already on a root-like screen (to preserve scroll positions when switching tabs).
                                    val shouldRestore =
                                        (currentScreen?.sidebarContextRoot != targetScreen.sidebarContextRoot) ||
                                            isCurrentNavRoot

                                    // Save state ONLY if we are leaving a root-like screen.
                                    // This prevents "stuck" detail screens from being saved and restored via sidebar jumps.
                                    val shouldSave = isCurrentNavRoot

                                    navController.navigate(resolvedRoute) {
                                        popUpTo(Screen.Dashboard.route) {
                                            inclusive = false
                                            saveState = shouldSave
                                        }
                                        restoreState = shouldRestore
                                        launchSingleTop = true
                                    }
                                }
                            } else {
                                mouseForwardRoutes.clear()
                                // Detail and utility screens are pushed onto the current backstack context.
                                navController.navigate(resolvedRoute) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    }

                AppShell(
                    modifier =
                        Modifier.mouseButtons(
                            enabled = !showCaptureSheetState && !shellState.modeDropdownExpanded && !shellState.notificationsExpanded,
                            onBackClick = {
                                val currentRoute = currentDestination?.route
                                val canPop = navController.previousBackStackEntry != null
                                if (!canPop) return@mouseButtons
                                if (isForwardNavigable(currentRoute)) {
                                    mouseForwardRoutes.addLast(currentRoute!!)
                                }
                                navController.popBackStack()
                            },
                            onForwardClick = {
                                val nextRoute = mouseForwardRoutes.removeLastOrNull() ?: return@mouseButtons
                                navController.navigate(nextRoute) { launchSingleTop = true }
                            },
                        ),
                    isDesktop = isDesktop,
                    shellState = shellState,
                    currentDestination = currentDestination,
                    activeTasksTab = selectedTasksTab,
                    onNavigate = { navigate(it.route) },
                    onNavigateToTasksTab = {
                        selectedTasksTab = it
                        navigate("${Screen.Tasks.route}?${Screen.PARAM_TAB}=${it.routeSegment}")
                    },
                    sidebarExpandedWidthDp = sidebarExpandedWidthDp,
                    onSidebarExpandedWidthChange = { viewModel.setSidebarExpandedWidthDp(it) },
                    onNewEntry = { showCaptureSheetState = true },
                    currentMode = currentMode,
                    allModes = allModes,
                    packRegistry = enabledPacks,
                    userProfile = userProfile,
                    isGlassmorphismEnabled = isGlassmorphismEnabled,
                    onModeSelect = { viewModel.switchMode(it) },
                    drawerState = drawerState,
                    scope = scope,
                    screenHeader = screenHeaderController.model,
                    notifications = emptyList(),
                ) {
                    CompositionLocalProvider(LocalScreenHeaderController provides screenHeaderController) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val onEditNode: (Long) -> Unit = {
                                navigate(
                                    DetailNavigationContract.routeForNodeId(
                                        it,
                                        stableNodes.items,
                                    ),
                                )
                            }

                            NavHost(
                                navController = navController,
                                startDestination = Screen.Dashboard.route,
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                composable(Screen.Briefing.route) {
                                    com.tajemniktv.tajsos.ui.screens.briefing.BriefingRoute(
                                        onNavigate = navigate,
                                        onNewEntry = { showCaptureSheetState = true },
                                    )
                                }
                                composable(Screen.Dashboard.route) {
                                    com.tajemniktv.tajsos.ui.screens.dashboard.DashboardRoute(
                                        viewModel = viewModel,
                                        onNavigate = navigate,
                                        onEditNode = onEditNode,
                                        onNavigateToProject = {
                                            navigate(
                                                Screen.ProjectDetail.route.replace(
                                                    "{${Screen.PARAM_PROJECT_ID}}",
                                                    it.toString(),
                                                ),
                                            )
                                        },
                                        onNewEntry = { showCaptureSheetState = true },
                                        currentDestination = currentDestination,
                                    )
                                }
                                composable(Screen.Inbox.route) {
                                    com.tajemniktv.tajsos.ui.screens.inbox.InboxRoute(
                                        viewModel = viewModel,
                                        onEditNode = onEditNode,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Search.route) {
                                    com.tajemniktv.tajsos.ui.screens.search.SearchRoute(
                                        viewModel = viewModel,
                                        onItemClick = onEditNode,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Today.route) {
                                    com.tajemniktv.tajsos.ui.screens.today.TodayRoute(
                                        viewModel = viewModel,
                                        onEditNode = onEditNode,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Focus.route) {
                                    com.tajemniktv.tajsos.ui.screens.focus.FocusRoute(
                                        viewModel = viewModel,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Track.route) {
                                    com.tajemniktv.tajsos.ui.screens.track.TrackRoute(
                                        viewModel = viewModel,
                                        onNavigate = navigate,
                                    )
                                }
                                Screen.Tasks.children.forEach { child ->
                                    composable(child.route) {
                                        val tab =
                                            TasksTab.fromRouteSegment(
                                                child.route.substringAfterLast("="),
                                            )
                                        com.tajemniktv.tajsos.ui.screens.tasks.TasksRoute(
                                            viewModel = viewModel,
                                            onEditNode = onEditNode,
                                            onNavigate = navigate,
                                            currentTab = tab,
                                            onTabChange = {
                                                selectedTasksTab = it
                                                navigate("${Screen.Tasks.route}?${Screen.PARAM_TAB}=${it.routeSegment}")
                                            },
                                        )
                                    }
                                }
                                composable(Screen.Tasks.route) {
                                    com.tajemniktv.tajsos.ui.screens.tasks.TasksRoute(
                                        viewModel = viewModel,
                                        onEditNode = onEditNode,
                                        onNavigate = navigate,
                                        currentTab = selectedTasksTab,
                                        onTabChange = {
                                            selectedTasksTab = it
                                            navigate("${Screen.Tasks.route}?${Screen.PARAM_TAB}=${it.routeSegment}")
                                        },
                                    )
                                }
                                composable(Screen.Notes.route) {
                                    com.tajemniktv.tajsos.ui.screens.notes.NotesRoute(
                                        viewModel = viewModel,
                                        onNavigateToNode = onEditNode,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Notes.route + "?${Screen.PARAM_NOTE_ID}={${Screen.PARAM_NOTE_ID}}") {
                                    val noteId =
                                        it.savedStateHandle
                                            .get<Any>(Screen.PARAM_NOTE_ID)
                                            ?.toString()
                                            ?.toLongOrNull()
                                    com.tajemniktv.tajsos.ui.screens.notes.NotesRoute(
                                        viewModel = viewModel,
                                        onNavigateToNode = onEditNode,
                                        onNavigate = navigate,
                                        initialSelectedNoteId = noteId,
                                    )
                                }
                                composable(Screen.Calendar.route) {
                                    com.tajemniktv.tajsos.ui.screens.calendar.CalendarRoute(
                                        viewModel = viewModel,
                                        onEditNode = onEditNode,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Decisions.route) {
                                    com.tajemniktv.tajsos.ui.screens.decisions.DecisionsRoute(
                                        viewModel = viewModel,
                                        onEditNode = onEditNode,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.OpenLoops.route) {
                                    com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsRoute(
                                        viewModel = viewModel,
                                        onEditNode = onEditNode,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Protocols.route) {
                                    com.tajemniktv.tajsos.ui.screens.protocols.ProtocolsRoute(
                                        viewModel = viewModel,
                                        onEditNode = onEditNode,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.TimeArchitecture.route) {
                                    com.tajemniktv.tajsos.ui.screens.timearchitecture.TimeArchitectureRoute(
                                        viewModel = viewModel,
                                        onEditNode = onEditNode,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Places.route) {
                                    com.tajemniktv.tajsos.ui.screens.places.PlacesRoute(
                                        viewModel = viewModel,
                                        onEditNode = onEditNode,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Finances.route) {
                                    com.tajemniktv.tajsos.ui.screens.finance.FinancesRoute(
                                        viewModel = viewModel,
                                        onEditNode = onEditNode,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Health.route) {
                                    com.tajemniktv.tajsos.ui.screens.health.HealthRoute(
                                        viewModel = viewModel,
                                        onEditNode = onEditNode,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Relationships.route) {
                                    com.tajemniktv.tajsos.ui.screens.relationships.RelationshipsRoute(
                                        viewModel = viewModel,
                                        onEditNode = onEditNode,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Education.route) {
                                    com.tajemniktv.tajsos.ui.screens.study.StudyRoute(
                                        viewModel = viewModel,
                                        onEditNode = onEditNode,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.StudyLegacy.route) {
                                    com.tajemniktv.tajsos.ui.screens.study.StudyRoute(
                                        viewModel = viewModel,
                                        onEditNode = onEditNode,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Rules.route) {
                                    com.tajemniktv.tajsos.ui.screens.rules.RulesRoute(
                                        viewModel = viewModel,
                                        onEditNode = onEditNode,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Vaults.route) {
                                    com.tajemniktv.tajsos.ui.screens.vaults.VaultsRoute(
                                        viewModel = viewModel,
                                        onEditNode = onEditNode,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Capacity.route) {
                                    com.tajemniktv.tajsos.ui.screens.capacity.CapacityRoute(
                                        viewModel = viewModel,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Identity.route) {
                                    com.tajemniktv.tajsos.ui.screens.identity.IdentityRoute(
                                        viewModel = viewModel,
                                        onEditNode = onEditNode,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Templates.route) {
                                    TemplatesScreen(viewModel) { navController.popBackStack() }
                                }
                                composable(Screen.Settings.route) {
                                    com.tajemniktv.tajsos.ui.screens.settings.SettingsRoute(
                                        viewModel = viewModel,
                                        onNavigate = navigate,
                                    )
                                }
                                val settingsPref =
                                    Screen.Settings.children.first {
                                        (it is Screen.Sub) &&
                                            it.route.contains(Screen.Settings.SUB_PREFERENCES)
                                    }
                                composable(settingsPref.route) {
                                    com.tajemniktv.tajsos.ui.screens.settings.SettingsRoute(
                                        viewModel = viewModel,
                                        onNavigate = navigate,
                                    )
                                }
                                val settingsCal =
                                    Screen.Settings.children.first {
                                        (it is Screen.Sub) &&
                                            it.route.contains(Screen.Settings.SUB_CALENDAR)
                                    }
                                composable(settingsCal.route) {
                                    CalendarSettingsRoute(
                                        viewModel = viewModel,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.SettingsHealth.route) {
                                    com.tajemniktv.tajsos.ui.screens.settings.SettingsRoute(
                                        viewModel = viewModel,
                                        onNavigate = navigate,
                                        screenId = Screen.SettingsHealth.ID,
                                    )
                                }
                                composable(Screen.SettingsAppearance.route) {
                                    com.tajemniktv.tajsos.ui.screens.settings.SettingsRoute(
                                        viewModel = viewModel,
                                        onNavigate = navigate,
                                        screenId = Screen.SettingsAppearance.ID,
                                    )
                                }
                                composable(Screen.SettingsFeaturePacks.route) {
                                    com.tajemniktv.tajsos.ui.screens.settings.SettingsRoute(
                                        viewModel = viewModel,
                                        onNavigate = navigate,
                                        screenId = Screen.SettingsFeaturePacks.ID,
                                    )
                                }
                                composable(Screen.SettingsData.route) {
                                    com.tajemniktv.tajsos.ui.screens.settings.SettingsRoute(
                                        viewModel = viewModel,
                                        onNavigate = navigate,
                                        screenId = Screen.SettingsData.ID,
                                    )
                                }
                                composable(Screen.SettingsDebug.route) {
                                    com.tajemniktv.tajsos.ui.screens.settings.SettingsRoute(
                                        viewModel = viewModel,
                                        onNavigate = navigate,
                                        screenId = Screen.SettingsDebug.ID,
                                    )
                                }
                                composable(Screen.CalendarSettings.route) {
                                    CalendarSettingsRoute(
                                        viewModel = viewModel,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Projects.route) {
                                    com.tajemniktv.tajsos.ui.screens.projects.ProjectsRoute(
                                        viewModel = viewModel,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Areas.route) {
                                    com.tajemniktv.tajsos.ui.screens.areas.AreasRoute(
                                        viewModel = viewModel,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.ProjectDetail.route) {
                                    val projectId =
                                        it.savedStateHandle
                                            .get<Any>(Screen.PARAM_PROJECT_ID)
                                            ?.toString()
                                            ?.toLongOrNull() ?: -1L
                                    com.tajemniktv.tajsos.ui.screens.projects.detail.ProjectDetailRoute(
                                        viewModel = viewModel,
                                        projectId = projectId,
                                        onEditNode = onEditNode,
                                        onBack = { navController.popBackStack() },
                                        isDesktop = isDesktop,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.AreaDetail.route) {
                                    val areaId =
                                        it.savedStateHandle
                                            .get<Any>(Screen.PARAM_AREA_ID)
                                            ?.toString()
                                            ?.toLongOrNull() ?: -1L
                                    com.tajemniktv.tajsos.ui.screens.areas.detail.AreaDetailRoute(
                                        viewModel = viewModel,
                                        areaId = areaId,
                                        onNavigateToProject = { id ->
                                            navigate(
                                                Screen.ProjectDetail.route.replace(
                                                    "{${Screen.PARAM_PROJECT_ID}}",
                                                    id.toString(),
                                                ),
                                            )
                                        },
                                        onEditNode = onEditNode,
                                        onBack = { navController.popBackStack() },
                                        isDesktop = isDesktop,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.NoteDetail.route) {
                                    val noteId =
                                        it.savedStateHandle
                                            .get<Any>(Screen.PARAM_NOTE_ID)
                                            ?.toString()
                                            ?.toLongOrNull() ?: -1L
                                    NoteDetailScreen(
                                        viewModel = viewModel,
                                        noteId = noteId,
                                        onBack = { navController.popBackStack() },
                                        onNavigateToNode = onEditNode,
                                        onNavigateToSearch = { navigate(Screen.Search.route) },
                                        isDesktop = isDesktop,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.TaskDetail.route) {
                                    val taskId =
                                        it.savedStateHandle
                                            .get<Any>(Screen.PARAM_TASK_ID)
                                            ?.toString()
                                            ?.toLongOrNull() ?: -1L
                                    com.tajemniktv.tajsos.ui.screens.tasks.detail.TaskDetailRoute(
                                        viewModel = viewModel,
                                        taskId = taskId,
                                        onBack = { navController.popBackStack() },
                                        onNavigateToNode = onEditNode,
                                        onNavigateToSearch = { navigate(Screen.Search.route) },
                                        isDesktop = isDesktop,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.RecordDetail.route) {
                                    val recordId =
                                        it.savedStateHandle
                                            .get<Any>(Screen.PARAM_RECORD_ID)
                                            ?.toString()
                                            ?.toLongOrNull() ?: -1L
                                    com.tajemniktv.tajsos.ui.screens.records.detail.RecordDetailRoute(
                                        viewModel = viewModel,
                                        recordId = recordId,
                                        onBack = { navController.popBackStack() },
                                        onNavigateToNode = onEditNode,
                                        onNavigateToSearch = { navigate(Screen.Search.route) },
                                        isDesktop = isDesktop,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Insights.route) {
                                    com.tajemniktv.tajsos.ui.screens.insights.InsightsRoute(
                                        viewModel = viewModel,
                                        onNavigateToProject = {
                                            navigate(
                                                Screen.ProjectDetail.route.replace(
                                                    "{${Screen.PARAM_PROJECT_ID}}",
                                                    it.toString(),
                                                ),
                                            )
                                        },
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Graph.route) {
                                    com.tajemniktv.tajsos.ui.screens.graph.GraphRoute(
                                        viewModel = viewModel,
                                        onNodeClick = onEditNode,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Archive.route) {
                                    com.tajemniktv.tajsos.ui.screens.archive.ArchiveRoute(
                                        viewModel = viewModel,
                                        onEditNode = onEditNode,
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Review.route) {
                                    com.tajemniktv.tajsos.ui.screens.review.ReviewRoute(
                                        viewModel = viewModel,
                                        onBack = { navController.popBackStack() },
                                        onNavigate = navigate,
                                    )
                                }
                                composable(Screen.Profile.route) {
                                    com.tajemniktv.tajsos.ui.screens.profile.ProfileRoute(
                                        viewModel = viewModel,
                                        onNavigate = navigate,
                                        onPickAvatar = onPickAvatar,
                                        pickedAvatarRef = avatarPickResult,
                                        onAvatarPickConsume = onAvatarPickConsume,
                                    )
                                }
                            }

                            if (!isDesktop || (currentDestination?.route != Screen.Dashboard.route)) {
                                FloatingActionButton(
                                    onClick = { showCaptureSheetState = true },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = stringResource(Res.string.nav_capture),
                                    )
                                }
                            }

                            AppCaptureSheet(
                                show = showCaptureSheetState,
                                onDismiss = { showCaptureSheetState = false },
                                viewModel = viewModel,
                                voiceResult = voiceCaptureResult,
                                onVoiceConsume = onVoiceCaptureConsume,
                                projects = stableProjects,
                                areas = stableAreas,
                                templates = stableTemplates,
                                defaultProjectId = lastActiveProjectId,
                                defaultAreaId = lastActiveAreaId,
                                onVoiceClick = onVoiceCapture,
                                contextScreen = currentDestination?.route,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders the capture sheet overlay.
 *
 * @param show Whether to show the sheet.
 * @param onDismiss Callback to close.
 * @param viewModel Main ViewModel.
 * @param voiceResult Voice text result.
 * @param onVoiceConsume Callback to clear voice result.
 * @param projects Projects list.
 * @param areas Areas list.
 * @param templates Templates list.
 * @param defaultProjectId Default project.
 * @param defaultAreaId Default area.
 * @param onVoiceClick Voice click handler.
 * @param contextScreen Originating screen.
 */
@Composable
private fun AppCaptureSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    viewModel: MainViewModel,
    voiceResult: String?,
    onVoiceConsume: () -> Unit,
    projects: StableList<NodeEntity>,
    areas: StableList<NodeEntity>,
    templates: StableList<TemplateEntity>,
    defaultProjectId: Long?,
    defaultAreaId: Long?,
    onVoiceClick: (() -> Unit)?,
    contextScreen: String?,
) {
    if (show) {
        CaptureSheet(
            onDismiss = {
                onDismiss()
                onVoiceConsume()
            },
            onCapture = { text, type, projectId, areaId, isRec, recInt, remAt, ctx, sticky, decisionCat ->
                handleOnCapture(
                    viewModel = viewModel,
                    text = text,
                    type = type,
                    projectId = projectId,
                    areaId = areaId,
                    isRec = isRec,
                    recInt = recInt,
                    remAt = remAt,
                    ctx = ctx,
                    sticky = sticky,
                    decisionCat = decisionCat,
                )
            },
            projects = projects.items,
            areas = areas.items,
            templates = templates.items,
            defaultProjectId = defaultProjectId,
            defaultAreaId = defaultAreaId,
            initialText = voiceResult ?: "",
            onVoiceCaptureClick = onVoiceClick,
            contextScreen = contextScreen,
        )
    }
}

/**
 * Handles node capture submission from the capture sheet, delegating to the appropriate ViewModel action.
 *
 * @param viewModel The main ViewModel to trigger capture actions.
 * @param text The node title or raw text.
 * @param type The node type (inbox, project, area, or node type).
 * @param projectId Optional target project.
 * @param areaId Optional target area.
 * @param isRec Whether the node is recurring.
 * @param recInt Recurrence interval.
 * @param remAt Reminder timestamp.
 * @param ctx Creating screen context.
 * @param sticky Whether the node is pinned.
 * @param decisionCat Optional decision category.
 */
private fun handleOnCapture(
    viewModel: MainViewModel,
    text: String,
    type: String,
    projectId: Long?,
    areaId: Long?,
    isRec: Boolean,
    recInt: String?,
    remAt: Long?,
    ctx: String?,
    sticky: Boolean,
    decisionCat: String?,
) {
    when (type) {
        CAPTURE_TYPE_INBOX -> {
            viewModel.captureInboxEntry(
                rawText = text,
                areaId = areaId,
                projectId = projectId,
                contextScreen = ctx,
            )
        }

        ItemKind.PROJECT.storageKey -> {
            viewModel.addProject(text, areaId = areaId)
        }

        ItemKind.AREA.storageKey -> {
            viewModel.addArea(text)
        }

        else -> {
            viewModel.addNode(
                text,
                type = type,
                projectId = projectId,
                areaId = areaId,
                isRecurring = isRec,
                recurringInterval = recInt,
                reminderAt = remAt,
                contextScreen = ctx,
                isSticky = sticky,
                decisionCategory = decisionCat,
            )
        }
    }
}
